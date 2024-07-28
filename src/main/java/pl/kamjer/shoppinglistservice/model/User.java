package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import pl.kamjer.shoppinglistservice.validation.UniqUserNameConstraint;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "\"USER\"")
public class User implements Serializable {

    @Id
    @Column(name = "user_name")
    private String userName;
    @Column(name = "password")
    private String password;

    public UserDetails convertToSpringUser() {
        return org.springframework.security.core.userdetails.User.builder()
                .username(this.getUserName())
                .password(this.getPassword())
                .roles("ADMIN")
                .build();
    }
}
