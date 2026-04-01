package pl.kamjer.shoppinglistservice.config.websocket.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketHandler;
import pl.kamjer.shoppinglistservice.model.User;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private WebSocketHandler webSocketHandler;
    private SecClient secClient;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        webSocketHandler.registerTopic(
                "/synchronizeData",
                "/{userName}/pip",
                "/{userName}/putAmountType",
                "/{userName}/postAmountType",
                "/{userName}/deleteAmountType",
                "/{userName}/putCategory",
                "/{userName}/postCategory",
                "/{userName}/deleteCategory",
                "/{userName}/putShoppingItem",
                "/{userName}/postShoppingItem",
                "/{userName}/deleteShoppingItem"
        );
        registry.addHandler(webSocketHandler, "/ws").setAllowedOriginPatterns("*");
    }
}
