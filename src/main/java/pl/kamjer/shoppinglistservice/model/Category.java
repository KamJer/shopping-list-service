package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "CATEGORY")
public class Category {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private CategoryId categoryId;
    private String categoryName;
    @Column(name = "saved_time")
    private LocalDateTime savedTime;
    @Column(name = "deleted")
    private boolean deleted;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "itemCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingItem> shoppingItemList;

    @Transient
    private long localCategoryId;
}
