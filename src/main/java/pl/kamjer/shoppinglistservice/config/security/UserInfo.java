package pl.kamjer.shoppinglistservice.config.security;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserInfo {

    private String userName;
    private String role;
}
