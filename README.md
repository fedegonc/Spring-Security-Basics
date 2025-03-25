# Spring Security Basics - Sistema de Gestión de Reciclaje

Este proyecto es un sistema de gestión de reciclaje que utiliza Spring Security para la autenticación y autorización, diseñado para facilitar la gestión de solicitudes de reciclaje y la administración de roles de usuario.

## Propósito del Sistema
El sistema está diseñado para simplificar la gestión de reciclaje mediante la automatización de procesos de solicitud y la administración de usuarios, permitiendo a las organizaciones de reciclaje y administradores gestionar eficientemente sus operaciones.

## Características Principales

### Roles y Permisos
- **Usuario**: Acceso a funciones básicas y gestión de sus propias solicitudes de reciclaje.
- **Administrador**: Control total sobre el sistema, incluyendo la gestión de usuarios y solicitudes.

### Tipos de Organización
- Centro de acopio
- Empresa
- Institución de reciclaje

### Funcionalidades
- Registro y autenticación de usuarios
- Gestión de solicitudes de reciclaje
- Panel de administración
- Gestión de perfiles de usuario
- Sistema de reportes

## Tecnologías
- Spring Boot
- Spring Security
- Thymeleaf
- Bootstrap 5
- MySQL/H2

## Principios de Diseño
- **KISS**: Manteniendo la simplicidad en la implementación
- **DRY**: Reutilización de componentes y fragmentos Thymeleaf
- **SOLID**: Estructura modular y responsabilidades bien definidas

## Arquitectura del Sistema
El sistema sigue el patrón MVC (Modelo-Vista-Controlador), con una estructura organizada para facilitar el mantenimiento y la escalabilidad.

### Estructura del Proyecto
```
src/main/
├── java/
│   └── com.example.registrationlogindemo/
│       ├── config/                      # Configuración de seguridad y aplicación
│       │   ├── GlobalControllerAdvice.java  # Manejo global de excepciones y atributos comunes
│       │   ├── SpringSecurity.java          # Configuración de autenticación y autorización
│       │   ├── UserStatusService.java       # Servicio para gestionar estados de usuario
│       │   └── WebConfig.java               # Configuración web general
│       │
│       ├── controller/                  # Controladores MVC
│       │   ├── AuthController.java          # Gestión de autenticación y registro
│       │   ├── GuestController.java         # Páginas públicas para visitantes
│       │   ├── AdministradorController.java # Panel y funciones administrativas
│       │   ├── OrganizationController.java  # Gestión de organizaciones
│       │   ├── SolicitudeController.java    # Gestión de solicitudes de reciclaje
│       └── service/                     # Lógica de negocio
│           ├── UserService.java             # Servicio para gestión de usuarios
│           ├── SolicitudeService.java       # Servicio para gestión de solicitudes
│           └── OrganizationService.java     # Servicio para gestión de organizaciones
└── resources/
    ├── templates/                   # Plantillas Thymeleaf
    ├── static/                      # Recursos estáticos
    │   ├── css/                         # Hojas de estilo
    │   │   ├── bootstrap.min.css        # Framework Bootstrap
    │   │   └── styles.css               # Estilos personalizados
```

## Requisitos del Sistema
- Java 11 o superior
- Maven 3.6+
- Base de datos MySQL o H2

## Instrucciones de Arranque
1. Clonar el repositorio.
2. Configurar la base de datos en `application.properties`.
3. Ejecutar `mvn spring-boot:run` para iniciar la aplicación.

Este README proporciona una visión general del sistema, destacando su arquitectura y funcionalidades clave, con un enfoque en la simplicidad y eficiencia en la gestión de reciclaje.
