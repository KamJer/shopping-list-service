package pl.kamjer.shoppinglistservice.functional_interface;

import pl.kamjer.shoppinglistservice.model.User;

@FunctionalInterface
public interface HandleAuthAction {

    boolean action(User user);
}
