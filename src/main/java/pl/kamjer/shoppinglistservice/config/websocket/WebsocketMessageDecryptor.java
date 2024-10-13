package pl.kamjer.shoppinglistservice.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WebsocketMessageDecryptor {

    private ObjectMapper objectMapper;

    public Message decipher(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, Message.class);
    }

    public String jsonphyMessage(Message message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);

    }

}
