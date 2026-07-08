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
import pl.kamjer.shoppinglistservice.exception.NoResourcesFoundException;
import pl.kamjer.shoppinglistservice.mapping.IdAdjuster;
import pl.kamjer.shoppinglistservice.mapping.ShoppingEntityMapperImpl;
import pl.kamjer.shoppinglistservice.mapping.ShoppingItemResolver;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.AmountTypeRepository;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketShoppingItemServiceTest {

    private static final User USER = User.builder().userName("tester").password("token").build();

    @Mock private SecClient secClient;
    @Mock private WebSocketDataHolder webSocketDataHolder;
    @Mock private ShoppingItemRepository shoppingItemRepository;
    @Mock private AmountTypeRepository amountTypeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ShoppingItemResolver shoppingItemResolver;

    private ShoppingEntityMapperImpl shoppingEntityMapper;
    private WebSocketShoppingItemService service;

    @BeforeEach
    void setUp() {
        shoppingEntityMapper = new ShoppingEntityMapperImpl();
        ReflectionTestUtils.setField(shoppingEntityMapper, "idAdjuster", new IdAdjuster());
        service = new WebSocketShoppingItemService(
                secClient, webSocketDataHolder, shoppingItemRepository,
                amountTypeRepository, categoryRepository, new ObjectMapper(),
                shoppingEntityMapper, shoppingItemResolver);
        service = spy(service);
        doReturn(USER).when(service).requireAuthenticatedUser();
    }

    @Test
    void putShoppingItem_savesResolvedItemAndReturnsDtoWithUpdate() {
        AmountType amountType = AmountType.builder().amountTypeId(1L).typeName("kg").build();
        Category category = Category.builder().categoryId(2L).categoryName("Food").build();
        ShoppingItem resolved = ShoppingItem.builder()
                .userName("tester")
                .itemAmountType(amountType)
                .itemCategory(category)
                .itemName("Apples")
                .amount(2.0)
                .bought(false)
                .deleted(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .itemName("Apples")
                .amount(2.0)
                .itemAmountTypeId(1L)
                .itemCategoryId(2L)
                .localId(100L)
                .localAmountTypeId(10L)
                .localCategoryId(20L)
                .build();

        when(shoppingItemResolver.resolve(any(), anyMap(), anyMap(), any(), any())).thenReturn(resolved);
        when(shoppingItemRepository.save(any())).then(returnsFirstArg());

        ShoppingItemDto result = service.putShoppingItem(dto);

        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository).save(captor.capture());
        assertThat(captor.getValue().getItemName()).isEqualTo("Apples");

        verify(shoppingItemResolver).resolve(eq(USER), anyMap(), anyMap(), eq(dto), any());
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(100L);
        assertThat(result.getLocalAmountTypeId()).isEqualTo(10L);
        assertThat(result.getLocalCategoryId()).isEqualTo(20L);
    }

    @Test
    void putShoppingItem_whenResolverThrows_throwsNoResourcesFoundException() {
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .itemAmountTypeId(999L)
                .itemCategoryId(999L)
                .build();

        when(shoppingItemResolver.resolve(any(), anyMap(), anyMap(), any(), any()))
                .thenThrow(new NoResourcesFoundException("not found"));

        assertThatThrownBy(() -> service.putShoppingItem(dto))
                .isInstanceOf(NoResourcesFoundException.class);
        verify(shoppingItemRepository, never()).save(any());
        verifyNoInteractions(secClient);
    }

    @Test
    void postShoppingItem_whenExists_updatesAndReturnsUpdate() {
        AmountType amountType = AmountType.builder().amountTypeId(1L).typeName("kg").build();
        Category category = Category.builder().categoryId(2L).categoryName("Food").build();
        ShoppingItem existing = ShoppingItem.builder()
                .shoppingItemId(10L)
                .userName("tester")
                .itemAmountType(amountType)
                .itemCategory(category)
                .itemName("Old")
                .amount(1.0)
                .bought(false)
                .deleted(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(10L)
                .itemName("New")
                .amount(2.0)
                .itemAmountTypeId(1L)
                .itemCategoryId(2L)
                .bought(true)
                .deleted(false)
                .localId(100L)
                .localAmountTypeId(10L)
                .localCategoryId(20L)
                .build();

        when(shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId("tester", 10L))
                .thenReturn(Optional.of(existing));
        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 1L))
                .thenReturn(Optional.of(amountType));
        when(categoryRepository.findCategoryByUserNameAndCategoryId("tester", 2L))
                .thenReturn(Optional.of(category));

        ShoppingItemDto result = service.postShoppingItem(dto);

        assertThat(existing.getItemName()).isEqualTo("New");
        assertThat(existing.getAmount()).isEqualTo(2.0);
        assertThat(existing.isBought()).isTrue();
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(100L);
    }

    @Test
    void postShoppingItem_whenExistsButAmountTypeMissing_throws() {
        ShoppingItem existing = ShoppingItem.builder()
                .shoppingItemId(10L)
                .userName("tester")
                .build();
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(10L)
                .itemAmountTypeId(999L)
                .itemCategoryId(2L)
                .build();

        when(shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId("tester", 10L))
                .thenReturn(Optional.of(existing));
        when(amountTypeRepository.findAmountTypeByUserNameAndAmountTypeId("tester", 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.postShoppingItem(dto))
                .isInstanceOf(NoResourcesFoundException.class)
                .hasMessageContaining("Amount Type or Category does not exist");
        verify(secClient, never()).putUser(any(), any());
    }

    @Test
    void postShoppingItem_whenNotExists_throwsIllegalArgument() {
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(10L)
                .itemName("Item")
                .amount(1.0)
                .itemAmountTypeId(1L)
                .itemCategoryId(1L)
                .deleted(false)
                .build();

        when(shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId("tester", 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.postShoppingItem(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteShoppingItem_whenExists_softDeletesAndReturnsDelete() {
        ShoppingItem existing = ShoppingItem.builder()
                .shoppingItemId(10L)
                .userName("tester")
                .deleted(false)
                .build();

        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(10L)
                .deleted(true)
                .localId(100L)
                .localAmountTypeId(10L)
                .localCategoryId(20L)
                .build();

        when(shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId("tester", 10L))
                .thenReturn(Optional.of(existing));

        ShoppingItemDto result = service.deleteShoppingItem(dto);

        assertThat(existing.isDeleted()).isTrue();
        assertThat(existing.getLocalShoppingItemId()).isEqualTo(100L);
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
    }

    @Test
    void deleteShoppingItem_whenNotExists_returnsCopiedDtoWithDelete() {
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(10L)
                .itemName("Item")
                .amount(1.0)
                .build();

        when(shoppingItemRepository.findShoppingItemByUserNameAndShoppingItemId("tester", 10L))
                .thenReturn(Optional.empty());

        ShoppingItemDto result = service.deleteShoppingItem(dto);

        verify(shoppingItemRepository, never()).save(any());
        verify(secClient, never()).putUser(any(), any());
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(result.getItemName()).isEqualTo("Item");
    }
}
