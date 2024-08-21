package pl.kamjer.shoppinglistservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.ModifyState;

@Builder
@Getter
public class CategoryDto {

    private long categoryId;
    private long localCategoryId;
    private String categoryName;
    private boolean deleted;
    private ModifyState modifyState;

}
