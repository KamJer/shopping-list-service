package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class BeanInspector {

    // Used for serializing/deserializing method parameters and return values
    private ObjectMapper objectMapper;

    // Application context is needed to find all beans annotated with @Controller
    private ApplicationContext applicationContext;

    /**
     * Finds a controller method mapped to a specific topic (via @MessageMapping)
     * and invokes it with deserialized arguments.
     *
     * @param topic The topic the method is mapped to (e.g., "/send")
     * @param argumentToCallMethodWith JSON strings of method parameters
     * @return Optional with method's return value serialized as JSON, or empty if the result is null
     */
//    public Optional<String> findControllerMethodAndCall(String topic, String[] argumentToCallMethodWith)
//            throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
//
//        // Get all beans in the context that are annotated with @Controller
//        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Controller.class);
//
//        for (Map.Entry<String, Object> entry : beans.entrySet()) {
//            Object bean = entry.getValue();
//            Class<?> beanClass = bean.getClass();
//
//            // Inspect all declared methods of the controller class
//            for (Method method : beanClass.getDeclaredMethods()) {
//
//                // Check if the method is annotated with @MessageMapping
//                if (method.isAnnotationPresent(MessageMapping.class)) {
//                    MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
//
//                    // Check if the method maps to the given topic
//                    if (messageMapping.value()[0].equals(topic)) {
//
//                        // Prepare parameters for method invocation
//                        Class<?>[] parameterTypes = method.getParameterTypes();
//                        Object[] parameters = new Object[argumentToCallMethodWith.length];
//
//                        // Deserialize each JSON argument to its expected type
//                        for (int i = 0; i < parameters.length; i++) {
//                            parameters[i] = objectMapper.readValue(argumentToCallMethodWith[i], parameterTypes[i]);
//                        }
//
//                        // Validate argument count
//                        if (parameterTypes.length != argumentToCallMethodWith.length) {
//                            throw new IllegalArgumentException("Wrong amount of parameters");
//                        }
//
//                        // Invoke the method with the deserialized parameters
//                        String result = objectMapper.writeValueAsString(method.invoke(bean, parameters));
//
//                        // Return the result if it's not null
//                        if (result.equals("null")) {
//                            return Optional.empty();
//                        }
//                        return Optional.of(result);
//                    }
//                }
//            }
//        }
//
//        // No matching method found for the topic
//        throw new IllegalArgumentException("Couldn't find method for passed topic");
//    }

    public Optional<String> findControllerMethodAndCall(Topic topic, String[] argumentToCallMethodWith)
            throws InvocationTargetException, IllegalAccessException, JsonProcessingException {

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Controller.class);
        AntPathMatcher pathMatcher = new AntPathMatcher();

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(MessageMapping.class)) {
                    MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
                    String[] mappingValues = messageMapping.value();
                    Map<String, String> pathVariables = topic.getUriMap();

                    for (String mapping : mappingValues) {
                        if (pathMatcher.match(mapping, topic.topicUrl())) {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            Object[] parameters = new Object[parameterTypes.length];

                            int argIndex = 0;
                            for (int i = 0; i < parameterTypes.length; i++) {
                                if (method.getParameters()[i].isAnnotationPresent(DestinationVariable.class)) {
                                    DestinationVariable destVar = method.getParameters()[i].getAnnotation(DestinationVariable.class);
                                    String varName = destVar.value();
                                    if (varName.isEmpty()) {
                                        varName = method.getParameters()[i].getName();
                                    }
                                    String value = pathVariables.get(varName);
                                    parameters[i] = objectMapper.convertValue(value, parameterTypes[i]);
                                } else {
                                    if (argIndex >= argumentToCallMethodWith.length) {
                                        throw new IllegalArgumentException("Wrong amount of parameters");
                                    }
                                    parameters[i] = objectMapper.readValue(argumentToCallMethodWith[argIndex], parameterTypes[i]);
                                    argIndex++;
                                }
                            }

                            Object result = method.invoke(bean, parameters);
                            if (result == null) {
                                return Optional.empty();
                            }
                            return Optional.of(objectMapper.writeValueAsString(result));
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("Couldn't find method for passed topic");
    }

}
