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
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;
import pl.kamjer.shoppinglistservice.repository.CategoryRepository;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketCategoryServiceTest {

    private static final User USER = User.builder().userName("tester").password("token").build();

    @Mock private SecClient secClient;
    @Mock private WebSocketDataHolder webSocketDataHolder;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ShoppingItemRepository shoppingItemRepository;

    private ShoppingEntityMapperImpl shoppingEntityMapper;
    private WebSocketCategoryService service;

    @BeforeEach
    void setUp() {
        shoppingEntityMapper = new ShoppingEntityMapperImpl();
        ReflectionTestUtils.setField(shoppingEntityMapper, "idAdjuster", new IdAdjuster());
        service = new WebSocketCategoryService(
                secClient, webSocketDataHolder, categoryRepository,
                shoppingItemRepository,
                new ObjectMapper(), shoppingEntityMapper);
        service = spy(service);
        doReturn(USER).when(service).requireAuthenticatedUser();
    }

    @Test
    void putCategory_savesNewCategoryAndReturnsDtoWithUpdate() {
        CategoryDto dto = CategoryDto.builder()
                .categoryName("Dairy")
                .localId(50L)
                .build();

        when(categoryRepository.save(any())).then(returnsFirstArg());

        CategoryDto result = service.putCategory(dto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryName()).isEqualTo("Dairy");
        assertThat(captor.getValue().getUserName()).isEqualTo("tester");

        verify(secClient).putUser(any(UserDto.class), eq("token"));
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(50L);
    }

    @Test
    void postCategory_whenExists_updatesAndReturnsUpdate() {
        Category existing = Category.builder()
                .categoryId(10L)
                .userName("tester")
                .categoryName("Old")
                .deleted(false)
                .build();

        CategoryDto dto = CategoryDto.builder()
                .categoryId(10L)
                .categoryName("New")
                .deleted(false)
                .localId(50L)
                .build();

        when(categoryRepository.findCategoryByUserNameAndCategoryId("tester", 10L))
                .thenReturn(Optional.of(existing));

        CategoryDto result = service.postCategory(dto);

        assertThat(existing.getCategoryName()).isEqualTo("New");
        verify(secClient).putUser(any(UserDto.class), eq("token"));
        assertThat(result.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(result.getLocalId()).isEqualTo(50L);
    }

    @Test
    void postCategory_whenNotExists_delegatesToPutCategory() {
        CategoryDto dto = CategoryDto.builder()
                .categoryId(10L)
                .categoryName("New")
                .build();

        when(categoryRepository.findCategoryByUserNameAndCategoryId("tester", 10L))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(any())).then(returnsFirstArg());

        service.postCategory(dto);

        verify(service).putCategory(dto);
    }

    @Test
    void deleteCategory_whenExists_softDeletesAndCascadesToShoppingItems() {
        Category existing = Category.builder()
                .categoryId(10L)
                .userName("tester")
                .categoryName("Dairy")
                .deleted(false)
                .build();

        CategoryDto dto = CategoryDto.builder()
                .categoryId(10L)
                .deleted(true)
                .localId(50L)
                .build();

        when(categoryRepository.findCategoryByUserNameAndCategoryId("tester", 10L))
                .thenReturn(Optional.of(existing));

        CategoryDto result = service.deleteCategory(dto);

        assertThat(existing.isDeleted()).isTrue();
        assertThat(existing.getLocalId()).isEqualTo(50L);
        verify(shoppingItemRepository).markAllDeletedByCategoryId(10L);
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
    }

    @Test
    void deleteCategory_whenNotExists_returnsCopiedDtoWithDelete() {
        CategoryDto dto = CategoryDto.builder()
                .categoryId(10L)
                .categoryName("Dairy")
                .build();

        when(categoryRepository.findCategoryByUserNameAndCategoryId("tester", 10L))
                .thenReturn(Optional.empty());

        CategoryDto result = service.deleteCategory(dto);

        verify(categoryRepository, never()).save(any());
        verify(shoppingItemRepository, never()).markAllDeletedByCategoryId(any());
        verify(secClient, never()).putUser(any(), any());
        assertThat(result.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(result.getCategoryName()).isEqualTo("Dairy");
    }
}
