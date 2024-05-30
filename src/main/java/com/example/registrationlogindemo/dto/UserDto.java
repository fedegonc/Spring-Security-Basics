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
    @NotEmpty(message = "{user.firstName.notEmpty}")
    private String firstName; // Nombre del usuario
    @NotEmpty(message = "{user.lastName.notEmpty}")
    private String lastName; // Apellido del usuario
    @NotEmpty(message = "{user.email.notEmpty}")
    @Email(message = "{user.email.invalid}")
    private String email; // Email del usuario, debe cumplir el formato de email
    @NotEmpty(message = "{user.password.notEmpty}")
    private String password; // Contraseña del usuario
    private String roleName; // Rol del usuario
    @NotEmpty(message = "{user.username.notEmpty}")
    private String username; // Nombre de usuario del usuario
}
