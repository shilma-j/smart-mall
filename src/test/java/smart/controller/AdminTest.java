package smart.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
@Timeout(5)
public class AdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    public void testPage() throws Exception {
        Assertions.assertNotEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/admin")).andReturn().getResponse().getStatus());
    }


}
