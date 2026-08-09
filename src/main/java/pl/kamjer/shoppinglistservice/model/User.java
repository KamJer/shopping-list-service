package pl.kamjer.shoppinglistservice.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Serializable {

    @EqualsAndHashCode.Include
    private String userName;
    private String password;
    private LocalDateTime savedTime;
}
