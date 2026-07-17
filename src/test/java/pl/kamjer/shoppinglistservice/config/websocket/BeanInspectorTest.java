package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanInspectorTest {

    @Mock
    private ApplicationContext applicationContext;

    private ObjectMapper objectMapper;
    private BeanInspector beanInspector;

    @Controller
    static class TestController {
        @MessageMapping("/{userName}/putCategory")
        public TestDto putCategory(@DestinationVariable String userName, TestDto dto) {
            TestDto result = new TestDto();
            result.name = userName + "-" + dto.name;
            return result;
        }

        @MessageMapping("/synchronizeData")
        public void voidMethod() {
        }

        @MessageMapping("/{userName}/getByName")
        public TestDto getByName(@DestinationVariable String userName, String name) {
            TestDto result = new TestDto();
            result.name = userName + ":" + name;
            return result;
        }
    }

    static class TestDto {
        public String name;
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        beanInspector = new BeanInspector(objectMapper, applicationContext, new ArrayList<>());
    }

    @Test
    void init_cachesMessageMappingMethods() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));

        beanInspector.init();

        Topic topic = new Topic("/{userName}/putCategory", new String[]{"alice"});
        Optional<String> result = beanInspector.findControllerMethodAndCall(topic, new String[]{"{\"name\":\"test\"}"});
        assertThat(result).isPresent();
    }

    @Test
    void findControllerMethodAndCall_matchingMethod_invokesAndReturns() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));
        beanInspector.init();

        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.BODY, "{\"name\":\"test\"}");
        Topic topic = new Topic("/{userName}/putCategory", new String[]{"alice"});

        Optional<String> result = beanInspector.findControllerMethodAndCall(topic, new String[]{"{\"name\":\"test\"}"});

        assertThat(result).isPresent();
        assertThat(result.get()).contains("alice-test");
    }

    @Test
    void findControllerMethodAndCall_withDestinationVariable_resolvesParam() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));
        beanInspector.init();

        Topic topic = new Topic("/{userName}/getByName", new String[]{"bob"});

        Optional<String> result = beanInspector.findControllerMethodAndCall(topic, new String[]{"\"hello\""});

        assertThat(result).isPresent();
        assertThat(result.get()).contains("bob:hello");
    }

    @Test
    void findControllerMethodAndCall_noMatchingMethod_throws() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));
        beanInspector.init();

        Topic topic = new Topic("/nonexistent", new String[]{});

        assertThatThrownBy(() -> beanInspector.findControllerMethodAndCall(topic, new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Couldn't find method for passed topic");
    }

    @Test
    void findControllerMethodAndCall_voidMethod_returnsEmpty() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));
        beanInspector.init();

        Topic topic = new Topic("/synchronizeData", new String[]{});

        Optional<String> result = beanInspector.findControllerMethodAndCall(topic, new String[]{});

        assertThat(result).isEmpty();
    }

    @Test
    void findControllerMethodAndCall_wrongArgCount_throws() throws Exception {
        TestController controller = new TestController();
        when(applicationContext.getBeansWithAnnotation(Controller.class))
                .thenReturn(Map.of("testController", controller));
        beanInspector.init();

        Topic topic = new Topic("/{userName}/putCategory", new String[]{"alice"});

        assertThatThrownBy(() -> beanInspector.findControllerMethodAndCall(topic, new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wrong amount of parameters");
    }
}
