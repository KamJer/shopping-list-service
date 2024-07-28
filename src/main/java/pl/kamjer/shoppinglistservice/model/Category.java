package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "CATEGORY")
public class Category {

    @EmbeddedId
    private CategoryId categoryId;

    @Column(name = "category_name")
    private String categoryName;
}
