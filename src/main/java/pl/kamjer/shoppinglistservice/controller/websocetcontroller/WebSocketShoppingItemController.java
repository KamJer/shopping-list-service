package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketShoppingItemService;

import java.io.IOException;
import java.util.List;

@Controller
@Log
public class WebSocketShoppingItemController extends WebsocketCustomController{

    private final WebSocketShoppingItemService webSocketShoppingItemService;

    public WebSocketShoppingItemController(WebSocketDataHolder webSocketDataHolder, ObjectMapper objectMapper, WebSocketShoppingItemService webSocketShoppingItemService) {
        super(webSocketDataHolder, objectMapper);
        this.webSocketShoppingItemService = webSocketShoppingItemService;
    }

    @MessageMapping("/{userName}/putShoppingItem")
    public ShoppingItemDto  putShoppingItem(@DestinationVariable String userName, ShoppingItemDto shoppingItemDto) throws IOException {
        log.info("/putShoppingItem connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        ShoppingItemDto dto = webSocketShoppingItemService.putShoppingItem(shoppingItemDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of(dto))
                .build());
        return dto;
    }

    @MessageMapping("/{userName}/postShoppingItem")
    public ShoppingItemDto postShoppingItem(@DestinationVariable String userName, ShoppingItemDto shoppingItemDto) throws IOException {
        log.info("/postShoppingItem connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        ShoppingItemDto dto = webSocketShoppingItemService.postShoppingItem(shoppingItemDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of(dto))
                .build());
        return dto;
    }

    @MessageMapping("/{userName}/deleteShoppingItem")
    public ShoppingItemDto deleteShoppingItem(@DestinationVariable String userName, ShoppingItemDto shoppingItemDto) throws IOException {
        log.info("/deleteShoppingItem connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        ShoppingItemDto dto = webSocketShoppingItemService.deleteShoppingItem(shoppingItemDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of())
                .shoppingItemDtoList(List.of(dto))
                .build());
        return dto;
    }
}
