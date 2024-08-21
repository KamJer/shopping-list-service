package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class AllIdDto {
    private List<Long> amountTypeIds;
    private List<Long> categoriesIds;
    private List<Long> shoppingItemsIds;

    private LocalDateTime savedTime;

}
