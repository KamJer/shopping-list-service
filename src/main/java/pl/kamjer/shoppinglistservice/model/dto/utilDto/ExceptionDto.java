package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExceptionDto {
    private String message;
    private StackTraceElement[] stackTrace;
}
