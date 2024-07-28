package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@Table(name = "SHOPPING_ITEM")
public class ShoppingItem {

    @EmbeddedId
    private ShoppingItemId shoppingItemId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "amount_type_id", referencedColumnName = "amount_type_id"),
            @JoinColumn(name = "amount_type_user_name", referencedColumnName = "user_name")
    })
    private AmountType itemAmountType;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "category_id", referencedColumnName = "category_id"),
            @JoinColumn(name = "category_user_name", referencedColumnName = "user_name")
    })
    private Category itemCategory;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "bought")
    private boolean bought;

    @Column(name = "moved_to_bought")
    private boolean movedToBought;
}
