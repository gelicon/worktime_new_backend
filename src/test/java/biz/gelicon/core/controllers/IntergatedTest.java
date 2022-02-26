package biz.gelicon.core.controllers;

import biz.gelicon.core.config.Config;
import biz.gelicon.core.config.EditionTag;
import biz.gelicon.core.repository.RecreateDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class IntergatedTest {
    static public Logger logger = LoggerFactory.getLogger(IntergatedTest.class);

    public MockMvc mockMvc;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    RecreateDatabase recreateDatabase;

    static boolean start = false;

    protected static String token;

    @BeforeEach
    void beforeRunTest() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/").header("Authorization","Bearer "+token))
                .defaultRequest(get("/").header("Authorization","Bearer "+token))
                .apply(springSecurity())
                .build();
        if(!start) {
            onStart();
            start = true;
        }
    }

    protected void onStart() {

    }

    protected String buildUrl(String uri) {
        return "/v" + Config.CURRENT_VERSION + uri;
    }

    protected String buildUrl(String uri, String contoure, String module) {
        return "/v" + Config.CURRENT_VERSION + "/apps/" + contoure + "/" + module + "/" + uri;
    }

    protected String buildUrl(String uri, String contoure) {
        return "/v"+ Config.CURRENT_VERSION+"/apps/"+contoure+"/"+uri;
    }

}
