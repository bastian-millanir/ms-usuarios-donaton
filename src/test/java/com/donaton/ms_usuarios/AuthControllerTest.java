package com.donaton.ms_usuarios;

import com.donaton.ms_usuarios.controller.AuthController;
import com.donaton.ms_usuarios.dto.AuthResponse;
import com.donaton.ms_usuarios.dto.LoginRequest;
import com.donaton.ms_usuarios.dto.RegisterRequest;
import com.donaton.ms_usuarios.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;       // ← Boot 4.x
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // ← Boot 4.x (reemplaza @MockBean)
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean                          // ← Boot 4.x: reemplaza @MockBean
    private UserService userService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("plain123");
        req.setNombre("Juan Pérez");
        return req;
    }

    private LoginRequest buildLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("plain123");
        return req;
    }

    private AuthResponse buildRegisterResponse() {
        return AuthResponse.builder()
                .token("jwt.token.mock")
                .email("test@example.com")
                .username("Juan Pérez")
                .mensaje("Registro exitoso")
                .build();
    }

    private AuthResponse buildLoginResponse() {
        return AuthResponse.builder()
                .token("jwt.token.mock")
                .email("test@example.com")
                .username("Juan Pérez")
                .build();
    }

    // =========================================================================
    // POST /auth/register
    // =========================================================================

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @WithMockUser                     // ← Boot 4.x: Security activo, necesita usuario mock
        @DisplayName("200 OK: registro exitoso retorna token, email, username y mensaje")
        void register_success_returns200() throws Exception {
            when(userService.register(any(RegisterRequest.class)))
                    .thenReturn(buildRegisterResponse());

            mockMvc.perform(post("/auth/register")
                            .with(csrf())             // ← CSRF requerido con Security activo
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.mock"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.username").value("Juan Pérez"))
                    .andExpect(jsonPath("$.mensaje").value("Registro exitoso"));
        }

        @Test
        @WithMockUser
        @DisplayName("409 CONFLICT: email duplicado retorna mensaje de error")
        void register_emailDuplicated_returns409() throws Exception {
            when(userService.register(any(RegisterRequest.class)))
                    .thenThrow(new UserService.EmailAlreadyExistsException());

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("El email ya está registrado"));
        }

        @Test
        @WithMockUser
        @DisplayName("Delega exactamente una vez al servicio")
        void register_delegatesToServiceOnce() throws Exception {
            when(userService.register(any())).thenReturn(buildRegisterResponse());

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest())))
                    .andExpect(status().isOk());

            verify(userService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("JsonInclude.NON_NULL: campos null no aparecen en la respuesta")
        void register_nullFieldsNotSerializedInResponse() throws Exception {
            AuthResponse sinMensaje = AuthResponse.builder()
                    .token("jwt.token.mock")
                    .email("test@example.com")
                    .username("Juan Pérez")
                    .build();

            when(userService.register(any())).thenReturn(sinMensaje);

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").doesNotExist());
        }
    }

    // =========================================================================
    // POST /auth/login
    // =========================================================================

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @WithMockUser
        @DisplayName("200 OK: login exitoso retorna token, email y username")
        void login_success_returns200() throws Exception {
            when(userService.login(any(LoginRequest.class)))
                    .thenReturn(buildLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.mock"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.username").value("Juan Pérez"));
        }

        @Test
        @WithMockUser
        @DisplayName("200 OK: login por campo username (fallback)")
        void login_withUsername_returns200() throws Exception {
            LoginRequest byUsername = new LoginRequest();
            byUsername.setUsername("test@example.com");
            byUsername.setPassword("plain123");

            when(userService.login(any(LoginRequest.class)))
                    .thenReturn(buildLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(byUsername)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("401 UNAUTHORIZED: credenciales inválidas retorna mensaje de error")
        void login_invalidCredentials_returns401() throws Exception {
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new UserService.InvalidCredentialsException());

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
        }

        @Test
        @WithMockUser
        @DisplayName("Login exitoso no expone campo 'mensaje'")
        void login_success_doesNotExposeMensaje() throws Exception {
            when(userService.login(any())).thenReturn(buildLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").doesNotExist());
        }

        @Test
        @WithMockUser
        @DisplayName("Delega exactamente una vez al servicio")
        void login_delegatesToServiceOnce() throws Exception {
            when(userService.login(any())).thenReturn(buildLoginResponse());

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest())))
                    .andExpect(status().isOk());

            verify(userService, times(1)).login(any(LoginRequest.class));
        }
    }
}