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
}
