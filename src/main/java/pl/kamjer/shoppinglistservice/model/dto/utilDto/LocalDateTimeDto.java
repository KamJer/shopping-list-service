package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class LocalDateTimeDto extends Dto{

    private LocalDateTime savedTime;
}
