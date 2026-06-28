package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class BeanInspector {

    private ObjectMapper objectMapper;
    private ApplicationContext applicationContext;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<MethodEntry> methodCache;

    @PostConstruct
    void init() {
        methodCache = new ArrayList<>();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Controller.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(MessageMapping.class)) {
                    MessageMapping mapping = method.getAnnotation(MessageMapping.class);
                    for (String pattern : mapping.value()) {
                        methodCache.add(new MethodEntry(pattern, bean, method, method.getParameters()));
                    }
                }
            }
        }
    }

    public Optional<String> findControllerMethodAndCall(Topic topic, String[] argumentToCallMethodWith)
            throws InvocationTargetException, IllegalAccessException, JsonProcessingException {

        Map<String, String> pathVariables = topic.getUriMap();
        Class<?>[] parameterTypes;
        Parameter[] parameters;
        Method method;
        Object bean;

        for (MethodEntry entry : methodCache) {
            if (!pathMatcher.match(entry.pattern, topic.topicUrl())) {
                continue;
            }
            method = entry.method;
            bean = entry.bean;
            parameterTypes = method.getParameterTypes();
            parameters = entry.parameters;
            Object[] args = new Object[parameterTypes.length];

            int argIndex = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameters[i].isAnnotationPresent(DestinationVariable.class)) {
                    DestinationVariable destVar = parameters[i].getAnnotation(DestinationVariable.class);
                    String varName = destVar.value();
                    if (varName.isEmpty()) {
                        varName = parameters[i].getName();
                    }
                    args[i] = objectMapper.convertValue(pathVariables.get(varName), parameterTypes[i]);
                } else {
                    if (argIndex >= argumentToCallMethodWith.length) {
                        throw new IllegalArgumentException("Wrong amount of parameters");
                    }
                    args[i] = objectMapper.readValue(argumentToCallMethodWith[argIndex], parameterTypes[i]);
                    argIndex++;
                }
            }

            Object result = method.invoke(bean, args);
            if (result == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.writeValueAsString(result));
        }

        throw new IllegalArgumentException("Couldn't find method for passed topic");
    }

    private record MethodEntry(String pattern, Object bean, Method method, Parameter[] parameters) {}
}
