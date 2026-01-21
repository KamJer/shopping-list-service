package pl.kamjer.shoppinglistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.shoppinglistservice.model.AmountType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmountTypeRepository extends JpaRepository<AmountType, Long> {
    Optional<AmountType> findAmountTypeByUserNameAndAmountTypeId(String userName, long amountTypeId_amountTypeId);
    List<AmountType> findAmountTypeByUserNameAndSavedTimeAfter(String userNAme, LocalDateTime localDateTime);
    List<AmountType> findByUserName(String userName);
}
