package pl.kamjer.shoppinglistservice.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kamjer.shoppinglistservice.model.*;
import pl.kamjer.shoppinglistservice.model.dto.AmountTypeDto;
import pl.kamjer.shoppinglistservice.model.dto.CategoryDto;
import pl.kamjer.shoppinglistservice.model.dto.ShoppingItemDto;
import pl.kamjer.shoppinglistservice.model.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShoppingEntityMapperTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2024, 3, 15, 10, 0);
    private static final LocalDateTime T1 = LocalDateTime.of(2024, 9, 1, 12, 0);

    private ShoppingEntityMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new ShoppingEntityMapperImpl();
        ReflectionTestUtils.setField(mapper, "idAdjuster", new IdAdjuster());
    }

    @Test
    void toAmountType_mapsFields_andZeroIdBecomesNull() {
        User user = User.builder().userName("alice").build();
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(0L)
                .typeName("kg")
                .deleted(true)
                .modifyState(ModifyState.INSERT)
                .localId(7L)
                .build();

        AmountType entity = mapper.toAmountType(user, dto, T0);

        assertThat(entity.getAmountTypeId()).isNull();
        assertThat(entity.getUserName()).isEqualTo("alice");
        assertThat(entity.getTypeName()).isEqualTo("kg");
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getLocalId()).isEqualTo(7L);
        assertThat(entity.getSavedTime()).isEqualTo(T0);
        assertThat(entity.getShoppingItemList()).isNull();
    }

    @Test
    void toAmountType_positiveIdPreserved() {
        User user = User.builder().userName("bob").build();
        AmountTypeDto dto = AmountTypeDto.builder()
                .amountTypeId(42L)
                .typeName("pcs")
                .deleted(false)
                .modifyState(ModifyState.UPDATE)
                .localId(1L)
                .build();

        AmountType entity = mapper.toAmountType(user, dto, T1);

        assertThat(entity.getAmountTypeId()).isEqualTo(42L);
    }

    @Test
    void toCategory_mapsFields_andZeroIdBecomesNull() {
        User user = User.builder().userName("carol").build();
        CategoryDto dto = CategoryDto.builder()
                .categoryId(0L)
                .categoryName("Dairy")
                .deleted(false)
                .modifyState(ModifyState.INSERT)
                .localId(3L)
                .build();

        Category entity = mapper.toCategory(user, dto, T0);

        assertThat(entity.getCategoryId()).isNull();
        assertThat(entity.getUserName()).isEqualTo("carol");
        assertThat(entity.getCategoryName()).isEqualTo("Dairy");
        assertThat(entity.getSavedTime()).isEqualTo(T0);
        assertThat(entity.getShoppingItemList()).isNull();
    }

    @Test
    void toAmountTypeDto_twoArg_setsModifyState() {
        AmountType entity = AmountType.builder()
                .amountTypeId(5L)
                .userName("u")
                .typeName("L")
                .savedTime(T0)
                .deleted(false)
                .localId(9L)
                .build();

        AmountTypeDto dto = mapper.toAmountTypeDto(entity, ModifyState.DELETE);

        assertThat(dto.getAmountTypeId()).isEqualTo(5L);
        assertThat(dto.getTypeName()).isEqualTo("L");
        assertThat(dto.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(dto.getLocalId()).isEqualTo(9L);
    }

    @Test
    void toAmountTypeDto_threeArg_includesSavedTime() {
        AmountType entity = AmountType.builder()
                .amountTypeId(1L)
                .userName("u")
                .typeName("x")
                .savedTime(T0)
                .deleted(false)
                .localId(0L)
                .build();

        AmountTypeDto dto = mapper.toAmountTypeDto(entity, ModifyState.UPDATE, T1);

        assertThat(dto.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(dto.getSavedTime()).isEqualTo(T1);
    }

    @Test
    void copyAmountTypeDto_onlyMappedFields() {
        AmountTypeDto src = AmountTypeDto.builder()
                .amountTypeId(10L)
                .typeName("name")
                .deleted(true)
                .modifyState(ModifyState.INSERT)
                .localId(2L)
                .savedTime(T0)
                .build();

        AmountTypeDto out = mapper.copyAmountTypeDto(src, ModifyState.DELETE);

        assertThat(out.getAmountTypeId()).isEqualTo(10L);
        assertThat(out.getTypeName()).isEqualTo("name");
        assertThat(out.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(out.getLocalId()).isEqualTo(2L);
    }

    @Test
    void toCategoryDto_twoArg() {
        Category entity = Category.builder()
                .categoryId(8L)
                .userName("u")
                .categoryName("Snacks")
                .savedTime(T0)
                .deleted(false)
                .localId(4L)
                .build();

        CategoryDto dto = mapper.toCategoryDto(entity, ModifyState.INSERT);

        assertThat(dto.getCategoryId()).isEqualTo(8L);
        assertThat(dto.getCategoryName()).isEqualTo("Snacks");
        assertThat(dto.getModifyState()).isEqualTo(ModifyState.INSERT);
    }

    @Test
    void toCategoryDto_threeArg() {
        Category entity = Category.builder()
                .categoryId(1L)
                .userName("u")
                .categoryName("c")
                .savedTime(T0)
                .deleted(false)
                .localId(0L)
                .build();

        CategoryDto dto = mapper.toCategoryDto(entity, ModifyState.UPDATE, T1);

        assertThat(dto.getSavedTime()).isEqualTo(T1);
    }

    @Test
    void copyCategoryDto_onlyMappedFields() {
        CategoryDto src = CategoryDto.builder()
                .categoryId(3L)
                .categoryName("Bakery")
                .deleted(false)
                .modifyState(ModifyState.UPDATE)
                .localId(1L)
                .savedTime(T0)
                .build();

        CategoryDto out = mapper.copyCategoryDto(src, ModifyState.DELETE);

        assertThat(out.getCategoryId()).isEqualTo(3L);
        assertThat(out.getCategoryName()).isEqualTo("Bakery");
        assertThat(out.getModifyState()).isEqualTo(ModifyState.DELETE);
    }

    @Test
    void toUserDto() {
        User user = User.builder().userName("dave").savedTime(T0).build();

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto.getUserName()).isEqualTo("dave");
        assertThat(dto.getSavedTime()).isEqualTo(T0);
    }

    @Test
    void toShoppingItemDto_twoArg_readsNestedIds() {
        AmountType at = AmountType.builder().amountTypeId(100L).userName("u").typeName("t").savedTime(T0).deleted(false).build();
        Category cat = Category.builder().categoryId(200L).userName("u").categoryName("c").savedTime(T0).deleted(false).build();
        ShoppingItem item = ShoppingItem.builder()
                .shoppingItemId(50L)
                .userName("u")
                .itemAmountType(at)
                .itemCategory(cat)
                .itemName("milk")
                .amount(2.5)
                .bought(false)
                .savedTime(T0)
                .deleted(false)
                .localShoppingItemId(11L)
                .localAmountTypeId(1L)
                .localCategoryId(2L)
                .build();

        ShoppingItemDto dto = mapper.toShoppingItemDto(item, ModifyState.UPDATE);

        assertThat(dto.getShoppingItemId()).isEqualTo(50L);
        assertThat(dto.getItemAmountTypeId()).isEqualTo(100L);
        assertThat(dto.getItemCategoryId()).isEqualTo(200L);
        assertThat(dto.getItemName()).isEqualTo("milk");
        assertThat(dto.getAmount()).isEqualTo(2.5);
        assertThat(dto.getModifyState()).isEqualTo(ModifyState.UPDATE);
        assertThat(dto.getLocalId()).isEqualTo(11L);
    }

    @Test
    void toShoppingItemDto_threeArg_setsSavedTimeOnDto() {
        AmountType at = AmountType.builder().amountTypeId(1L).userName("u").typeName("t").savedTime(T0).deleted(false).build();
        Category cat = Category.builder().categoryId(2L).userName("u").categoryName("c").savedTime(T0).deleted(false).build();
        ShoppingItem item = ShoppingItem.builder()
                .shoppingItemId(1L)
                .userName("u")
                .itemAmountType(at)
                .itemCategory(cat)
                .itemName("x")
                .amount(1.0)
                .bought(true)
                .savedTime(T0)
                .deleted(false)
                .localShoppingItemId(0L)
                .localAmountTypeId(0L)
                .localCategoryId(0L)
                .build();

        ShoppingItemDto dto = mapper.toShoppingItemDto(item, ModifyState.INSERT, T1);

        assertThat(dto.getSavedTime()).isEqualTo(T1);
    }

    @Test
    void copyShoppingItemDto_overridesStateAndSavedTime() {
        ShoppingItemDto src = ShoppingItemDto.builder()
                .shoppingItemId(9L)
                .itemAmountTypeId(1L)
                .itemCategoryId(2L)
                .itemName("bread")
                .amount(1.0)
                .bought(false)
                .deleted(false)
                .modifyState(ModifyState.INSERT)
                .localId(5L)
                .localAmountTypeId(6L)
                .localCategoryId(7L)
                .savedTime(T0)
                .build();

        ShoppingItemDto out = mapper.copyShoppingItemDto(src, ModifyState.DELETE, T1);

        assertThat(out.getModifyState()).isEqualTo(ModifyState.DELETE);
        assertThat(out.getSavedTime()).isEqualTo(T1);
        assertThat(out.getItemName()).isEqualTo("bread");
        assertThat(out.getShoppingItemId()).isEqualTo(9L);
    }

    @Test
    void toShoppingItem_mapsRelationsAndScalars() {
        User user = User.builder().userName("eve").build();
        AmountType at = AmountType.builder().amountTypeId(30L).userName("eve").typeName("kg").savedTime(T0).deleted(false).build();
        Category cat = Category.builder().categoryId(40L).userName("eve").categoryName("x").savedTime(T0).deleted(false).build();
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(0L)
                .itemAmountTypeId(30L)
                .itemCategoryId(40L)
                .itemName("apples")
                .amount(3.0)
                .bought(false)
                .deleted(false)
                .modifyState(ModifyState.INSERT)
                .localId(1L)
                .localAmountTypeId(2L)
                .localCategoryId(3L)
                .build();

        ShoppingItem entity = mapper.toShoppingItem(user, dto, T1, at, cat);

        assertThat(entity.getShoppingItemId()).isNull();
        assertThat(entity.getUserName()).isEqualTo("eve");
        assertThat(entity.getItemAmountType()).isSameAs(at);
        assertThat(entity.getItemCategory()).isSameAs(cat);
        assertThat(entity.getItemName()).isEqualTo("apples");
        assertThat(entity.getAmount()).isEqualTo(3.0);
        assertThat(entity.getSavedTime()).isEqualTo(T1);
        assertThat(entity.getLocalShoppingItemId()).isEqualTo(1L);
    }

    @Test
    void toShoppingItem_positiveIdPreserved() {
        User user = User.builder().userName("u").build();
        AmountType at = AmountType.builder().amountTypeId(1L).userName("u").typeName("t").savedTime(T0).deleted(false).build();
        Category cat = Category.builder().categoryId(2L).userName("u").categoryName("c").savedTime(T0).deleted(false).build();
        ShoppingItemDto dto = ShoppingItemDto.builder()
                .shoppingItemId(77L)
                .itemAmountTypeId(1L)
                .itemCategoryId(2L)
                .itemName("n")
                .amount(0.0)
                .bought(false)
                .deleted(false)
                .modifyState(ModifyState.UPDATE)
                .localId(0L)
                .localAmountTypeId(0L)
                .localCategoryId(0L)
                .build();

        ShoppingItem entity = mapper.toShoppingItem(user, dto, T0, at, cat);

        assertThat(entity.getShoppingItemId()).isEqualTo(77L);
    }
}
