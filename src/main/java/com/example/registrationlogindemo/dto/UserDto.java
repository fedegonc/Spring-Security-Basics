package com.example.registrationlogindemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    // Atributos de la clase UserDto
    private Long id; // ID del usuario
    @NotEmpty
    private String firstName; // Nombre del usuario
    @NotEmpty
    private String lastName; // Apellido del usuario
    @NotEmpty(message = "El email no puede estar vacío")
    @Email
    private String email; // Email del usuario, debe cumplir el formato de email
    @NotEmpty(message = "La contraseña no puede estar vacía")
    private String password; // Contraseña del usuario
    private String roleName; // Rol del usuario
}
