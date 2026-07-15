package pl.kamjer.shoppinglistservice.model;

import java.time.LocalDateTime;

public interface Copyable<E extends ShoppingEntity> {
    void copyFrom(E source, LocalDateTime savedTime);
}
