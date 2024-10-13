package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.SessionScope;
import org.springframework.web.socket.config.annotation.*;

import java.util.HashMap;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        webSocketHandler.registerTopic("/synchronizeData", "/{username}/pip");
        registry.addHandler(webSocketHandler, "/ws");
    }
}
