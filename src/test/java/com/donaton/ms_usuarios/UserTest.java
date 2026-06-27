package com.donaton.ms_usuarios;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.donaton.ms_usuarios.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User model")
class UserTest {

    @Test
    @DisplayName("Builder crea un User con todos los campos correctamente")
    void builder_allFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .password("hash")
                .nombre("Ana")
                .createdAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("a@b.com");
        assertThat(user.getPassword()).isEqualTo("hash");
        assertThat(user.getNombre()).isEqualTo("Ana");
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("NoArgsConstructor + setters funcionan correctamente")
    void noArgsConstructor_andSetters() {
        User user = new User();
        user.setId(2L);
        user.setEmail("b@c.com");
        user.setPassword("pw");
        user.setNombre("Luis");

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getEmail()).isEqualTo("b@c.com");
        assertThat(user.getPassword()).isEqualTo("pw");
        assertThat(user.getNombre()).isEqualTo("Luis");
    }

    @Test
    @DisplayName("AllArgsConstructor asigna todos los campos")
    void allArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(3L, "c@d.com", "hash3", "Carlos", now);

        assertThat(user.getId()).isEqualTo(3L);
        assertThat(user.getEmail()).isEqualTo("c@d.com");
        assertThat(user.getNombre()).isEqualTo("Carlos");
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals y hashCode basados en los mismos datos")
    void equalsAndHashCode() {
        User u1 = User.builder().id(1L).email("x@y.com").password("p").nombre("X").build();
        User u2 = User.builder().id(1L).email("x@y.com").password("p").nombre("X").build();

        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }

    @Test
    @DisplayName("toString no lanza excepción y contiene el email")
    void toString_containsEmail() {
        User user = User.builder().id(1L).email("test@test.com").password("p").nombre("T").build();
        assertThat(user.toString()).contains("test@test.com");
    }

    @Test
    @DisplayName("Dos usuarios con distinto id no son iguales")
    void notEqual_differentId() {
        User u1 = User.builder().id(1L).email("a@a.com").password("p").nombre("A").build();
        User u2 = User.builder().id(2L).email("a@a.com").password("p").nombre("A").build();
        assertThat(u1).isNotEqualTo(u2);
    }
}