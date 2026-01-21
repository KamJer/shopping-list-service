package pl.kamjer.shoppinglistservice.config.websocket;


import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketScope implements Scope {

    private Map<String, Object> sessionScopedBeans = new HashMap<>();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return sessionScopedBeans.computeIfAbsent(name, s -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        return sessionScopedBeans.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        AtomicInteger hash = new AtomicInteger();
         sessionScopedBeans.forEach((key, value) -> hash.set(hash.get() * value.hashCode()));
         return hash.toString();
    }
}
