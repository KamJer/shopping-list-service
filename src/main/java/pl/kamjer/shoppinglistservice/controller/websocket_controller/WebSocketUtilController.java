package pl.kamjer.shoppinglistservice.controller.websocket_controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import pl.kamjer.shoppinglistservice.config.websocket.Message;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketUtilService;

import java.io.IOException;
import java.util.HashMap;


@Controller
@Log4j2
public class WebSocketUtilController extends WebsocketCustomController {

    private final WebSocketUtilService webSocketUtilService;

    public WebSocketUtilController(WebSocketUtilService webSocketUtilService, WebSocketDataHolder webSocketDataHolder, ObjectMapper objectMapper) {
        super(webSocketDataHolder, objectMapper);
        this.webSocketUtilService = webSocketUtilService;
    }

    @MessageMapping("/synchronizeData")
    public void synchronizeData(AllDto allDto) throws IOException {
        log.info("/synchronizeData connected: User " + webSocketDataHolder.getCurrentSession().getPrincipal());
//        generating message for clients
        HashMap<Message.Header, String> headers = new HashMap<>();
        headers.put(Message.Header.ID, webSocketDataHolder.getCurrentSession().getId());
        headers.put(Message.Header.DEST, "/synchronizeData");
        headers.put(Message.Header.BODY, objectMapper.writeValueAsString(webSocketUtilService.synchronizeWebSocket(allDto)));
        Message message = new Message(Message.Command.MESSAGE, headers);
        log.info("Sending message to owner: " + webSocketDataHolder.getCurrentSession().getId());
//        sending message to an original sender
        webSocketDataHolder.getCurrentSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

        notifyClients(allDto);
    }
}
