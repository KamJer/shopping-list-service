package pl.kamjer.shoppinglistservice.functional_interface;

import pl.kamjer.shoppinglistservice.model.User;

@FunctionalInterface
public interface UserProvider {
    User provideUser(String userName, String password);
}
