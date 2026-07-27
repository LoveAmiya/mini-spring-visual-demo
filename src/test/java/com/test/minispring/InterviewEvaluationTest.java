package com.test.minispring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.test.minispring.web.MiniSpringDemoService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** 24 black-box checks over the demo's IoC-visible contract. */
class InterviewEvaluationTest {
    static Stream<String> expectedSignals() {
        return Stream.of(
            "userDao", "userService", "userController",
            "数据访问对象", "业务服务", "调用入口",
            "load-xml", "parse-definitions", "register-definitions", "create-dao",
            "create-service", "inject-dao", "create-controller", "inject-service",
            "store-singletons", "get-bean", "classpath:spring.xml",
            "BeanFactory", "XmlBeanDefinitionReader", "userDao as a bean reference",
            "userService as a bean reference", "wired controller instance",
            "queryUserInfo()", "dependency chain"
        );
    }

    @ParameterizedTest(name = "IoC contract signal: {0}")
    @MethodSource("expectedSignals")
    void exposesEachInterviewContractSignal(String signal) {
        MiniSpringDemoService service = new MiniSpringDemoService();
        List<MiniSpringDemoService.BeanView> beans = service.listBeans();
        String visible = service.readXmlConfig() + "\n" + String.join("\n", service.traceSteps()) + "\n" + service.visualFlow();
        boolean present = beans.stream().anyMatch(bean -> (bean.name() + " " + bean.role()).contains(signal)) || visible.contains(signal);
        assertTrue(present, signal);
        assertEquals("user=Richard, company=Legend Co., Ltd.", service.queryUserInfo());
    }

    @org.junit.jupiter.api.Test
    void exposesPlainLanguageBeanNamesBeforeProxyDetails() {
        MiniSpringDemoService service = new MiniSpringDemoService();
        List<MiniSpringDemoService.BeanView> beans = service.listBeans();

        assertTrue(beans.stream().allMatch(bean -> !bean.displayClassName().contains("$$EnhancerByCGLIB")));
        assertTrue(beans.stream().allMatch(MiniSpringDemoService.BeanView::proxied));
        assertTrue(beans.stream().allMatch(bean -> bean.role().matches(".*[\\u4e00-\\u9fa5].*")));
    }

    @org.junit.jupiter.api.Test
    void explainsTheContainerFlowWithoutRequiringFrameworkVocabulary() {
        MiniSpringDemoService service = new MiniSpringDemoService();
        List<MiniSpringDemoService.FlowStep> steps = service.visualFlow();

        assertEquals("找到配置文件", steps.get(0).title());
        assertEquals("读懂三条对象配置", steps.get(1).title());
        assertEquals("按说明书创建 userDao", steps.get(3).title());
        assertTrue(steps.stream().allMatch(step -> step.description().contains("容器")
                || step.description().contains("程序")));
    }
}
