# Spring Security Basics - Sistema de Gestión de Reciclaje

Este proyecto es un sistema de gestión de reciclaje que utiliza Spring Security para la autenticación y autorización, diseñado para facilitar la gestión de solicitudes de reciclaje y la administración de roles de usuario.

## Propósito del Sistema
El sistema está diseñado para simplificar la gestión de reciclaje mediante la automatización de procesos de solicitud y la administración de usuarios, permitiendo a las organizaciones de reciclaje y administradores gestionar eficientemente sus operaciones.

## Características Principales

### Roles y Permisos
- **Usuario**: Acceso a funciones básicas y gestión de sus propias solicitudes de reciclaje.
- **Administrador**: Control total sobre el sistema, incluyendo la gestión de usuarios y solicitudes.
- **Organización**: Funcionalidades específicas para entidades de reciclaje.
- **Invitado**: Acceso limitado a información pública.

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
│       │   ├── OrgController.java           # Gestión de organizaciones
│       │   ├── SolicitudeController.java    # Gestión de solicitudes de reciclaje
│       │   └── UserController.java          # Gestión de perfiles y acciones de usuario
│       │
│       ├── dto/                         # Objetos de transferencia de datos
│       │   ├── UserDto.java                 # DTO para transferencia de datos de usuario
│       │   └── SolicitudeDto.java           # DTO para transferencia de datos de solicitudes
│       │
│       ├── entity/                      # Entidades JPA
│       │   ├── Role.java                    # Entidad de roles
│       │   ├── User.java                    # Entidad de usuarios
│       │   └── Solicitude.java              # Entidad de solicitudes de reciclaje
│       │
│       ├── repository/                  # Repositorios de acceso a datos
│       │   ├── RoleRepository.java          # Repositorio para roles
│       │   ├── UserRepository.java          # Repositorio para usuarios
│       │   └── SolicitudeRepository.java    # Repositorio para solicitudes
│       │
│       ├── service/                     # Interfaces de servicios
│       │   ├── UserService.java             # Interfaz para gestión de usuarios
│       │   ├── SolicitudeService.java       # Interfaz para gestión de solicitudes
│       │   ├── FileStorageService.java      # Interfaz para almacenamiento de archivos
│       │   ├── RoleManagementService.java   # Interfaz para gestión de roles
│       │   └── ValidationAndNotificationService.java # Interfaz para validación y notificaciones
│       │
│       └── service/impl/                # Implementaciones de servicios
│           ├── UserServiceImpl.java         # Implementación del servicio de usuarios
│           ├── SolicitudeServiceImpl.java   # Implementación del servicio de solicitudes
│           ├── FileStorageServiceImpl.java  # Implementación para almacenamiento de archivos
│           ├── RoleManagementServiceImpl.java # Implementación de gestión de roles
│           └── ValidationAndNotificationServiceImpl.java # Implementación centralizada de validación
└── resources/
    ├── static/                      # Recursos estáticos
    │   ├── css/                         # Hojas de estilo
    │   │   ├── bootstrap.min.css        # Framework Bootstrap
    │   │   └── styles.css               # Estilos personalizados
    │   ├── js/                          # JavaScript
    │   └── img/                         # Imágenes y recursos gráficos
    │
    ├── templates/                   # Plantillas Thymeleaf
    │   ├── admin/                       # Vistas para administradores
    │   ├── auth/                        # Vistas de autenticación
    │   ├── error/                       # Páginas de error
    │   ├── fragments/                   # Fragmentos reutilizables
    │   │   ├── header.html                 # Header unificado adaptable según roles
    │   │   ├── layout/                     # Componentes de layout
    │   │   │   ├── page_layout.html           # Layout principal de páginas
    │   │   │   ├── head.html                  # Fragment de head HTML
    │   │   │   ├── footer.html                # Footer común
    │   │   │   └── breadcrumb.html            # Navegación de migas de pan
    │   │   └── messages.html               # Mensajes de sistema (errores/éxito)
    │   ├── user/                        # Vistas para usuarios normales
    │   └── org/                         # Vistas para organizaciones
    │
    └── application.properties       # Configuración de la aplicación
```

### Arquitectura Frontend
- **Diseño Modular**: Los componentes frontend están organizados en fragmentos Thymeleaf reutilizables.
- **Header Unificado**: Un único componente de header que adapta su contenido según el rol del usuario.
- **Responsive Design**: Uso de Bootstrap 5 para garantizar compatibilidad con diferentes dispositivos.

### Arquitectura Backend
- **Servicios Consolidados**: Implementación de servicios que agrupan funcionalidades relacionadas:
  - `ValidationAndNotificationService`: Centraliza la validación y notificaciones del sistema.
  - `RoleManagementService`: Gestiona roles y permisos de forma unificada.
  - `FileStorageService`: Maneja todas las operaciones relacionadas con archivos.
  
- **Seguridad por Capas**: 
  - Autenticación basada en formularios.
  - Autorización basada en roles con Spring Security.
  - Validación y sanitización de entradas.

## Requisitos del Sistema
- Java 11 o superior
- Maven 3.6+
- Base de datos MySQL o H2

## Instrucciones de Arranque
1. Clonar el repositorio.
2. Configurar la base de datos en `application.properties`.
3. Ejecutar `mvn spring-boot:run` para iniciar la aplicación.

Este README proporciona una visión general del sistema, destacando su arquitectura y funcionalidades clave, con un enfoque en la simplicidad y eficiencia en la gestión de reciclaje.
