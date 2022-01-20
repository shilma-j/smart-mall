package smart;

import smart.authentication.UserToken;
import smart.lib.Helper;
import smart.lib.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TokenTests {

    @Test
    public void test() {
        var salt = Helper.randomString(4);
        UserToken userToken = new UserToken();
        userToken.setSalt(salt);
        var token = Json.decode(userToken.toString(), UserToken.class);
        assert token != null;
        Assertions.assertEquals(token.getSalt(), salt);
    }
}
