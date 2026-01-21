package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pl.kamjer.shoppinglistservice.config.websocket.Message;
import pl.kamjer.shoppinglistservice.config.websocket.Topic;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Controller
@AllArgsConstructor
@Log
public class WebsocketCustomController {

    WebSocketDataHolder webSocketDataHolder;
    ObjectMapper objectMapper;

    void notifyClients(AllDto allDto) throws IOException {
        String currentUserName = Optional.ofNullable(webSocketDataHolder.getCurrentSession().getPrincipal()).orElseThrow().getName();

        if (newDataBrought(allDto)) {
            HashMap<Message.Header, String> headersForOthers = new HashMap<>();
            headersForOthers.put(Message.Header.ID, webSocketDataHolder.getCurrentSession().getId());
            headersForOthers.put(Message.Header.DEST, "/{userName}/pip");
            headersForOthers.put(Message.Header.BODY, "");
            headersForOthers.put(Message.Header.PARA, currentUserName);
            Message messageForOthers  = new Message(Message.Command.MESSAGE, headersForOthers);

            HashMap<String, WebSocketSession> sessions = webSocketDataHolder.getSessionsForTopic(new Topic("/synchronizeData", new String[]{}));

            for (WebSocketSession session: sessions.values()) {
                if (!session.getId().equals(webSocketDataHolder.getCurrentSession().getId())) {
                    if (!session.isOpen()) {
                        webSocketDataHolder.removeSessionFromTopics(session);
                    } else {
                        log.log(Level.INFO, "Sending message to: " + session.getId());
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageForOthers)));
                    }
                }
            }
        }
    }

    boolean newDataBrought(AllDto allDto) {
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
