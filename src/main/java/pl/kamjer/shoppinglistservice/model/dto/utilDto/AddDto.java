package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class AddDto extends Dto{
    private Long newId;
    private LocalDateTime savedTime;
}
