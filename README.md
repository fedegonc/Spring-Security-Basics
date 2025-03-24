# Spring Security Basics - Sistema de Gestión de Reciclaje

Sistema de gestión de reciclaje con autenticación y autorización basado en Spring Security.

## Características Principales

### Roles y Permisos
- **Usuario**: Acceso a funciones básicas y gestión de solicitudes propias
- **Administrador**: Gestión completa del sistema, usuarios y solicitudes

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

## Estructura del Proyecto
```
src/main/
├── java/
│   └── com.example.registrationlogindemo/
│       ├── config/          # Configuración de seguridad
│       ├── controller/      # Controladores MVC
│       ├── entity/          # Entidades JPA
│       ├── repository/      # Repositorios
│       └── service/         # Servicios
└── resources/
    ├── static/             # Recursos estáticos (CSS, JS)
    └── templates/          # Plantillas Thymeleaf
```
