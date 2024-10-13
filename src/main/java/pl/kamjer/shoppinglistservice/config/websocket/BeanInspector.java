package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BeanInspector {

    private ObjectMapper objectMapper;
    private ApplicationContext applicationContext;

    public Optional<String> findControllerMethodAndCall(String topic, String[] argumentToCallMethodWith) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Controller.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(MessageMapping.class)) {
                    MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
                    if (messageMapping.value()[0].equals(topic)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] parameters = new Object[argumentToCallMethodWith.length];

                        for (int i = 0; i < parameters.length; i++) {
                            parameters[i] = objectMapper.readValue(argumentToCallMethodWith[i], parameterTypes[i]);
                        }
                        if (parameterTypes.length != argumentToCallMethodWith.length) {
                            throw new IllegalArgumentException("Wrong amount of parameters");
                        }
                        String result = objectMapper.writeValueAsString(method.invoke(bean, parameters));
                        if (result.equals("null")) {
                            return Optional.empty();
                        }
                        return Optional.of(result);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Couldn't find method for passed topic");
    }
}
