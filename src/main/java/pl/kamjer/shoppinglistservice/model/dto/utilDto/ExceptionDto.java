package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExceptionDto {
    private String message;
    @NotNull
    private StackTraceElement[] stackTrace;
}
