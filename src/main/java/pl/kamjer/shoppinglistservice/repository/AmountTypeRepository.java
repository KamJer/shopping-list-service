package pl.kamjer.shoppinglistservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmountTypeRepository extends JpaRepository<AmountType, Long> {
    List<AmountType> findAllAmountTypeByUserUserName(String userName);
    Optional<AmountType> findAmountTypeByUserUserNameAndAmountTypeId(String userName, long amountTypeId_amountTypeId);
    List<AmountType> findAmountTypeByUserUserNameAndSavedTimeAfter(String userNAme, LocalDateTime localDateTime);
    List<AmountType> findAmountTypeByUserUserNameAndSavedTimeAfterAndDeletedIsFalse(String userNAme, LocalDateTime localDateTime);
    @Transactional
    void deleteByAmountTypeIdAndUserUserName(Long amountTypeId, String userName);
}
