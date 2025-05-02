package pl.kamjer.shoppinglistservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import pl.kamjer.shoppinglistservice.service.UtilService;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {
        "/schema.sql",
        "/test_data.sql"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UtilServiceTest {

    private UtilService utilService;



}
