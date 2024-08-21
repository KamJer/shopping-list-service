package pl.kamjer.shoppinglistservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.AmountTypeId;
import pl.kamjer.shoppinglistservice.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmountTypeRepository extends JpaRepository<AmountType, Long> {
    List<AmountType> findAllAmountTypeByAmountTypeIdUserUserName(String userName);
    Optional<AmountType> findAmountTypeByAmountTypeIdUserUserNameAndAmountTypeIdAmountTypeId(String userName, long amountTypeId_amountTypeId);
    List<AmountType> findAmountTypeByAmountTypeIdUserUserNameAndSavedTimeAfter(String userNAme, LocalDateTime localDateTime);
    List<AmountType> findAmountTypeByAmountTypeIdUserUserNameAndSavedTimeAfterAndDeletedIsFalse(String userNAme, LocalDateTime localDateTime);
    @Transactional
    void deleteByAmountTypeIdAmountTypeIdAndAmountTypeIdUserUserName(Long amountTypeId, String userName);
}
