package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.model.CategoryId;
import pl.kamjer.shoppinglistservice.model.User;

@Builder
@Getter
public class CategoryDto {

    private long categoryId;

    private String userName;

    private String categoryName;

}
