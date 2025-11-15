package pl.kamjer.shoppinglistservice.controller.websocetcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import pl.kamjer.shoppinglistservice.config.websocket.Message;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.AllDto;
import pl.kamjer.shoppinglistservice.service.websocketservice.WebSocketUtilService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@Controller
@Log
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
//        generating body for a message
        String auth = "";
        List<String> authList = webSocketDataHolder.getCurrentSession().getHandshakeHeaders().get("Authorization");
//        checking if auth header exists and if it exists it's not empty
        if (authList != null && !authList.isEmpty()) {
            auth = authList.getFirst();
        }
        headers.put(Message.Header.BODY, objectMapper.writeValueAsString(webSocketUtilService.synchronizeWebSocket(allDto, auth)));
        Message message = new Message(Message.Command.MESSAGE, headers);
        log.log(Level.INFO, "Sending message to owner: " + webSocketDataHolder.getCurrentSession().getId());
//        sending message to an original sender
        webSocketDataHolder.getCurrentSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));

        notifyClients(allDto);
    }
}
