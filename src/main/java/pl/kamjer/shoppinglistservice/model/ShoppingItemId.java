package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Embeddable
public class ShoppingItemId implements Serializable {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private User user;
    @Column(name = "shopping_item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long shoppingItemId;
}
