package pl.kamjer.shoppinglistservice.config.websocket.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketHandler;

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
