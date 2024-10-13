package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class AllDto extends Dto{
    private List<AmountTypeDto> amountTypeDtoList;
    private List<CategoryDto> categoryDtoList;
    private List<ShoppingItemDto> shoppingItemDtoList;
    private LocalDateTime savedTime;

}
