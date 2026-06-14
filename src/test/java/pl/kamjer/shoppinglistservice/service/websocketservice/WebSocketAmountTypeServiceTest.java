package pl.kamjer.shoppinglistservice.service.websocketservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kamjer.shoppinglistservice.client.SecClient;
import pl.kamjer.shoppinglistservice.config.websocket.WebSocketDataHolder;
import pl.kamjer.shoppinglistservice.mapping.IdAdjuster;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapperImpl;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAmountTypeServiceTest {

    private static final User USER = User.builder().userName("tester").password("token").build();

    @Mock private SecClient secClient;
    @Mock private WebSocketDataHolder webSocketDataHolder;
    @Mock private AmountTypeRepository amountTypeRepository;

    private ShoppingEntityMapperImpl shoppingEntityMapper;
    private WebSocketAmountTypeService service;

    @BeforeEach
    void setUp() {
        shoppingEntityMapper = new ShoppingEntityMapperImpl();
        ReflectionTestUtils.setField(shoppingEntityMapper, "idAdjuster", new IdAdjuster());
        service = new WebSocketAmountTypeService(
                secClient, webSocketDataHolder, amountTypeRepository,
                new ObjectMapper(), shoppingEntityMapper);
        service = spy(service);
        doReturn(USER).when(service).requireAuthenticatedUser();
    }

    @Test
    void putAmountType_savesNewAmountTypeAndReturnsDtoWithUpdate() {
        AmountTypeDto dto = AmountTypeDto.builder()
                .typeName("kg")
                .localId(50L)
                .build();

        when(amountTypeRepository.save(any())).then(returnsFirstArg());

        AmountTypeDto result = service.putAmountType(dto);

        ArgumentCaptor<AmountType> captor = ArgumentCaptor.forClass(AmountType.class);
        verify(amountTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getTypeName()).isEqualTo("kg");
        assertThat(captor.getValue().getUserName()).isEqualTo("tester");

        verify(secClient).putUser(any(UserDto.class), eq("token"));
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(50L);
    }

    @Test
    void postAmountType_whenExists_updatesAndReturnsUpdate() {
        AmountType existing = AmountType.builder()
                .amountTypeId(10L)
                .userName("tester")
                .typeName("Old")
                .deleted(false)
                .build();

        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(10L)
                .typeName("New")
                .deleted(false)
                .localId(50L)
                .build();

        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 10L))
                .thenReturn(Optional.of(existing));

        AmountTypeDto result = service.postAmountType(dto);

        assertThat(existing.getTypeName()).isEqualTo("New");
        verify(secClient).putUser(any(UserDto.class), eq("token"));
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(50L);
    }

    @Test
    void postAmountType_whenNotExists_delegatesToPutAmountType() {
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(10L)
                .typeName("New")
                .build();

        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 10L))
                .thenReturn(Optional.empty());

        when(amountTypeRepository.save(any())).then(returnsFirstArg());

        service.postAmountType(dto);

        verify(service).putAmountType(dto);
    }

    @Test
    void deleteAmountType_whenExists_softDeletesAndCascadesToShoppingItems() {
        ShoppingItem item1 = ShoppingItem.builder().shoppingItemId(1L).deleted(false).build();
        ShoppingItem item2 = ShoppingItem.builder().shoppingItemId(2L).deleted(false).build();
        AmountType existing = AmountType.builder()
                .amountTypeId(10L)
                .userName("tester")
                .typeName("kg")
                .deleted(false)
                .shoppingItemList(List.of(item1, item2))
                .build();

        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(10L)
                .deleted(true)
                .localId(50L)
                .build();

        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 10L))
                .thenReturn(Optional.of(existing));

        AmountTypeDto result = service.deleteAmountType(dto);

        assertThat(existing.isDeleted()).isTrue();
        assertThat(existing.getLocalId()).isEqualTo(50L);
        assertThat(item1.isDeleted()).isTrue();
        assertThat(item2.isDeleted()).isTrue();
        verify(secClient).putUser(any(UserDto.class), eq("token"));
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
    }

    @Test
    void deleteAmountType_whenNotExists_returnsCopiedDtoWithDelete() {
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(10L)
                .typeName("kg")
                .build();

        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 10L))
                .thenReturn(Optional.empty());

        AmountTypeDto result = service.deleteAmountType(dto);

        verify(amountTypeRepository, never()).save(any());
        verify(secClient, never()).putUser(any(), any());
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(result.getTypeName()).isEqualTo("kg");
    }
}
