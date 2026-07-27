package com.test.minispring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.test.minispring.web.MiniSpringDemoService.BeanView;
import com.test.minispring.web.MiniSpringDemoService.FlowStep;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用于可视化 IoC 演示的无额外依赖本地 HTTP 服务。
 *
 * 本类刻意保持为薄传输层：端点 handler 调用 MiniSpringDemoService 并序列化其结果，
 * 核心容器无需感知 HTTP 和浏览器相关逻辑。
 */
public class MiniSpringWebServer {

    private static final int DEFAULT_PORT = 18080;
    private static final MiniSpringDemoService DEMO_SERVICE = new MiniSpringDemoService();

    public static void main(String[] args) throws IOException {
        // 端口可来自参数或环境变量，方便面试现场切换；默认值保证 README 命令可直接复制运行。
        int port = resolvePort(args, System.getenv("MINI_SPRING_WEB_PORT"));
        HttpServer server = createServer(port);
        server.start();
        System.out.println("Mini-Spring IoC Visualizer started at http://127.0.0.1:" + port);
    }

    static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        // 静态页面加小型 JSON 接口，让浏览器可分别查看每个 IoC 阶段，而不只看到测试结果。
        createGetContext(server, "/", MiniSpringWebServer::handleIndex);
        createGetContext(server, "/api/health", MiniSpringWebServer::handleHealth);
        createGetContext(server, "/api/security", MiniSpringWebServer::handleSecurity);
        createGetContext(server, "/api/beans", MiniSpringWebServer::handleBeans);
        createGetContext(server, "/api/user", MiniSpringWebServer::handleUser);
        createGetContext(server, "/api/xml", MiniSpringWebServer::handleXml);
        createGetContext(server, "/api/trace", MiniSpringWebServer::handleTrace);
        createGetContext(server, "/api/flow", MiniSpringWebServer::handleFlow);
        server.setExecutor(null);
        return server;
    }

    static int resolvePort(String[] args, String envPort) {
        String rawPort = null;
        if (args.length > 0) {
            rawPort = args[0];
        } else if (envPort != null && !envPort.isBlank()) {
            rawPort = envPort;
        }
        if (rawPort == null) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(rawPort);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Port must be an integer", exception);
        }
    }

    private static void createGetContext(HttpServer server, String path, HttpHandler handler) {
        server.createContext(path, exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "GET");
                sendJson(exchange, 405, errorJson("METHOD_NOT_ALLOWED", "Only GET is supported."));
                return;
            }
            handler.handle(exchange);
        });
    }

    private static void handleIndex(HttpExchange exchange) throws IOException {
        send(exchange, 200, "text/html; charset=utf-8", indexHtml());
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"ok\":true,\"service\":\"mini-spring-ioc-visualizer\"}");
    }

    private static void handleSecurity(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"localOnly\":true,\"binding\":\"127.0.0.1\","
                + "\"allowedMethods\":[\"GET\"],\"xml\":{\"doctypeAllowed\":false,"
                + "\"externalEntitiesAllowed\":false,\"externalDtdAllowed\":false,"
                + "\"configSource\":\"trusted-classpath\"}}");
    }

    private static void handleBeans(HttpExchange exchange) throws IOException {
        // 此接口展示 BeanDefinition 名称和实例类型，体现“配置元数据”和“运行对象”的关键区别。
        try {
            List<BeanView> beans = DEMO_SERVICE.listBeans();
            StringBuilder json = new StringBuilder();
            json.append("{\"beans\":[");
            for (int i = 0; i < beans.size(); i++) {
                BeanView bean = beans.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{\"name\":\"").append(escapeJson(bean.name()))
                        .append("\",\"className\":\"").append(escapeJson(bean.className()))
                        .append("\",\"displayClassName\":\"").append(escapeJson(bean.displayClassName()))
                        .append("\",\"role\":\"").append(escapeJson(bean.role()))
                        .append("\",\"proxied\":").append(bean.proxied())
                        .append("}");
            }
            json.append("]}");
            sendJson(exchange, 200, json.toString());
        } catch (Exception e) {
            sendJson(exchange, 500, errorJson("BEAN_LOAD_FAILED", e.getMessage()));
        }
    }

    private static void handleUser(HttpExchange exchange) throws IOException {
        // 成功响应证明真实容器中已注入的 Controller -> Service -> Dao 链路能够端到端运行。
        try {
            String userInfo = DEMO_SERVICE.queryUserInfo();
            sendJson(exchange, 200, "{\"result\":\"" + escapeJson(userInfo) + "\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, errorJson("DEMO_QUERY_FAILED", e.getMessage()));
        }
    }

    private static void handleXml(HttpExchange exchange) throws IOException {
        String xml = DEMO_SERVICE.readXmlConfig();
        sendJson(exchange, 200, "{\"xml\":\"" + escapeJson(xml) + "\"}");
    }

    private static void handleTrace(HttpExchange exchange) throws IOException {
        List<String> steps = DEMO_SERVICE.traceSteps();
        StringBuilder json = new StringBuilder();
        json.append("{\"steps\":");
        appendStringArray(json, steps);
        json.append("}");
        sendJson(exchange, 200, json.toString());
    }

    private static void handleFlow(HttpExchange exchange) throws IOException {
        // FlowStep 使用结构化 JSON，使 UI 能把可视化节点映射到对应源码与依赖关系。
        List<FlowStep> steps = DEMO_SERVICE.visualFlow();
        StringBuilder json = new StringBuilder();
        json.append("{\"steps\":[");
        for (int i = 0; i < steps.size(); i++) {
            FlowStep step = steps.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"id\":\"").append(escapeJson(step.id()))
                    .append("\",\"title\":\"").append(escapeJson(step.title()))
                    .append("\",\"description\":\"").append(escapeJson(step.description()))
                    .append("\",\"codeReference\":\"").append(escapeJson(step.codeReference()))
                    .append("\",\"activeNodes\":");
            appendStringArray(json, step.activeNodes());
            json.append(",\"activeEdges\":");
            appendStringArray(json, step.activeEdges());
            json.append("}");
        }
        json.append("]}");
        sendJson(exchange, 200, json.toString());
    }

    private static void appendStringArray(StringBuilder json, List<String> values) {
        json.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(values.get(i))).append("\"");
        }
        json.append("]");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        send(exchange, statusCode, "application/json; charset=utf-8", body);
    }

    private static void send(HttpExchange exchange, int statusCode, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static String errorJson(String code, String message) {
        return "{\"error\":{\"code\":\"" + escapeJson(code) + "\",\"message\":\""
                + escapeJson(message == null ? "Unknown error" : message) + "\"}}";
    }

    private static String escapeJson(String value) {
        // 本服务刻意不引入 JSON 库，因此所有动态字符串写入响应体前必须转义。
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private static String indexHtml() {
        return """
                <!doctype html>
                <html lang="zh-CN">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Mini-Spring IoC Visualizer</title>
                  <style>
                    :root {
                      --ink: #172033;
                      --muted: #5b667a;
                      --line: #d8dee8;
                      --panel: #ffffff;
                      --blue: #2563eb;
                      --green: #059669;
                      --orange: #d97706;
                      --red: #dc2626;
                      --bg: #f4f7fb;
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      min-height: 100vh;
                      font-family: "Microsoft YaHei", "Segoe UI", Arial, sans-serif;
                      background: var(--bg);
                      color: var(--ink);
                    }
                    button {
                      border: 1px solid transparent;
                      border-radius: 8px;
                      padding: 11px 16px;
                      font-size: 15px;
                      font-weight: 700;
                      cursor: pointer;
                      background: var(--blue);
                      color: white;
                    }
                    button.secondary { background: white; color: var(--ink); border-color: var(--line); }
                    button.warning { background: var(--orange); }
                    button:disabled { opacity: .45; cursor: not-allowed; }
                    .topbar {
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      gap: 20px;
                      padding: 18px 24px;
                      background: #101827;
                      color: white;
                      border-bottom: 4px solid #22c55e;
                    }
                    .title h1 { margin: 0; font-size: 26px; }
                    .title p { margin: 5px 0 0; color: #cbd5e1; font-size: 15px; }
                    .toolbar { display: flex; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }
                    .workspace {
                      display: grid;
                      grid-template-columns: 300px minmax(620px, 1fr) 360px;
                      gap: 16px;
                      padding: 16px;
                      min-height: calc(100vh - 89px);
                    }
                    .panel {
                      background: var(--panel);
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      box-shadow: 0 12px 28px rgba(15, 23, 42, .06);
                      min-width: 0;
                    }
                    .sidebar, .inspector { padding: 16px; overflow: auto; }
                    .sidebar h2, .inspector h2, .stage h2 {
                      margin: 0 0 12px;
                      font-size: 18px;
                    }
                    .steps { display: grid; gap: 9px; }
                    .step-item {
                      width: 100%;
                      text-align: left;
                      background: #f8fafc;
                      color: var(--ink);
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      padding: 10px 12px;
                      cursor: pointer;
                    }
                    .step-item strong { display: block; font-size: 14px; margin-bottom: 3px; }
                    .step-item span { display: block; color: var(--muted); font-size: 13px; line-height: 1.35; }
                    .step-item.active {
                      border-color: var(--blue);
                      background: #eff6ff;
                      box-shadow: inset 4px 0 0 var(--blue);
                    }
                    .step-item.done { border-color: #bbf7d0; background: #f0fdf4; }
                    .stage {
                      padding: 16px;
                      display: flex;
                      flex-direction: column;
                      min-height: 680px;
                    }
                    .stage-head {
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      gap: 16px;
                      margin-bottom: 12px;
                    }
                    .progress-wrap {
                      flex: 1;
                      min-width: 180px;
                      height: 10px;
                      background: #e5e7eb;
                      border-radius: 999px;
                      overflow: hidden;
                    }
                    .progress-bar {
                      height: 100%;
                      width: 0;
                      background: linear-gradient(90deg, var(--blue), var(--green));
                      transition: width .25s ease;
                    }
                    .flow-map {
                      position: relative;
                      flex: 1;
                      min-height: 560px;
                      border: 1px solid #dbe3ef;
                      border-radius: 8px;
                      background:
                        linear-gradient(90deg, rgba(37, 99, 235, .05) 1px, transparent 1px),
                        linear-gradient(rgba(37, 99, 235, .05) 1px, transparent 1px),
                        #fbfdff;
                      background-size: 34px 34px;
                      overflow: hidden;
                    }
                    .flow-svg {
                      position: absolute;
                      inset: 0;
                      width: 100%;
                      height: 100%;
                      pointer-events: none;
                    }
                    .flow-svg path {
                      stroke: #aeb8c8;
                      stroke-width: 1.6;
                      fill: none;
                      opacity: .62;
                      transition: stroke .2s ease, stroke-width .2s ease, opacity .2s ease;
                    }
                    .flow-svg path.active {
                      stroke: var(--blue);
                      stroke-width: 3.3;
                      opacity: 1;
                      stroke-dasharray: 7 5;
                      animation: dash 1.1s linear infinite;
                    }
                    @keyframes dash { to { stroke-dashoffset: -24; } }
                    .node {
                      position: absolute;
                      width: 150px;
                      min-height: 88px;
                      transform: translate(-50%, -50%);
                      padding: 12px;
                      border: 2px solid #d6deea;
                      border-radius: 8px;
                      background: white;
                      box-shadow: 0 10px 20px rgba(15, 23, 42, .08);
                      transition: transform .22s ease, border-color .22s ease, box-shadow .22s ease, background .22s ease;
                    }
                    .node.active {
                      border-color: var(--blue);
                      background: #eff6ff;
                      transform: translate(-50%, -50%) scale(1.05);
                      box-shadow: 0 16px 32px rgba(37, 99, 235, .22);
                    }
                    .node.bean.active { border-color: var(--green); background: #ecfdf5; }
                    .node.cache.active { border-color: var(--orange); background: #fff7ed; }
                    .node small {
                      display: block;
                      color: var(--muted);
                      font-size: 12px;
                      line-height: 1.3;
                      margin-bottom: 5px;
                    }
                    .node strong {
                      display: block;
                      font-size: 15px;
                      line-height: 1.25;
                      overflow-wrap: anywhere;
                    }
                    .node code {
                      display: block;
                      margin-top: 7px;
                      font-size: 12px;
                      color: #334155;
                      overflow-wrap: anywhere;
                    }
                    .detail-block {
                      border-top: 1px solid var(--line);
                      padding-top: 14px;
                      margin-top: 14px;
                    }
                    .detail-title { font-size: 24px; margin: 0 0 10px; }
                    .detail-text { color: #334155; font-size: 16px; line-height: 1.7; margin: 0; }
                    .code-ref {
                      display: block;
                      margin-top: 12px;
                      padding: 10px 12px;
                      border-radius: 8px;
                      background: #f8fafc;
                      border: 1px solid var(--line);
                      color: #0f172a;
                      font-size: 13px;
                      overflow-wrap: anywhere;
                    }
                    .runtime {
                      display: grid;
                      grid-template-columns: 1fr 1fr;
                      gap: 12px;
                      margin-top: 14px;
                    }
                    .runtime-section {
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      padding: 12px;
                      min-width: 0;
                      background: #ffffff;
                    }
                    .runtime-section h3 { margin: 0 0 8px; font-size: 15px; }
                    pre {
                      margin: 0;
                      max-height: 210px;
                      overflow: auto;
                      white-space: pre-wrap;
                      overflow-wrap: anywhere;
                      border-radius: 8px;
                      background: #0f172a;
                      color: #e5e7eb;
                      padding: 12px;
                      font-size: 13px;
                      line-height: 1.55;
                    }
                    .bean-list {
                      display: grid;
                      gap: 8px;
                    }
                    .bean-row {
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      padding: 9px 10px;
                      background: #f8fafc;
                    }
                    .bean-row strong { display: block; font-size: 14px; }
                    .bean-row span { display: block; color: var(--muted); font-size: 12px; line-height: 1.4; overflow-wrap: anywhere; }
                    .bean-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
                    .bean-badge { padding: 3px 6px; border-radius: 4px; background: #e0f2fe; color: #075985; font-size: 11px; font-weight: 700; }
                    details { margin-top: 10px; }
                    summary { color: var(--blue); cursor: pointer; font-size: 12px; font-weight: 700; }
                    details code { display: block; margin-top: 7px; color: #475569; font-size: 11px; overflow-wrap: anywhere; }
                    .plain-result { min-height: 92px; padding: 12px; border: 1px solid #dbe3ef; border-radius: 8px; background: #f8fafc; line-height: 1.65; }
                    .plain-result strong { display: block; margin-bottom: 5px; }
                    .plain-result.pending { color: var(--muted); }
                    .outcome-band {
                      width: min(1440px, calc(100% - 48px));
                      margin: 12px auto 0;
                      padding: 14px 16px;
                      background: #fff;
                      border: 1px solid var(--line);
                      border-left: 4px solid var(--blue);
                      border-radius: 8px;
                    }
                    .outcome-band h2 { margin: 0 0 6px; font-size: 16px; }
                    .outcome-band p { margin: 0; color: #334155; line-height: 1.55; }
                    .outcome-chain { color: var(--blue); font-weight: 700; }
                    .glossary-band {
                      width: min(1440px, calc(100% - 48px));
                      margin: 12px auto 0;
                      padding: 14px 16px;
                      background: #fff;
                      border: 1px solid var(--line);
                      border-left: 4px solid var(--orange);
                      border-radius: 8px;
                    }
                    .glossary-band h2 { margin: 0 0 10px; font-size: 16px; }
                    .glossary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px 18px; }
                    .term-item { margin: 0; padding-left: 10px; border-left: 2px solid #fed7aa; color: #334155; font-size: 13px; line-height: 1.55; }
                    .term-item strong { color: var(--ink); }
                    .xml-guide { margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--line); }
                    .xml-guide h4 { margin: 0 0 7px; font-size: 14px; }
                    .xml-guide ol { margin: 0; padding-left: 22px; color: #334155; font-size: 13px; line-height: 1.55; }
                    .xml-legend { display: grid; grid-template-columns: 1fr 1fr; gap: 5px 10px; margin-top: 9px; font-size: 12px; color: var(--muted); }
                    .xml-legend b { color: var(--ink); }
                    .section-help { margin: -5px 0 10px; color: var(--muted); font-size: 12px; line-height: 1.55; }
                    .bean-class { margin: 4px 0 5px; color: #334155 !important; }
                    .status {
                      min-height: 22px;
                      margin: 0 0 12px;
                      color: var(--muted);
                      line-height: 1.5;
                    }
                    .guardrail-band {
                      width: min(1440px, calc(100% - 48px));
                      margin: 16px auto 0;
                      padding: 13px 14px;
                      background: #eef8f3;
                      border: 1px solid #b8dcc8;
                      border-radius: 8px;
                    }
                    .guardrail-head { display: flex; justify-content: space-between; gap: 12px; align-items: center; margin-bottom: 9px; }
                    .guardrail-head h2 { margin: 0; font-size: 15px; }
                    .guardrail-head span { color: var(--green); font-size: 12px; font-weight: 700; }
                    .guardrail-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 8px; }
                    .guardrail-item { min-width: 0; padding: 8px 9px; background: #fff; border: 1px solid #d2e8dc; border-radius: 6px; }
                    .guardrail-item span, .guardrail-item strong { display: block; overflow-wrap: anywhere; }
                    .guardrail-item span { color: var(--muted); font-size: 11px; margin-bottom: 4px; }
                    .guardrail-item strong { font-size: 13px; }
                    @media (max-width: 1340px) {
                      .workspace { grid-template-columns: 260px 1fr; }
                      .inspector { grid-column: 1 / -1; }
                    }
                    @media (max-width: 820px) {
                      .topbar { align-items: flex-start; flex-direction: column; }
                      .toolbar { justify-content: flex-start; }
                      .workspace { grid-template-columns: 1fr; }
                      .stage { min-height: 0; }
                      .flow-map {
                        display: grid;
                        grid-template-columns: 1fr;
                        gap: 8px;
                        min-height: 0;
                        padding: 12px;
                        overflow: visible;
                      }
                      .flow-svg { display: none; }
                      .node, .node.active {
                        position: relative;
                        left: auto !important;
                        top: auto !important;
                        width: 100%;
                        min-height: 0;
                        transform: none;
                      }
                      .runtime { grid-template-columns: 1fr; }
                      .guardrail-grid { grid-template-columns: 1fr; }
                      .glossary-grid, .xml-legend { grid-template-columns: 1fr; }
                    }
                  </style>
                </head>
                <body>
                  <header class="topbar">
                    <div class="title">
                      <h1>Mini-Spring IoC Visualizer</h1>
                      <p>逐步观察一份配置如何变成三个能协作的 Java 对象，以及容器怎样自动把它们连接起来。</p>
                    </div>
                    <div class="toolbar">
                      <button id="prevBtn" class="secondary">上一步</button>
                      <button id="nextBtn">下一步</button>
                      <button id="playBtn" class="warning">自动播放</button>
                      <button id="resetBtn" class="secondary">重置</button>
                    </div>
                  </header>

                  <section class="guardrail-band" aria-labelledby="guardrailTitle">
                    <div class="guardrail-head">
                      <h2 id="guardrailTitle">运行护栏</h2>
                      <span>XML 与 Web 边界已启用</span>
                    </div>
                    <div id="guardrailGrid" class="guardrail-grid"></div>
                  </section>

                  <section class="outcome-band" id="learningOutcome" aria-labelledby="learningOutcomeTitle">
                    <h2 id="learningOutcomeTitle">这套容器最终做成了什么</h2>
                    <p>它会读取一份 XML 配置，自动创建三个对象并连接成
                      <span class="outcome-chain">调用入口 userController -> 业务服务 userService -> 数据访问 userDao</span>。
                      走到第 10 步后，页面会用真实查询结果证明这条依赖链可以工作。</p>
                  </section>

                  <section class="glossary-band" id="glossary" aria-labelledby="glossaryTitle">
                    <h2 id="glossaryTitle">先认识 6 个名词</h2>
                    <div class="glossary-grid">
                      <p class="term-item">Bean：由容器创建和管理的 Java 对象。</p>
                      <p class="term-item">IoC 容器：负责创建、保存和连接这些对象的程序。</p>
                      <p class="term-item">依赖注入：容器自动把一个对象需要的另一个对象连接进去。</p>
                      <p class="term-item">AOP 代理：在不改业务代码的前提下附加通用功能。</p>
                      <p class="term-item">BeanDefinition：配置被读懂后形成的“对象说明书”，还不是真正对象。</p>
                      <p class="term-item">单例：同一个名字多次获取时，容器复用同一个对象。</p>
                    </div>
                  </section>

                  <main class="workspace">
                    <aside class="panel sidebar">
                      <h2>创建流程</h2>
                      <p class="status" id="status">正在加载流程数据...</p>
                      <div class="steps" id="stepList"></div>
                    </aside>

                    <section class="panel stage">
                      <div class="stage-head">
                        <h2 id="stageTitle">IoC 容器创建流程</h2>
                        <div class="progress-wrap" aria-hidden="true">
                          <div class="progress-bar" id="progressBar"></div>
                        </div>
                      </div>
                      <div class="flow-map" aria-label="Mini-Spring IoC flow map">
                        <svg class="flow-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
                          <defs>
                            <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
                              <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"></path>
                            </marker>
                          </defs>
                          <path data-edge="xml-reader" d="M 17 14 L 28 14" marker-end="url(#arrow)"></path>
                          <path data-edge="reader-definition" d="M 40 14 L 51 14" marker-end="url(#arrow)"></path>
                          <path data-edge="definition-registry" d="M 63 14 L 74 14" marker-end="url(#arrow)"></path>
                          <path data-edge="registry-factory" d="M 82 23 C 82 38, 55 34, 50 42" marker-end="url(#arrow)"></path>
                          <path data-edge="factory-dao" d="M 45 50 C 35 58, 25 58, 20 66" marker-end="url(#arrow)"></path>
                          <path data-edge="factory-service" d="M 50 51 L 50 64" marker-end="url(#arrow)"></path>
                          <path data-edge="factory-controller" d="M 55 50 C 65 58, 74 58, 80 66" marker-end="url(#arrow)"></path>
                          <path data-edge="dao-service" d="M 28 72 L 41 72" marker-end="url(#arrow)"></path>
                          <path data-edge="service-controller" d="M 59 72 L 72 72" marker-end="url(#arrow)"></path>
                          <path data-edge="dao-singletons" d="M 22 79 C 28 89, 38 89, 45 88" marker-end="url(#arrow)"></path>
                          <path data-edge="service-singletons" d="M 50 79 L 50 84" marker-end="url(#arrow)"></path>
                          <path data-edge="controller-singletons" d="M 78 79 C 72 89, 61 89, 55 88" marker-end="url(#arrow)"></path>
                          <path data-edge="singletons-client" d="M 58 88 L 76 88" marker-end="url(#arrow)"></path>
                          <path data-edge="client-controller" d="M 88 84 C 93 73, 91 70, 87 70" marker-end="url(#arrow)"></path>
                        </svg>

                        <div class="node" data-node="xml" style="left:14%;top:14%;">
                          <small>第 1 步</small><strong>找到配置文件</strong><code>技术名：spring.xml</code>
                        </div>
                        <div class="node" data-node="reader" style="left:38%;top:14%;">
                          <small>第 2 步</small><strong>读懂配置</strong><code>技术名：XmlBeanDefinitionReader</code>
                        </div>
                        <div class="node" data-node="definition" style="left:62%;top:14%;">
                          <small>第 2 步的产物</small><strong>生成对象说明书</strong><code>技术名：BeanDefinition</code>
                        </div>
                        <div class="node" data-node="registry" style="left:86%;top:14%;">
                          <small>第 3 步</small><strong>记住三份说明书</strong><code>技术名：BeanDefinitionRegistry</code>
                        </div>
                        <div class="node" data-node="factory" style="left:50%;top:46%;">
                          <small>第 4-9 步</small><strong>按说明书创建对象</strong><code>技术名：BeanFactory</code>
                        </div>
                        <div class="node bean" data-node="dao" style="left:20%;top:73%;">
                          <small>数据访问对象</small><strong>提供用户数据</strong><code>对象名：userDao</code>
                        </div>
                        <div class="node bean" data-node="service" style="left:50%;top:73%;">
                          <small>业务服务对象</small><strong>处理业务</strong><code>对象名：userService</code>
                        </div>
                        <div class="node bean" data-node="controller" style="left:80%;top:73%;">
                          <small>调用入口对象</small><strong>接收外部查询</strong><code>对象名：userController</code>
                        </div>
                        <div class="node cache" data-node="singletons" style="left:50%;top:90%;">
                          <small>第 9 步</small><strong>保存可复用对象</strong><code>技术名：singletonObjects</code>
                        </div>
                        <div class="node" data-node="client" style="left:86%;top:90%;">
                          <small>第 10 步</small><strong>取出入口开始查询</strong><code>技术名：getBean()</code>
                        </div>
                      </div>

                      <div class="runtime">
                        <div class="runtime-section">
                          <h3>当前运行结果</h3>
                          <div id="userResult" class="plain-result pending">尚未执行 getBean("userController")。走到第 10 步后显示最终结果。</div>
                        </div>
                        <div class="runtime-section">
                          <h3>当前配置变化</h3>
                          <div id="xmlExplanation" class="plain-result">正在加载 XML...</div>
                          <div class="xml-guide">
                            <h4>每段配置是什么意思</h4>
                            <ol>
                              <li><strong>userDao：</strong>创建负责提供用户数据的对象。</li>
                              <li><strong>userService：</strong>创建业务对象，填入公司名称，并连接 userDao。</li>
                              <li><strong>userController：</strong>创建调用入口，并连接 userService。</li>
                            </ol>
                            <div class="xml-legend">
                              <span>id 是对象在容器里的名字</span>
                              <span>class 是要创建的 Java 类</span>
                              <span>value 是直接填入的文字或数字</span>
                              <span>ref 表示引用另一个 Bean</span>
                            </div>
                          </div>
                          <details>
                            <summary>技术详情：查看完整 spring.xml</summary>
                            <pre id="xmlSource">正在加载 XML...</pre>
                          </details>
                        </div>
                      </div>
                    </section>

                    <aside class="panel inspector">
                      <h2>当前步骤</h2>
                      <p class="status" id="stepCounter">Step 0 / 0</p>
                      <h3 class="detail-title" id="detailTitle">等待加载</h3>
                      <p class="detail-text" id="detailText">流程数据加载后，这里会显示当前步骤的输入、输出和容器行为。</p>
                      <details>
                        <summary>技术详情：查看对应源码</summary>
                        <code class="code-ref" id="codeRef">code reference</code>
                      </details>
                      <div class="detail-block">
                        <h2>Bean 列表</h2>
                        <p class="section-help">下面每一项都是一个 Bean。这是容器自动创建并管理的 Java 对象；对象名用于查找，Java 类保存业务代码。“已由 AOP 增强”表示容器用代理包裹原对象，在不修改业务代码的情况下附加通用功能。</p>
                        <div class="bean-list" id="beanList"></div>
                      </div>
                      <div class="detail-block">
                        <h2>已经完成的步骤</h2>
                        <p class="section-help">这里只显示走到当前为止发生的事；技术类名放在“技术详情”中，需要时再查看。</p>
                        <pre id="traceText">正在加载流程...</pre>
                      </div>
                    </aside>
                  </main>

                  <script>
                    const state = {
                      steps: [],
                      beans: [],
                      userResult: '',
                      current: 0,
                      timer: null
                    };

                    const els = {
                      status: document.getElementById('status'),
                      stepList: document.getElementById('stepList'),
                      stageTitle: document.getElementById('stageTitle'),
                      progressBar: document.getElementById('progressBar'),
                      detailTitle: document.getElementById('detailTitle'),
                      detailText: document.getElementById('detailText'),
                      codeRef: document.getElementById('codeRef'),
                      stepCounter: document.getElementById('stepCounter'),
                      userResult: document.getElementById('userResult'),
                      xmlExplanation: document.getElementById('xmlExplanation'),
                      xmlSource: document.getElementById('xmlSource'),
                      beanList: document.getElementById('beanList'),
                      traceText: document.getElementById('traceText'),
                      guardrailGrid: document.getElementById('guardrailGrid'),
                      prevBtn: document.getElementById('prevBtn'),
                      nextBtn: document.getElementById('nextBtn'),
                      playBtn: document.getElementById('playBtn'),
                      resetBtn: document.getElementById('resetBtn')
                    };

                    async function getJson(url) {
                      const response = await fetch(url);
                      const data = await response.json();
                      if (!response.ok) {
                        throw new Error(data.error?.message || response.statusText);
                      }
                      return data;
                    }

                    function escapeHtml(value) {
                      return String(value)
                        .replaceAll('&', '&amp;')
                        .replaceAll('<', '&lt;')
                        .replaceAll('>', '&gt;')
                        .replaceAll('"', '&quot;')
                        .replaceAll("'", '&#039;');
                    }

                    function renderStepList() {
                      els.stepList.innerHTML = state.steps.map((step, index) => `
                        <button class="step-item" data-step-index="${index}">
                          <strong>${index + 1}. ${escapeHtml(step.title)}</strong>
                          <span>点击查看这一阶段发生了什么</span>
                        </button>
                      `).join('');
                      els.stepList.querySelectorAll('[data-step-index]').forEach(button => {
                        button.addEventListener('click', () => showStep(Number(button.dataset.stepIndex)));
                      });
                    }

                    function renderGuardrails(security) {
                      const xml = security.xml || {};
                      const items = [
                        ['访问范围', security.localOnly ? '仅本机' : security.binding],
                        ['HTTP 方法', (security.allowedMethods || []).join(', ')],
                        ['危险 XML 声明（DOCTYPE）', xml.doctypeAllowed ? '允许' : '已拒绝'],
                        ['XML 读取外部文件', xml.externalEntitiesAllowed || xml.externalDtdAllowed ? '允许' : '已拒绝'],
                        ['配置来源', xml.configSource === 'trusted-classpath' ? '只读项目内部配置' : xml.configSource]
                      ];
                      els.guardrailGrid.replaceChildren();
                      items.forEach(([label, value]) => {
                        const item = document.createElement('div');
                        item.className = 'guardrail-item';
                        const name = document.createElement('span');
                        const detail = document.createElement('strong');
                        name.textContent = label;
                        detail.textContent = value;
                        item.append(name, detail);
                        els.guardrailGrid.append(item);
                      });
                    }

                    function beanVisibleAt(beanName) {
                      return { userDao: 3, userService: 4, userController: 6 }[beanName] ?? 0;
                    }

                    function beanState(beanName) {
                      if (beanName === 'userService' && state.current < 5) return '对象已创建，等待注入 userDao';
                      if (beanName === 'userController' && state.current < 7) return '对象已创建，等待注入 userService';
                      if (state.current >= 8) return '依赖已连接，并已放入单例池';
                      return '对象已创建，所需依赖已连接';
                    }

                    function renderBeans() {
                      const visibleBeans = state.beans
                        .filter(bean => state.current >= beanVisibleAt(bean.name))
                        .sort((left, right) => beanVisibleAt(left.name) - beanVisibleAt(right.name));
                      if (!visibleBeans.length) {
                        els.beanList.innerHTML = '<div class="plain-result pending">目前只有配置蓝图，还没有创建 Java 对象。</div>';
                        return;
                      }
                      els.beanList.innerHTML = visibleBeans.map(bean => `
                        <div class="bean-row">
                          <div class="bean-head">
                            <strong>对象名：${escapeHtml(bean.name)}</strong>
                            ${bean.proxied ? '<span class="bean-badge">已由 AOP 增强</span>' : ''}
                          </div>
                          <span class="bean-class">Java 类：${escapeHtml(bean.displayClassName)}（实际业务代码）</span>
                          <span>${escapeHtml(bean.role)}</span>
                          <span>${escapeHtml(beanState(bean.name))}</span>
                          <details>
                            <summary>技术详情</summary>
                            <code>${escapeHtml(bean.className)}</code>
                          </details>
                        </div>
                      `).join('');
                    }

                    function renderCurrentState() {
                      const completedSteps = state.steps.slice(0, state.current + 1);
                      const currentStep = state.steps[state.current];
                      renderBeans();
                      els.traceText.textContent = completedSteps
                        .map((step, index) => `${index + 1}. ${step.title}\\n   ${step.description}`)
                        .join('\\n');

                      const xmlExplanations = {
                        'load-xml': '容器已找到 spring.xml。这份文件告诉容器需要管理哪些对象。',
                        'parse-definitions': '容器读懂三个 <bean> 标签，并为每个对象生成一份说明书；对象还没有创建。',
                        'register-definitions': '容器已经记住 userDao、userService、userController 的三份对象说明书。',
                        'create-dao': '容器开始照着说明书创建最底层的 userDao。',
                        'create-service': '容器创建 userService，并看到它还需要 company 值和 userDao。',
                        'inject-dao': 'ref="userDao" 表示需要另一个对象；容器自动把 userDao 交给 userService，这就是依赖注入。',
                        'create-controller': '容器创建对外调用入口 userController。',
                        'inject-service': '容器自动把 userService 交给 userController，三个对象现在已经连通。',
                        'store-singletons': '容器保存好三个对象；以后按同一个名字获取时，会复用原来的对象。',
                        'get-bean': '程序按名字取出 userController，并沿着入口、业务、数据三层完成查询。'
                      };
                      els.xmlExplanation.textContent = xmlExplanations[currentStep.id] || currentStep.description;

                      if (state.current === state.steps.length - 1) {
                        els.userResult.classList.remove('pending');
                        els.userResult.innerHTML = `<strong>依赖链运行成功</strong>
                          userController 调用 userService，userService 再调用 userDao。<br>
                          最终返回：${escapeHtml(state.userResult)}`;
                      } else {
                        els.userResult.classList.add('pending');
                        els.userResult.textContent = `当前完成 ${state.current + 1}/10 步。最终查询尚未执行；继续到第 10 步即可看到结果。`;
                      }
                    }

                    function showStep(index) {
                      if (!state.steps.length) return;
                      state.current = Math.max(0, Math.min(index, state.steps.length - 1));
                      const step = state.steps[state.current];
                      const activeNodes = new Set(step.activeNodes);
                      const activeEdges = new Set(step.activeEdges);

                      document.querySelectorAll('[data-node]').forEach(node => {
                        node.classList.toggle('active', activeNodes.has(node.dataset.node));
                      });
                      document.querySelectorAll('[data-edge]').forEach(edge => {
                        edge.classList.toggle('active', activeEdges.has(edge.dataset.edge));
                      });
                      els.stepList.querySelectorAll('.step-item').forEach((item, itemIndex) => {
                        item.classList.toggle('active', itemIndex === state.current);
                        item.classList.toggle('done', itemIndex < state.current);
                      });

                      els.stageTitle.textContent = step.title;
                      els.detailTitle.textContent = step.title;
                      els.detailText.textContent = step.description;
                      els.codeRef.textContent = step.codeReference;
                      els.stepCounter.textContent = `Step ${state.current + 1} / ${state.steps.length}`;
                      els.progressBar.style.width = `${((state.current + 1) / state.steps.length) * 100}%`;
                      els.prevBtn.disabled = state.current === 0;
                      els.nextBtn.disabled = state.current === state.steps.length - 1;
                      renderCurrentState();
                    }

                    function nextStep() {
                      if (state.current < state.steps.length - 1) {
                        showStep(state.current + 1);
                      } else {
                        stopPlayback();
                      }
                    }

                    function prevStep() {
                      showStep(state.current - 1);
                    }

                    function startPlayback() {
                      if (state.timer) {
                        stopPlayback();
                        return;
                      }
                      els.playBtn.textContent = '暂停';
                      state.timer = window.setInterval(nextStep, 1250);
                    }

                    function stopPlayback() {
                      if (state.timer) {
                        window.clearInterval(state.timer);
                        state.timer = null;
                      }
                      els.playBtn.textContent = '自动播放';
                    }

                    async function init() {
                      try {
                        const [flow, beans, xml, security, user] = await Promise.all([
                          getJson('/api/flow'),
                          getJson('/api/beans'),
                          getJson('/api/xml'),
                          getJson('/api/security'),
                          getJson('/api/user')
                        ]);
                        state.steps = flow.steps;
                        state.beans = beans.beans;
                        state.userResult = user.result;
                        renderStepList();
                        els.xmlSource.textContent = xml.xml;
                        renderGuardrails(security);
                        els.status.textContent = '流程已加载，可以逐步查看 IoC 容器创建 Bean 的过程。';
                        showStep(0);
                      } catch (error) {
                        els.status.textContent = `加载失败：${error.message}`;
                      }
                    }

                    els.prevBtn.addEventListener('click', prevStep);
                    els.nextBtn.addEventListener('click', nextStep);
                    els.playBtn.addEventListener('click', startPlayback);
                    els.resetBtn.addEventListener('click', () => {
                      stopPlayback();
                      showStep(0);
                    });

                    init();
                  </script>
                </body>
                </html>
                """;
    }
}
