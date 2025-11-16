package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

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
    @Column(name = "password")
    private String password;
    @Version
    @Column(name = "saved_time")
    private LocalDateTime savedTime;

    public UserDetails convertToSpringUser() {
        return org.springframework.security.core.userdetails.User.builder()
                .username(this.getUserName())
                .password(this.getPassword())
                .roles("USER")
                .build();
    }
}
