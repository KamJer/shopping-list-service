package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class SubscribeMessage {

    private String topic;

    private String[] parameters;

    public boolean isMessageParameterized() {
        return parameters.length > 0;
    }
}
