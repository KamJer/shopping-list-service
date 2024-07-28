package pl.kamjer.shoppinglistservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.kamjer.shoppinglistservice.validation.UniqUserNameConstraint;

@Getter
@Builder
public class UserDto {

    @UniqUserNameConstraint
    private String userName;
    private String password;


}
