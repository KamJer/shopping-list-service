package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketAmountTypeService;

import java.io.IOException;
import java.util.List;

@Controller
@Log
public class WebSocketAmountTypeController extends WebsocketCustomController {

    private final WebSocketAmountTypeService webSocketAmountTypeService;

    public WebSocketAmountTypeController(WebSocketAmountTypeService webSocketAmountTypeService, WebSocketDataHolder webSocketDataHolder, ObjectMapper objectMapper) {
        super(webSocketDataHolder, objectMapper);
        this.webSocketAmountTypeService = webSocketAmountTypeService;
    }

    @MessageMapping("/{userName}/putAmountType")
    public AmountTypeDto putAmountType(@DestinationVariable String userName, AmountTypeDto amountTypeDto) throws IOException {
        log.info("/putAmountType connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        AmountTypeDto amountTypeDtoProcessed = webSocketAmountTypeService.putAmountType(amountTypeDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of(amountTypeDtoProcessed))
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of())
                .build());
        return amountTypeDtoProcessed;
    }

    @MessageMapping("/{userName}/postAmountType")
    public AmountTypeDto postAmountType(@DestinationVariable String userName, AmountTypeDto amountTypeDto) throws IOException {
        log.info("/postAmountType connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        AmountTypeDto amountTypeDtoProcessed = webSocketAmountTypeService.postAmountType(amountTypeDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of(amountTypeDtoProcessed))
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of())
                .build());
        return amountTypeDtoProcessed;
    }

    @MessageMapping("/{userName}/deleteAmountType")
    public AmountTypeDto deleteAmountType(@DestinationVariable String userName, AmountTypeDto amountTypeDto) throws IOException {
        log.info("/deleteAmountType connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        AmountTypeDto amountTypeDtoProcessed = webSocketAmountTypeService.deleteAmountType(amountTypeDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of(amountTypeDtoProcessed))
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of())
                .build());
        return amountTypeDtoProcessed;
    }
}
