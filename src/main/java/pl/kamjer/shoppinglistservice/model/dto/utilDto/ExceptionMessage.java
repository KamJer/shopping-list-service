package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class ExceptionMessage {
    private String field;
    private String message;

    public String toJson() {
        return "\"" + field + "\":\"" + message + "\"";
    }
}
