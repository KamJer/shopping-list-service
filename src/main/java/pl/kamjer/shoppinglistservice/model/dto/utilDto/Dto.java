package pl.kamjer.shoppinglistservice.model.dto.utilDto;

import pl.kamjer.shoppinglistservice.model.ModifyState;

import java.time.LocalDateTime;

public interface Dto {
    LocalDateTime getSavedTime();
    ModifyState getModifyState();
    boolean isDeleted();

}
