package pl.kamjer.shoppinglistservice.config.websocket;

import lombok.AllArgsConstructor;

import javax.security.auth.Subject;
import java.security.Principal;

@AllArgsConstructor
public class UserPrincipal implements Principal {

    private String username;
    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
