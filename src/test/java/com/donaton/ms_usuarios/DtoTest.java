package com.donaton.ms_usuarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.donaton.ms_usuarios.dto.AuthResponse;
import com.donaton.ms_usuarios.dto.LoginRequest;
import com.donaton.ms_usuarios.dto.RegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;

// Sin @SpringBootTest — puro JUnit 5, funciona igual en Boot 4.x
@DisplayName("DTOs")
class DtoTest {

    @Nested
    @DisplayName("AuthResponse")
    class AuthResponseTests {

        @Test
        @DisplayName("Builder setea todos los campos")
        void builder_allFields() {
            AuthResponse r = AuthResponse.builder()
                    .token("tok").username("user").email("e@e.com").mensaje("ok").build();

            assertThat(r.getToken()).isEqualTo("tok");
            assertThat(r.getUsername()).isEqualTo("user");
            assertThat(r.getEmail()).isEqualTo("e@e.com");
            assertThat(r.getMensaje()).isEqualTo("ok");
        }

        @Test
        @DisplayName("NoArgsConstructor crea objeto con todos los campos null")
        void noArgs_allNull() {
            AuthResponse r = new AuthResponse();
            assertThat(r.getToken()).isNull();
            assertThat(r.getEmail()).isNull();
            assertThat(r.getMensaje()).isNull();
        }

        @Test
        @DisplayName("AllArgsConstructor asigna valores en orden")
        void allArgs() {
            AuthResponse r = new AuthResponse("t", "u", "e@e.com", "msg");
            assertThat(r.getToken()).isEqualTo("t");
            assertThat(r.getMensaje()).isEqualTo("msg");
        }

        @Test
        @DisplayName("Setters funcionan correctamente")
        void setters() {
            AuthResponse r = new AuthResponse();
            r.setToken("newToken");
            r.setEmail("new@e.com");
            r.setUsername("newUser");
            r.setMensaje("newMsg");

            assertThat(r.getToken()).isEqualTo("newToken");
            assertThat(r.getEmail()).isEqualTo("new@e.com");
        }

        @Test
        @DisplayName("equals y hashCode basados en el mismo contenido")
        void equalsAndHashCode() {
            AuthResponse r1 = AuthResponse.builder().token("t").email("e@e.com").build();
            AuthResponse r2 = AuthResponse.builder().token("t").email("e@e.com").build();
            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("toString no lanza excepción")
        void toStringWorks() {
            assertThat(AuthResponse.builder().token("t").build().toString()).isNotNull();
        }
    }

    @Nested
    @DisplayName("RegisterRequest")
    class RegisterRequestTests {

        @Test
        @DisplayName("NoArgsConstructor + setters funcionan")
        void noArgsAndSetters() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("r@r.com");
            req.setPassword("pass");
            req.setNombre("Rosa");

            assertThat(req.getEmail()).isEqualTo("r@r.com");
            assertThat(req.getPassword()).isEqualTo("pass");
            assertThat(req.getNombre()).isEqualTo("Rosa");
        }

        @Test
        @DisplayName("equals basado en el mismo contenido")
        void equalsWorks() {
            RegisterRequest r1 = new RegisterRequest();
            r1.setEmail("a@a.com"); r1.setPassword("p"); r1.setNombre("A");

            RegisterRequest r2 = new RegisterRequest();
            r2.setEmail("a@a.com"); r2.setPassword("p"); r2.setNombre("A");

            assertThat(r1).isEqualTo(r2);
        }

        @Test
        @DisplayName("toString contiene el email")
        void toStringContainsEmail() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("x@x.com");
            assertThat(req.toString()).contains("x@x.com");
        }
    }

    @Nested
    @DisplayName("LoginRequest")
    class LoginRequestTests {

        @Test
        @DisplayName("NoArgsConstructor + setters funcionan")
        void noArgsAndSetters() {
            LoginRequest req = new LoginRequest();
            req.setEmail("l@l.com");
            req.setUsername("luser");
            req.setPassword("pw");

            assertThat(req.getEmail()).isEqualTo("l@l.com");
            assertThat(req.getUsername()).isEqualTo("luser");
            assertThat(req.getPassword()).isEqualTo("pw");
        }

        @Test
        @DisplayName("email y username son campos independientes")
        void emailAndUsernameIndependent() {
            LoginRequest req = new LoginRequest();
            req.setEmail("primary@e.com");
            req.setUsername("fallback@e.com");

            assertThat(req.getEmail()).isNotEqualTo(req.getUsername());
        }

        @Test
        @DisplayName("equals basado en el mismo contenido")
        void equalsWorks() {
            LoginRequest r1 = new LoginRequest();
            r1.setEmail("x@x.com"); r1.setPassword("p");

            LoginRequest r2 = new LoginRequest();
            r2.setEmail("x@x.com"); r2.setPassword("p");

            assertThat(r1).isEqualTo(r2);
        }

        @Test
        @DisplayName("toString no lanza excepción")
        void toStringWorks() {
            LoginRequest req = new LoginRequest();
            req.setEmail("t@t.com");
            assertThat(req.toString()).isNotNull();
        }
    }
}