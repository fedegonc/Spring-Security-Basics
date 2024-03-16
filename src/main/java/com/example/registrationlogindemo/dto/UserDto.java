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
public class UserDto
{


    private Long id;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty(message = "El email no puede estar vacío")
    @Email
    private String email;
    @NotEmpty(message = "La contraseña no puede estar vacía")
    private String password;

    private String roleName;
    public String getRoleName() {
        return roleName;
    }
}
