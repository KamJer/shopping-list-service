package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamjer.shoppinglistservice.DatabaseUtil;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.model.AmountType;
import pl.kamjer.shoppinglistservice.model.ModifyState;
import pl.kamjer.shoppinglistservice.model.User;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WebSocketUtilServiceSyncEntitiesTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2024, 1, 1, 12, 0);
    private static final LocalDateTime T1 = LocalDateTime.of(2024, 6, 1, 12, 0);

    @Mock
    private AmountTypeRepository amountTypeRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ShoppingItemRepository shoppingItemRepository;
    @Mock
    private SecClient secClient;
    @Mock
    private WebSocketDataHolder webSocketDataHolder;

    private WebSocketUtilService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new WebSocketUtilService(
                secClient,
                amountTypeRepository,
                categoryRepository,
                shoppingItemRepository,
                webSocketDataHolder,
                new ObjectMapper()
        );
        user = User.builder().userName("tester").build();
    }

    @Test
    void syncEntities_insert_callsSaveForNewEntity() {
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(0L)
                .typeName("Kg")
                .modifyState(ModifyState.INSERT)
                .savedTime(T0)
                .build();
        List<AmountType> db = new ArrayList<>();

        service.syncEntities(
                List.of(dto),
                db,
                user,
                T1,
                amountTypeRepository::save,
                DatabaseUtil::toAmountType,
                AmountType::getAmountTypeId,
                AmountType::setSavedTime,
                AmountType::setDeleted
        );

        ArgumentCaptor<AmountType> captor = ArgumentCaptor.forClass(AmountType.class);
        verify(amountTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getTypeName()).isEqualTo("Kg");
        assertThat(captor.getValue().getUserName()).isEqualTo("tester");
        verifyNoInteractions(categoryRepository, shoppingItemRepository);
    }

    @Test
    void syncEntities_update_existing_mutatesInPlaceAndDoesNotCallSave() {
        AmountType existing = AmountType.builder()
                .amountTypeId(10L)
                .userName("tester")
                .typeName("Old")
                .savedTime(T0)
                .deleted(false)
                .build();
        List<AmountType> db = new ArrayList<>(List.of(existing));

        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(10L)
                .typeName("New")
                .modifyState(ModifyState.UPDATE)
                .savedTime(T0)
                .build();

        service.syncEntities(
                List.of(dto),
                db,
                user,
                T1,
                amountTypeRepository::save,
                DatabaseUtil::toAmountType,
                AmountType::getAmountTypeId,
                AmountType::setSavedTime,
                AmountType::setDeleted
        );

        assertThat(existing.getTypeName()).isEqualTo("New");
        assertThat(existing.getSavedTime()).isEqualTo(T1);
        verify(amountTypeRepository, never()).save(any());
    }

    @Test
    void syncEntities_delete_softDeletesExisting() {
        AmountType existing = AmountType.builder()
                .amountTypeId(3L)
                .userName("tester")
                .typeName("Pc")
                .savedTime(T0)
                .deleted(false)
                .build();
        List<AmountType> db = new ArrayList<>(List.of(existing));

        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(3L)
                .typeName("Pc")
                .modifyState(ModifyState.DELETE)
                .savedTime(T0)
                .build();

        service.syncEntities(
                List.of(dto),
                db,
                user,
                T1,
                amountTypeRepository::save,
                DatabaseUtil::toAmountType,
                AmountType::getAmountTypeId,
                AmountType::setSavedTime,
                AmountType::setDeleted
        );

        assertThat(existing.isDeleted()).isTrue();
        assertThat(existing.getSavedTime()).isEqualTo(T1);
        verify(amountTypeRepository, never()).save(any());
    }

    @Test
    void syncEntities_update_whenMissingInDb_insertsViaSave() {
        List<AmountType> db = new ArrayList<>();
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(99L)
                .typeName("orphan")
                .modifyState(ModifyState.UPDATE)
                .savedTime(T0)
                .build();

        service.syncEntities(
                List.of(dto),
                db,
                user,
                T1,
                amountTypeRepository::save,
                DatabaseUtil::toAmountType,
                AmountType::getAmountTypeId,
                AmountType::setSavedTime,
                AmountType::setDeleted
        );

        ArgumentCaptor<AmountType> captor = ArgumentCaptor.forClass(AmountType.class);
        verify(amountTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getAmountTypeId()).isEqualTo(99L);
        assertThat(captor.getValue().getTypeName()).isEqualTo("orphan");
    }
}
