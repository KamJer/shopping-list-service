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
@Table(name = "CATEGORY")
public class Category {

    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private Long categoryId;
    @ManyToOne()
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private User user;
    private String categoryName;
    @Column(name = "saved_time")
    private LocalDateTime savedTime;
    @Column(name = "deleted", columnDefinition = "BIT(1) NOT NULL DEFAULT b'0'")
    private boolean deleted;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "itemCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingItem> shoppingItemList;

    @Transient
    private long localId;
}