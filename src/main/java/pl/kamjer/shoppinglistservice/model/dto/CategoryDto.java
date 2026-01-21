package pl.kamjer.shoppinglistservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;

import java.time.LocalDateTime;

@Builder
@Getter
public class CategoryDto {

    private long categoryId;
    private String categoryName;
    private boolean deleted;
    private ModifyState modifyState;

    private long localId;

    private LocalDateTime savedTime;
}
