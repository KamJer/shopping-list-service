package pl.kamjer.shoppinglistservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.dto.utilDto.Dto;

@Builder
@Getter
public class CategoryDto extends Dto {

    private long categoryId;
    private String categoryName;
    private boolean deleted;
    private ModifyState modifyState;

    private long localId;
}
