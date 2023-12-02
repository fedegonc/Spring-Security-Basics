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
public class LivroDto
{
    private Long id;
    @NotEmpty
    private String titulo;
    @NotEmpty
    private String genero;
    @NotEmpty(message = "Email should not be empty")
    @Email
    private String autor;
    @NotEmpty(message = "Password should not be empty")
    private String preco;

    private String resumo;

    private String imagem;
}
