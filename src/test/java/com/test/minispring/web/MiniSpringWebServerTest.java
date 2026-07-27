package com.test.minispring.web;

import com.sun.net.httpserver.HttpServer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniSpringWebServerTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createsServerOnLoopbackOnly() throws Exception {
        server = MiniSpringWebServer.createServer(0);

        assertTrue(server.getAddress().getAddress().isLoopbackAddress());
    }

    @Test
    void rejectsUnsupportedMethodsOnApiEndpoints() throws Exception {
        server = MiniSpringWebServer.createServer(0);
        server.start();
        int port = server.getAddress().getPort();
        HttpURLConnection connection = (HttpURLConnection) new URL(
                "http", InetAddress.getLoopbackAddress().getHostAddress(), port, "/api/health")
                .openConnection();
        connection.setRequestMethod("POST");

        assertEquals(405, connection.getResponseCode());
        assertEquals("application/json; charset=utf-8", connection.getHeaderField("Content-Type"));
    }

    @Test
    void exposesSecurityBoundaryAndRendersItInTheVisualizer() throws Exception {
        server = MiniSpringWebServer.createServer(0);
        server.start();
        int port = server.getAddress().getPort();

        HttpURLConnection security = open(port, "/api/security");
        assertEquals(200, security.getResponseCode());
        String securityBody = new String(security.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(securityBody.contains("\"localOnly\":true"));
        assertTrue(securityBody.contains("\"doctypeAllowed\":false"));
        assertTrue(securityBody.contains("\"externalEntitiesAllowed\":false"));
        assertTrue(securityBody.contains("\"configSource\":\"trusted-classpath\""));

        HttpURLConnection index = open(port, "/");
        assertEquals(200, index.getResponseCode());
        String html = new String(index.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(html.contains("id=\"guardrailGrid\""));
        assertTrue(html.contains("getJson('/api/security')"));
        assertTrue(html.contains("id=\"learningOutcome\""));
        assertTrue(html.contains("renderCurrentState"));
        assertTrue(html.contains("state.steps.slice(0, state.current + 1)"));
        assertTrue(html.contains(".join('\\n');"));
        assertTrue(html.contains("技术详情"));
        assertTrue(html.contains("id=\"glossary\""));
        assertTrue(html.contains("Bean：由容器创建和管理的 Java 对象"));
        assertTrue(html.contains("AOP 代理：在不改业务代码的前提下附加通用功能"));
        assertTrue(html.contains("依赖注入：容器自动把一个对象需要的另一个对象连接进去"));
        assertTrue(html.contains("找到配置文件"));
        assertTrue(html.contains("按说明书创建对象"));
        assertTrue(html.contains("这是容器自动创建并管理的 Java 对象"));
        assertTrue(html.contains("已由 AOP 增强"));
        assertTrue(html.contains("每段配置是什么意思"));
        assertTrue(html.contains("id 是对象在容器里的名字"));
        assertTrue(html.contains("ref 表示引用另一个 Bean"));
        assertTrue(!html.contains("<strong>XmlBeanDefinitionReader</strong>"));
        assertTrue(!html.contains("<strong>BeanDefinitionRegistry</strong>"));
    }

    @Test
    void rejectsPortsOutsideUserFacingRange() {
        assertThrows(IllegalArgumentException.class,
                () -> MiniSpringWebServer.resolvePort(new String[] {"0"}, null));
        assertThrows(IllegalArgumentException.class,
                () -> MiniSpringWebServer.resolvePort(new String[] {"65536"}, null));
        assertThrows(IllegalArgumentException.class,
                () -> MiniSpringWebServer.resolvePort(new String[] {"not-a-port"}, null));
    }

    private static HttpURLConnection open(int port, String path) throws Exception {
        return (HttpURLConnection) new URL(
                "http", InetAddress.getLoopbackAddress().getHostAddress(), port, path)
                .openConnection();
    }
}
