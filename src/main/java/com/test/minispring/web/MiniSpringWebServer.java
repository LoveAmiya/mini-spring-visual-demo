package com.test.minispring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.test.minispring.web.MiniSpringDemoService.BeanView;
import com.test.minispring.web.MiniSpringDemoService.FlowStep;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MiniSpringWebServer {

    private static final int DEFAULT_PORT = 18080;
    private static final MiniSpringDemoService DEMO_SERVICE = new MiniSpringDemoService();

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", MiniSpringWebServer::handleIndex);
        server.createContext("/api/health", MiniSpringWebServer::handleHealth);
        server.createContext("/api/beans", MiniSpringWebServer::handleBeans);
        server.createContext("/api/user", MiniSpringWebServer::handleUser);
        server.createContext("/api/xml", MiniSpringWebServer::handleXml);
        server.createContext("/api/trace", MiniSpringWebServer::handleTrace);
        server.createContext("/api/flow", MiniSpringWebServer::handleFlow);
        server.setExecutor(null);
        server.start();
        System.out.println("Mini-Spring IoC Visualizer started at http://127.0.0.1:" + port);
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("MINI_SPRING_WEB_PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return DEFAULT_PORT;
    }

    private static void handleIndex(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, errorJson("METHOD_NOT_ALLOWED", "Only GET is supported."));
            return;
        }
        send(exchange, 200, "text/html; charset=utf-8", indexHtml());
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"ok\":true,\"service\":\"mini-spring-ioc-visualizer\"}");
    }

    private static void handleBeans(HttpExchange exchange) throws IOException {
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
                        .append("\",\"role\":\"").append(escapeJson(bean.role()))
                        .append("\"}");
            }
            json.append("]}");
            sendJson(exchange, 200, json.toString());
        } catch (Exception e) {
            sendJson(exchange, 500, errorJson("BEAN_LOAD_FAILED", e.getMessage()));
        }
    }

    private static void handleUser(HttpExchange exchange) throws IOException {
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
                      width: 168px;
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
                    .status {
                      min-height: 22px;
                      margin: 0 0 12px;
                      color: var(--muted);
                      line-height: 1.5;
                    }
                    @media (max-width: 1180px) {
                      .workspace { grid-template-columns: 260px 1fr; }
                      .inspector { grid-column: 1 / -1; }
                    }
                    @media (max-width: 820px) {
                      .topbar { align-items: flex-start; flex-direction: column; }
                      .toolbar { justify-content: flex-start; }
                      .workspace { grid-template-columns: 1fr; }
                      .stage { min-height: 620px; }
                      .flow-map { min-height: 520px; }
                      .runtime { grid-template-columns: 1fr; }
                    }
                  </style>
                </head>
                <body>
                  <header class="topbar">
                    <div class="title">
                      <h1>Mini-Spring IoC Visualizer</h1>
                      <p>逐步观察 XML 配置如何变成 BeanDefinition、Bean 实例、依赖注入链和 getBean 返回结果。</p>
                    </div>
                    <div class="toolbar">
                      <button id="prevBtn" class="secondary">上一步</button>
                      <button id="nextBtn">下一步</button>
                      <button id="playBtn" class="warning">自动播放</button>
                      <button id="resetBtn" class="secondary">重置</button>
                    </div>
                  </header>

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

                        <div class="node" data-node="xml" style="left:10%;top:14%;">
                          <small>配置文件</small><strong>spring.xml</strong><code>&lt;bean id="..." /&gt;</code>
                        </div>
                        <div class="node" data-node="reader" style="left:34%;top:14%;">
                          <small>读取器</small><strong>XmlBeanDefinitionReader</strong><code>loadBeanDefinitions()</code>
                        </div>
                        <div class="node" data-node="definition" style="left:57%;top:14%;">
                          <small>对象蓝图</small><strong>BeanDefinition</strong><code>class + properties</code>
                        </div>
                        <div class="node" data-node="registry" style="left:82%;top:14%;">
                          <small>定义注册表</small><strong>BeanDefinitionRegistry</strong><code>userDao/userService/userController</code>
                        </div>
                        <div class="node" data-node="factory" style="left:50%;top:46%;">
                          <small>创建中心</small><strong>BeanFactory</strong><code>createBean()</code>
                        </div>
                        <div class="node bean" data-node="dao" style="left:20%;top:73%;">
                          <small>数据层 Bean</small><strong>userDao</strong><code>TestUserDao</code>
                        </div>
                        <div class="node bean" data-node="service" style="left:50%;top:73%;">
                          <small>业务层 Bean</small><strong>userService</strong><code>userDao + company</code>
                        </div>
                        <div class="node bean" data-node="controller" style="left:80%;top:73%;">
                          <small>入口 Bean</small><strong>userController</strong><code>userService</code>
                        </div>
                        <div class="node cache" data-node="singletons" style="left:50%;top:90%;">
                          <small>单例缓存</small><strong>singletonObjects</strong><code>ready beans</code>
                        </div>
                        <div class="node" data-node="client" style="left:86%;top:90%;">
                          <small>应用调用</small><strong>getBean()</strong><code>"userController"</code>
                        </div>
                      </div>

                      <div class="runtime">
                        <div class="runtime-section">
                          <h3>运行结果</h3>
                          <pre id="userResult">等待执行 getBean("userController")...</pre>
                        </div>
                        <div class="runtime-section">
                          <h3>spring.xml</h3>
                          <pre id="xmlSource">正在加载 XML...</pre>
                        </div>
                      </div>
                    </section>

                    <aside class="panel inspector">
                      <h2>当前步骤</h2>
                      <p class="status" id="stepCounter">Step 0 / 0</p>
                      <h3 class="detail-title" id="detailTitle">等待加载</h3>
                      <p class="detail-text" id="detailText">流程数据加载后，这里会显示当前步骤的输入、输出和容器行为。</p>
                      <code class="code-ref" id="codeRef">code reference</code>
                      <div class="detail-block">
                        <h2>Bean 列表</h2>
                        <div class="bean-list" id="beanList"></div>
                      </div>
                      <div class="detail-block">
                        <h2>执行轨迹</h2>
                        <pre id="traceText">正在加载 trace...</pre>
                      </div>
                    </aside>
                  </main>

                  <script>
                    const state = {
                      steps: [],
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
                      xmlSource: document.getElementById('xmlSource'),
                      beanList: document.getElementById('beanList'),
                      traceText: document.getElementById('traceText'),
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
                          <span>${escapeHtml(step.id)}</span>
                        </button>
                      `).join('');
                      els.stepList.querySelectorAll('[data-step-index]').forEach(button => {
                        button.addEventListener('click', () => showStep(Number(button.dataset.stepIndex)));
                      });
                    }

                    function renderBeans(beans) {
                      els.beanList.innerHTML = beans.map(bean => `
                        <div class="bean-row">
                          <strong>${escapeHtml(bean.name)}</strong>
                          <span>${escapeHtml(bean.className)}</span>
                          <span>${escapeHtml(bean.role)}</span>
                        </div>
                      `).join('');
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

                    async function runGetBean() {
                      const data = await getJson('/api/user');
                      els.userResult.textContent = data.result;
                    }

                    async function init() {
                      try {
                        const [flow, beans, xml, trace] = await Promise.all([
                          getJson('/api/flow'),
                          getJson('/api/beans'),
                          getJson('/api/xml'),
                          getJson('/api/trace')
                        ]);
                        state.steps = flow.steps;
                        renderStepList();
                        renderBeans(beans.beans);
                        els.xmlSource.textContent = xml.xml;
                        els.traceText.textContent = trace.steps.join('\\n');
                        els.status.textContent = '流程已加载，可以逐步查看 IoC 容器创建 Bean 的过程。';
                        showStep(0);
                        await runGetBean();
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
