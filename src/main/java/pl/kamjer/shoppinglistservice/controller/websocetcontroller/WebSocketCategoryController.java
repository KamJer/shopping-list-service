package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketCategoryService;

import java.io.IOException;
import java.util.List;

@Controller
@Log
public class WebSocketCategoryController extends WebsocketCustomController{

    private final WebSocketCategoryService webSocketCategoryService;

    public WebSocketCategoryController(WebSocketDataHolder webSocketDataHolder, ObjectMapper objectMapper, WebSocketCategoryService webSocketCategoryService) {
        super(webSocketDataHolder, objectMapper);
        this.webSocketCategoryService = webSocketCategoryService;
    }

    @MessageMapping("/{userName}/putCategory")
    public CategoryDto putCategory(@DestinationVariable String userName, CategoryDto categoryDto) throws IOException {
        log.info("/putCategory connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        CategoryDto categoryDtoToSend = webSocketCategoryService.putCategory(categoryDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of(categoryDtoToSend))
                .shoppingItemDtoList(List.of())
                .build());
        return categoryDtoToSend;
    }

    @MessageMapping("/{userName}/postCategory")
    public CategoryDto postCategory(@DestinationVariable String userName, CategoryDto categoryDto) throws IOException {
        log.info("/postCategory connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        CategoryDto categoryDtoProcessed = webSocketCategoryService.postCategory(categoryDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of(categoryDtoProcessed))
                .shoppingItemDtoList(List.of())
                .build());
        return categoryDtoProcessed;
    }

    @MessageMapping("/{userName}/deleteCategory")
    public CategoryDto deleteCategory(@DestinationVariable String userName, CategoryDto categoryDto) throws IOException {
        log.info("/deleteCategory connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
        CategoryDto categoryDtoProcessed = webSocketCategoryService.deleteCategory(categoryDto);
        notifyClients(AllDto.builder()
                .amountTypeDtoList(List.of())
                .categoryDtoList(List.of(categoryDtoProcessed))
                .shoppingItemDtoList(List.of())
                .build());
        return categoryDtoProcessed;
    }
}
