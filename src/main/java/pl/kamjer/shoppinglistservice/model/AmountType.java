package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "AMOUNT_TYPE")
public class AmountType {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private AmountTypeId amountTypeId;
    @Column(name = "type_name")
    private String typeName;
    @Column(name = "saved_time")
    private LocalDateTime savedTime;
    @Column(name = "deleted", columnDefinition = "BIT(1) NOT NULL DEFAULT b'0'")
    private boolean deleted;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "itemAmountType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingItem> shoppingItemList;

    @Transient
    private long localId;
}
