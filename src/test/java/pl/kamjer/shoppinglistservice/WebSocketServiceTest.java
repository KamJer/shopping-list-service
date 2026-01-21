package pl.kamjer.shoppinglistservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {
        "/schema.sql",
        "/test_data.sql"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WebSocketServiceTest {

//    private WebSocketService synchronizationService;
//    @Autowired
//    private EntityManager entityManager;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private AmountTypeRepository amountTypeRepository;
//    @Autowired
//    private CategoryRepository categoryRepository;
//    @Autowired
//    private ShoppingItemRepository shoppingItemRepository;
//
//    @BeforeEach
//    void setUp() {
//        WebSocketSession mockSession = Mockito.mock(WebSocketSession.class);
//        Principal mockPrincipal = () -> "test-user";
//        Mockito.when(mockSession.getPrincipal()).thenReturn(mockPrincipal);
//        ThreadLocal<WebSocketSession> threadLocal = new ThreadLocal<>();
//        threadLocal.set(mockSession);
//
//        WebSocketDataHolder webSocketDataHolder = new WebSocketDataHolder(
//                new ConcurrentHashMap<>(),
//                new ConcurrentHashMap<>(),
//                threadLocal,
//                new CopyOnWriteArrayList<>(),
//                new CopyOnWriteArrayList<>()
//        );
//
//        synchronizationService =
//                new WebSocketService(userRepository,
//                        amountTypeRepository,
//                        categoryRepository,
//                        shoppingItemRepository,
//                        webSocketDataHolder,
//                        entityManager);
//    }
//
//
//    @Test
//    void shouldSynchronizeDataCorrectly() {
//        // Given
//        AllDto allDto = AllDto.builder()
//                .savedTime(LocalDateTime.of(2024, 1, 1, 0, 0))
//                .amountTypeDtoList(List.of(
//                        AmountTypeDto.builder()
//                                .localId(1L)
//                                .amountTypeId(1L)
//                                .typeName("Updated Type")
//                                .modifyState(ModifyState.UPDATE)
//                                .deleted(false)
//                                .build(),
//                        AmountTypeDto.builder()
//                                .localId(2L)
//                                .typeName("kg")
//                                .modifyState(ModifyState.INSERT)
//                                .deleted(false)
//                                .build(),
//                        AmountTypeDto.builder()
//                                .localId(3L)
//                                .typeName("szt")
//                                .modifyState(ModifyState.INSERT)
//                                .deleted(false)
//                                .build()
//
//                ))
//                .categoryDtoList(List.of(
//                        CategoryDto.builder()
//                                .categoryName("test")
//                                .localId(1L)
//                                .modifyState(ModifyState.INSERT)
//                                .deleted(false)
//                                .build(),
//                        CategoryDto.builder()
//                                .categoryName("test1")
//                                .localId(2L)
//                                .modifyState(ModifyState.INSERT)
//                                .deleted(false)
//                                .build()
//                ))
//                .shoppingItemDtoList(List.of())
//                .build();
//
//        // When
//        AllDto result = synchronizationService.synchronizeWebSocket(allDto);
//
//        // Then
//        assertThat(result.getAmountTypeDtoList()).anySatisfy(dto -> {
//            assertThat(dto.getModifyState()).isEqualTo(ModifyState.UPDATE);
//            assertThat(dto.getTypeName()).isEqualTo("Updated Type");
//        });
//    }

}
