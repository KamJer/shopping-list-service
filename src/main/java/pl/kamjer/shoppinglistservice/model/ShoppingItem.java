package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "SHOPPING_ITEM")
public class ShoppingItem {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ShoppingItemId shoppingItemId;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "amount_type_id", referencedColumnName = "amount_type_id"),
            @JoinColumn(name = "amount_type_user_name", referencedColumnName = "user_name")
    })
    private AmountType itemAmountType;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "category_id", referencedColumnName = "category_id"),
            @JoinColumn(name = "category_user_name", referencedColumnName = "user_name")
    })
    private Category itemCategory;
    @Column(name = "item_name")
    private String itemName;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "bought", columnDefinition = "BIT(1) NOT NULL DEFAULT b'0'")
    private boolean bought;
    @Column(name = "saved_time")
    private LocalDateTime savedTime;
    @Column(name = "deleted", columnDefinition = "BIT(1) NOT NULL DEFAULT b'0'")
    private boolean deleted;

    @Transient
    private long localAmountTypeId;
    @Transient
    private long localCategoryId;
    @Transient
    private long localShoppingItemId;
}
