package com.test.minispring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.test.minispring.web.MiniSpringDemoService;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void loadsUserControllerThroughMiniSpringContext() {
        MiniSpringDemoService service = new MiniSpringDemoService();

        String userInfo = service.queryUserInfo();

        assertEquals("user=Richard, company=Legend Co., Ltd.", userInfo);
    }

    @Test
    public void listsBeanDefinitionsForVisualConsole() {
        MiniSpringDemoService service = new MiniSpringDemoService();

        assertEquals(3, service.listBeans().size());
        assertTrue(service.listBeans().stream().anyMatch(bean -> bean.name().equals("userController")));
    }

    @Test
    public void exposesXmlAndTraceForBrowserDemo() {
        MiniSpringDemoService service = new MiniSpringDemoService();

        assertTrue(service.readXmlConfig().contains("userController"));
        assertFalse(service.traceSteps().isEmpty());
    }

    @Test
    public void exposesStepByStepIocFlowForVisualConsole() {
        MiniSpringDemoService service = new MiniSpringDemoService();

        assertEquals(10, service.visualFlow().size());
        assertEquals("load-xml", service.visualFlow().get(0).id());
        assertTrue(service.visualFlow().stream()
                .anyMatch(step -> step.title().contains("注入 userDao")
                        && step.activeEdges().contains("dao-service")));
        assertTrue(service.visualFlow().get(9).activeNodes().contains("client"));
    }
}
