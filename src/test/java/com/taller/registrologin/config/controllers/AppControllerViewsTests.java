package com.taller.registrologin.config.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.taller.registrologin.models.Users;
import com.taller.registrologin.repositories.UsersRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppControllerViewsTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void rendersHomeRegistrationAndStylesheet() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/css/app.css")));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register_form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/process_register")));

        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void registrationShowsSuccessAndDoesNotRedisplayDuplicatePassword() throws Exception {
        String email = "view-test-" + UUID.randomUUID() + "@example.test";
        String password = "initial-secret";

        mockMvc.perform(post("/process_register")
                        .with(csrf())
                        .param("firstName", "Prueba")
                        .param("lastName", "Vista")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(view().name("registration_success"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registro exitoso")));

        String duplicatePassword = "must-not-be-redisplayed";
        mockMvc.perform(post("/process_register")
                        .with(csrf())
                        .param("firstName", "Prueba")
                        .param("lastName", "Vista")
                        .param("email", email)
                        .param("password", duplicatePassword))
                .andExpect(status().isOk())
                .andExpect(view().name("register_form"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(duplicatePassword))));
    }

    @Test
    void rejectsInvalidRegistrationOnTheServerAndShowsFieldErrors() throws Exception {
        String shortPassword = "short";

        mockMvc.perform(post("/process_register")
                        .with(csrf())
                        .param("firstName", " ")
                        .param("lastName", "")
                        .param("email", "not-an-email")
                        .param("password", shortPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("register_form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El nombre es obligatorio.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("El apellido es obligatorio.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ingresa un correo electrónico válido.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "La contraseña debe tener entre 8 y 100 caracteres.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(shortPassword))));
    }

    @Test
    void rendersUserListOnlyForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/users").with(user("viewer@example.test")))
                .andExpect(status().isOk())
                .andExpect(view().name("users_list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuarios registrados")));
    }

    @Test
    void logsInListsUsersAndLogsOutWithoutPersistingTestUser() throws Exception {
        String email = "session-test-" + UUID.randomUUID() + "@example.test";
        String rawPassword = "temporary-test-password";

        Users user = new Users();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName("Sesion");
        user.setLastName("Temporal");
        usersRepository.saveAndFlush(user);
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/users"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/users").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(email)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sesion")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Temporal")));

        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/"));

        mockMvc.perform(get("/users").session(session))
                .andExpect(status().is3xxRedirection());
    }
}
