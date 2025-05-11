package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.AmountType;

@Controller
@AllArgsConstructor
@Log
public class WebSocketAmountTypeController {

    private WebSocketDataHolder webSocketDataHolder;

    @MessageMapping("/amountTypePut")
    public AmountType putAmountType(AmountType amountType) {
        log.info("/amountTypePut connected: User " +  webSocketDataHolder.getCurrentSession().getPrincipal());

        return null;
    }
}
