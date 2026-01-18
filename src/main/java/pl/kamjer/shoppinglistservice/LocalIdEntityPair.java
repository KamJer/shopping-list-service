package pl.kamjer.shoppinglistservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class LocalIdEntityPair {
    private Long localId;
    private AmountType entity;

    public void insertToDb(AmountTypeRepository repository, AmountType amountTypeToInsert) {
        setEntity(repository.save(amountTypeToInsert));
    }
}
