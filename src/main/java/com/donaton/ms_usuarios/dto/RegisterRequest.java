package com.donaton.ms_usuarios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String nombre;
}
