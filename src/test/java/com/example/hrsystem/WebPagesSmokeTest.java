package com.example.hrsystem;

import com.example.hrsystem.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebPagesSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Кадровий реєстр")));
    }

    @Test
    void reportsPageRendersForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/reports").sessionAttr(LoginController.SESSION_USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Огляд HR-бази даних")));
    }
}
