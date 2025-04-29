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
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private User user;
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long categoryId;


//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
//    private User user;
//    @Column(name = "shopping_item_id")
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long shoppingItemId;
}
