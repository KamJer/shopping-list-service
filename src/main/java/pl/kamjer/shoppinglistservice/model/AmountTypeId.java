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
@ToString
@Embeddable
public class AmountTypeId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "user_name", referencedColumnName = "user_name", nullable = false)
    private User user;
    @Column(name = "amount_type_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long amountTypeId;
}
