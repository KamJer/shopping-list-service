package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.config.websocket.Message;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Controller
@AllArgsConstructor
@Log
public class WebSocketUtilController {

    private WebSocketService webSocketService;
    private ObjectMapper objectMapper;

    private WebSocketDataHolder webSocketDataHolder;

    @MessageMapping("/synchronizeData")
    public void synchronizeData(AllDto allDto) throws IOException {
        log.info("/synchronizeData connected: User " +  webSocketDataHolder.getCurrentSession().getPrincipal());
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, webSocketDataHolder.getCurrentSession().getId());
        headers.put(Message.Header.DEST, "/synchronizeData");
        headers.put(Message.Header.BODY, objectMapper.writeValueAsString(webSocketService.synchronizeWebSocket(allDto)));
        Message message  = new Message(Message.Command.MESSAGE, headers);
        webSocketDataHolder.getCurrentSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

        String currentUserName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();

        if (newDataBrought(allDto)) {
            HashMap<Message.Header, String> headersForOthers = new HashMap<>();
            headersForOthers.put(Message.Header.ID, webSocketDataHolder.getCurrentSession().getId());
            headersForOthers.put(Message.Header.DEST, "/" + currentUserName + "/pip");
            headersForOthers.put(Message.Header.BODY, "");
            Message messageForOthers  = new Message(Message.Command.MESSAGE, headersForOthers);

            List<WebSocketSession> sessions = webSocketDataHolder.getSessionsForTopic("/synchronizeData");

            for (WebSocketSession session: sessions) {
                if (!session.equals(webSocketDataHolder.getCurrentSession())) {
                    if (!session.isOpen()) {
                        webSocketDataHolder.removeSessionFromTopics(session);
                    } else {
                        log.log(Level.FINE, "sending message: " + session.getId());
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageForOthers)));
                    }
                }
            }
        }
    }

    private boolean newDataBrought(AllDto allDto) {
        List<Boolean> categoryBool = allDto.getCategoryDtoList()
                .stream()
                .map(categoryDto -> categoryDto.getModifyState() == ModifyState.UPDATE || categoryDto.getModifyState() == ModifyState.INSERT || categoryDto.getModifyState() == ModifyState.DELETE)
                .filter(aBoolean -> aBoolean)
                .toList();
        List<Boolean> amountTypeBool = allDto.getAmountTypeDtoList()
                .stream()
                .map(amountTypeDto -> amountTypeDto.getModifyState() == ModifyState.UPDATE || amountTypeDto.getModifyState() == ModifyState.INSERT || amountTypeDto.getModifyState() == ModifyState.DELETE)
                .filter(aBoolean -> aBoolean)
                .toList();
        List<Boolean> shoppingItemBool = allDto.getShoppingItemDtoList()
                .stream()
                .map(shoppingItemDto -> shoppingItemDto.getModifyState() == ModifyState.UPDATE || shoppingItemDto.getModifyState() == ModifyState.INSERT || shoppingItemDto.getModifyState() == ModifyState.DELETE)
                .filter(aBoolean -> aBoolean)
                .toList();
        return !categoryBool.isEmpty() || !amountTypeBool.isEmpty() || !shoppingItemBool.isEmpty();
    }
}
