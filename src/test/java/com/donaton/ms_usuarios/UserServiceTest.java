package com.donaton.ms_usuarios;

import com.donaton.ms_usuarios.dto.AuthResponse;
import com.donaton.ms_usuarios.dto.LoginRequest;
import com.donaton.ms_usuarios.dto.RegisterRequest;
import com.donaton.ms_usuarios.model.User;
import com.donaton.ms_usuarios.repository.UserRepository;
import com.donaton.ms_usuarios.security.JwtUtil;
import com.donaton.ms_usuarios.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("plain123");
        registerRequest.setNombre("Juan Pérez");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("plain123");

        savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashed123")
                .nombre("Juan Pérez")
                .build();
    }

    // ===========================================================================
    // register()
    // ===========================================================================

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Registro exitoso: retorna token, email, nombre y mensaje")
        void register_success_returnsAuthResponse() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plain123")).thenReturn("hashed123");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt.token.mock");

            AuthResponse response = userService.register(registerRequest);

            assertThat(response.getToken()).isEqualTo("jwt.token.mock");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getUsername()).isEqualTo("Juan Pérez");
            assertThat(response.getMensaje()).isEqualTo("Registro exitoso");
        }

        @Test
        @DisplayName("Registro exitoso: la contraseña se guarda hasheada, no en texto plano")
        void register_success_passwordIsEncoded() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plain123")).thenReturn("hashed123");
            when(jwtUtil.generateToken(any())).thenReturn("token");

            userService.register(registerRequest);

            verify(passwordEncoder).encode("plain123");
            // Verifica que el usuario guardado tiene la contraseña hasheada
            verify(userRepository).save(argThat(user ->
                    "hashed123".equals(user.getPassword())
            ));
        }

        @Test
        @DisplayName("Registro exitoso: se llama a save() y generateToken() exactamente una vez")
        void register_success_callsRepositoryAndJwt() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(jwtUtil.generateToken(any())).thenReturn("token");

            userService.register(registerRequest);

            verify(userRepository, times(1)).save(any(User.class));
            verify(jwtUtil, times(1)).generateToken("test@example.com");
        }

        @Test
        @DisplayName("Registro con nombre nulo: nombre se guarda como cadena vacía")
        void register_nullNombre_savesEmptyString() {
            registerRequest.setNombre(null);

            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(jwtUtil.generateToken(any())).thenReturn("token");

            userService.register(registerRequest);

            verify(userRepository).save(argThat(user -> "".equals(user.getNombre())));
        }

        @Test
        @DisplayName("Email duplicado: lanza EmailAlreadyExistsException")
        void register_emailAlreadyExists_throwsException() {
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(savedUser));

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(UserService.EmailAlreadyExistsException.class)
                    .hasMessage("El email ya está registrado");

            // No debe guardar ni generar token
            verify(userRepository, never()).save(any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Email nulo: lanza IllegalArgumentException")
        void register_nullEmail_throwsIllegalArgument() {
            registerRequest.setEmail(null);

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email y password son obligatorios");

            verifyNoInteractions(userRepository, passwordEncoder, jwtUtil);
        }

        @Test
        @DisplayName("Password nulo: lanza IllegalArgumentException")
        void register_nullPassword_throwsIllegalArgument() {
            registerRequest.setPassword(null);

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email y password son obligatorios");

            verifyNoInteractions(userRepository, passwordEncoder, jwtUtil);
        }

        @Test
        @DisplayName("Email y password nulos: lanza IllegalArgumentException")
        void register_nullEmailAndPassword_throwsIllegalArgument() {
            registerRequest.setEmail(null);
            registerRequest.setPassword(null);

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ===========================================================================
    // login()
    // ===========================================================================

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Login exitoso por email: retorna token y datos del usuario")
        void login_successWithEmail_returnsAuthResponse() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches("plain123", "hashed123")).thenReturn(true);
            when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt.token.mock");

            AuthResponse response = userService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt.token.mock");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getUsername()).isEqualTo("Juan Pérez");
            // El login no devuelve mensaje, debe ser null (JsonInclude.NON_NULL lo omite)
            assertThat(response.getMensaje()).isNull();
        }

        @Test
        @DisplayName("Login exitoso por username (fallback): resuelve email correctamente")
        void login_successWithUsername_usesUsernameAsEmail() {
            loginRequest.setEmail(null);
            loginRequest.setUsername("test@example.com");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(jwtUtil.generateToken(any())).thenReturn("token");

            AuthResponse response = userService.login(loginRequest);

            assertThat(response.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("Email no registrado: lanza InvalidCredentialsException")
        void login_userNotFound_throwsInvalidCredentials() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(UserService.InvalidCredentialsException.class)
                    .hasMessage("Credenciales inválidas");

            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Contraseña incorrecta: lanza InvalidCredentialsException")
        void login_wrongPassword_throwsInvalidCredentials() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches("plain123", "hashed123")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(loginRequest))
                    .isInstanceOf(UserService.InvalidCredentialsException.class)
                    .hasMessage("Credenciales inválidas");

            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Login exitoso: genera token con el email del usuario, no del request")
        void login_success_generatesTokenWithUserEmail() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches(any(), any())).thenReturn(true);
            when(jwtUtil.generateToken("test@example.com")).thenReturn("token");

            userService.login(loginRequest);

            // El token debe generarse con el email que tiene el User en BD
            verify(jwtUtil).generateToken("test@example.com");
        }
    }
}