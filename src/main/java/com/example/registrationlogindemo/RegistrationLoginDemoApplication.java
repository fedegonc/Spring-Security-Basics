package com.example.registrationlogindemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RegistrationLoginDemoApplication {

	public static void main(String[] args) {
		// @SpringBootApplication: Esta anotación combina tres anotaciones:
		// @Configuration, @EnableAutoConfiguration y @ComponentScan.
		// Marca esta clase como una clase de configuración de Spring,
		// habilita la configuración automática de Spring Boot y escanea
		// los paquetes para encontrar componentes y configuración.
		// pasando la clase principal RegistrationLoginDemoApplication y los argumentos
		// de línea de comandos como parámetros.

		SpringApplication.run(RegistrationLoginDemoApplication.class, args);
	}
}
