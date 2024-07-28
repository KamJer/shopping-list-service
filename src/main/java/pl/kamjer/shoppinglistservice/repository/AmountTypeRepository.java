package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.AmountTypeId;
import pl.kamjer.shoppinglistservice.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmountTypeRepository extends JpaRepository<AmountType, Long> {
    List<AmountType> findAmountTypeByAmountTypeIdUserUserName(String userName);
    Optional<AmountType> findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(String userName, long amountTypeId_amountTypeId);
}
