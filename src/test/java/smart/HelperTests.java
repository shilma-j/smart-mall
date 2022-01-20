package smart;

import smart.lib.Helper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HelperTests {
    @Test
    public void test() {
        Assertions.assertEquals(Helper.camelCase(Object.class), "object");
        Assertions.assertEquals(Helper.camelCase("FirstName"), "firstName");
    }
}
