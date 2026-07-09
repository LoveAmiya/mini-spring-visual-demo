package com.test.minispring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.test.minispring.web.MiniSpringDemoService.BeanView;

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
        server.setExecutor(null);
        server.start();
        System.out.println("Mini-Spring Visual Console started at http://127.0.0.1:" + port);
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
        sendJson(exchange, 200, "{\"ok\":true,\"service\":\"mini-spring-visual-console\"}");
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
        json.append("{\"steps\":[");
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(steps.get(i))).append("\"");
        }
        json.append("]}");
        sendJson(exchange, 200, json.toString());
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
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Mini-Spring Visual Console</title>
                  <style>
                    body { margin: 0; font-family: Arial, sans-serif; background: #f6f7fb; color: #172033; }
                    header { background: #172033; color: white; padding: 24px 32px; }
                    main { max-width: 1120px; margin: 0 auto; padding: 24px; }
                    h1 { margin: 0 0 6px; font-size: 28px; }
                    p { line-height: 1.55; }
                    button { border: 0; background: #2563eb; color: white; padding: 10px 14px; border-radius: 6px; cursor: pointer; }
                    button.secondary { background: #4b5563; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
                    .panel { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; box-shadow: 0 8px 20px rgba(15, 23, 42, .05); }
                    .panel h2 { font-size: 17px; margin: 0 0 12px; }
                    pre { overflow: auto; background: #0f172a; color: #e5e7eb; padding: 14px; border-radius: 6px; min-height: 92px; }
                    table { width: 100%; border-collapse: collapse; font-size: 14px; }
                    th, td { text-align: left; padding: 8px; border-bottom: 1px solid #e5e7eb; vertical-align: top; }
                    .status { color: #2563eb; font-weight: 700; }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>Mini-Spring Visual Console</h1>
                    <p>Browser demo for XML loading, BeanFactory registration, dependency injection, and getBean.</p>
                  </header>
                  <main>
                    <p class="status" id="status">Ready.</p>
                    <p>
                      <button onclick="loadAll()">Refresh Demo</button>
                      <button class="secondary" onclick="loadUser()">Run getBean Demo</button>
                    </p>
                    <div class="grid">
                      <section class="panel">
                        <h2>getBean Result</h2>
                        <pre id="userResult">Click "Run getBean Demo".</pre>
                      </section>
                      <section class="panel">
                        <h2>Bean Definitions</h2>
                        <div id="beans">Not loaded.</div>
                      </section>
                      <section class="panel">
                        <h2>Execution Trace</h2>
                        <pre id="trace">Not loaded.</pre>
                      </section>
                      <section class="panel">
                        <h2>spring.xml</h2>
                        <pre id="xml">Not loaded.</pre>
                      </section>
                    </div>
                  </main>
                  <script>
                    async function getJson(url) {
                      const response = await fetch(url);
                      const data = await response.json();
                      if (!response.ok) throw new Error(data.error?.message || response.statusText);
                      return data;
                    }
                    async function loadUser() {
                      document.getElementById('status').textContent = 'Running getBean demo...';
                      const data = await getJson('/api/user');
                      document.getElementById('userResult').textContent = data.result;
                      document.getElementById('status').textContent = 'getBean demo completed.';
                    }
                    async function loadBeans() {
                      const data = await getJson('/api/beans');
                      document.getElementById('beans').innerHTML = '<table><thead><tr><th>Name</th><th>Class</th><th>Role</th></tr></thead><tbody>' +
                        data.beans.map(bean => `<tr><td>${bean.name}</td><td>${bean.className}</td><td>${bean.role}</td></tr>`).join('') +
                        '</tbody></table>';
                    }
                    async function loadXml() {
                      const data = await getJson('/api/xml');
                      document.getElementById('xml').textContent = data.xml;
                    }
                    async function loadTrace() {
                      const data = await getJson('/api/trace');
                      document.getElementById('trace').textContent = data.steps.join('\\n');
                    }
                    async function loadAll() {
                      try {
                        document.getElementById('status').textContent = 'Loading Mini-Spring context...';
                        await Promise.all([loadUser(), loadBeans(), loadXml(), loadTrace()]);
                        document.getElementById('status').textContent = 'Mini-Spring demo loaded.';
                      } catch (error) {
                        document.getElementById('status').textContent = 'Error: ' + error.message;
                      }
                    }
                    loadAll();
                  </script>
                </body>
                </html>
                """;
    }
}
