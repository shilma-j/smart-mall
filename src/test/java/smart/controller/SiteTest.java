package smart.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import smart.lib.Captcha;
import smart.lib.Helper;
import smart.lib.UserAgent;
import smart.lib.session.Session;

@AutoConfigureMockMvc
@SpringBootTest
@Timeout(2)
public class SiteTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCaptcha() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/captcha")).andReturn();
        Session session = Session.from(mvcResult.getRequest());
        Assertions.assertNotNull(session);
        String code = (String) session.get(Helper.camelCase(Captcha.class));
        MockHttpServletResponse response = mvcResult.getResponse();
        Assertions.assertEquals(Captcha.SIZE, code.length());
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertTrue(response.getContentAsByteArray().length > 600);
        var contentType = response.getContentType();
        Assertions.assertNotNull(contentType);
        Assertions.assertTrue(contentType.startsWith("image/png"));
    }

    @Test
    public void testPage() throws Exception {
        logger.warn("sssss");
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/captcha")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/category")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/category").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/list")).andReturn().getResponse().getStatus());
        Assertions.assertEquals(200, mockMvc.perform(MockMvcRequestBuilders.get("/list").header("user-agent", UserAgent.IPHONE_SAFARI)).andReturn().getResponse().getStatus());
    }

}
