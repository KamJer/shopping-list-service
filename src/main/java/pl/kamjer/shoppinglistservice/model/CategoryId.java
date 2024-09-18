package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
@Embeddable
public class CategoryId implements Serializable {

    @ManyToOne
    @MapsId("user_name")
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private User user;
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long categoryId;
}
