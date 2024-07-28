package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "AMOUNT_TYPE")
public class AmountType {

    @EmbeddedId
    private AmountTypeId amountTypeId;

    @Column(name = "type_name")
    private String typeName;
}
