package pl.kamjer.shoppinglistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Embeddable
public class AmountTypeId implements Serializable {

    @ManyToOne
    @MapsId("user_name")
    @JoinColumn(name = "user_name", referencedColumnName = "user_name")
    private User user;
    @Column(name = "amount_type_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long amountTypeId;


}
