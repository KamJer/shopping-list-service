package pl.kamjer.shoppinglistservice.model;

import java.time.LocalDateTime;

public interface ShoppingEntity {
    LocalDateTime getSavedTime();
    boolean isDeleted();
}
