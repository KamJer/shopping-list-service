package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "\"USER\"")
public class User implements Serializable {

    @Id
    @Column(name = "user_name")
    @EqualsAndHashCode.Include
    private String userName;
    @Column(name = "password")
    private String password;
    @Column(name = "saved_time")
    private LocalDateTime savedTime;

    public UserDetails convertToSpringUser() {
        return org.springframework.security.core.userdetails.User.builder()
                .username(this.getUserName())
                .password(this.getPassword())
                .roles("ADMIN")
                .build();
    }
}
