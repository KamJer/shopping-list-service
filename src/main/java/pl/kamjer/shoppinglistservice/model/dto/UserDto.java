package pl.kamjer.shoppinglistservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kamjer.shoppinglistservice.validation.UniqUserNameConstraint;

@Getter
@Builder
public class UserDto {

    @UniqUserNameConstraint
    @NotEmpty
    private String userName;
    private String password;
}
