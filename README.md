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
│       │   └── UserController.java          # Perfil, funciones de usuario y dashboard de organización
│       │
│       ├── dto/                         # Objetos de transferencia de datos
│       │   └── UserDto.java                 # DTO para transferencia de datos de usuario
│       │
│       ├── entity/                      # Entidades JPA (modelo de datos)
│       │   ├── Message.java                 # Mensajes del sistema
│       │   ├── Organization.java            # Organizaciones de reciclaje (incluye tipo como enum)
│       │   ├── Report.java                  # Reportes y estadísticas
│       │   ├── Role.java                    # Roles de usuario
│       │   ├── Solicitude.java              # Solicitudes de reciclaje
│       │   └── User.java                    # Usuarios del sistema
│       │
│       ├── repository/                  # Interfaces de repositorio para acceso a datos
│       │   ├── MessageRepository.java       # Repositorio para mensajes
│       │   ├── OrganizationRepository.java  # Repositorio para organizaciones
│       │   ├── ReportRepository.java        # Repositorio para reportes
│       │   ├── RoleRepository.java          # Repositorio para roles
│       │   ├── SolicitudeRepository.java    # Repositorio para solicitudes
│       │   └── UserRepository.java          # Repositorio para usuarios
│       │
│       └── service/                     # Servicios de negocio
│           ├── MessageService.java          # Interfaz para servicio de mensajes
│           ├── OrganizationService.java     # Interfaz para servicio de organizaciones
│           ├── ReportService.java           # Interfaz para servicio de reportes
│           ├── SolicitudeService.java       # Interfaz para servicio de solicitudes
│           ├── UserService.java             # Interfaz para servicio de usuarios
│           └── impl/                        # Implementaciones de servicios
│               ├── MessageServiceImpl.java      # Implementación de servicio de mensajes
│               ├── OrganizationServiceImpl.java # Implementación de servicio de organizaciones
│               ├── ReportServiceImpl.java       # Implementación de servicio de reportes
│               ├── SolicitudeServiceImpl.java   # Implementación de servicio de solicitudes
│               ├── UserDetailsServiceImpl.java  # Implementación de UserDetailsService para Spring Security
│               └── UserServiceImpl.java         # Implementación de servicio de usuarios
│
└── resources/
    ├── static/                      # Recursos estáticos
    │   ├── css/                         # Hojas de estilo
    │   │   ├── bootstrap.min.css        # Framework Bootstrap
    │   │   └── styles.css               # Estilos personalizados
    │   ├── img/                         # Imágenes y recursos gráficos
    │   │   ├── logo.png                 # Logo del sistema
    │   │   └── default-profile.jpg      # Imagen de perfil por defecto
    │   ├── js/                          # Scripts JavaScript
    │   │   ├── bootstrap.bundle.min.js  # JavaScript de Bootstrap
    │   │   └── adminScript.js           # Scripts para el panel de administración
    │   └── scripts/                     # Scripts adicionales
    │
    ├── templates/                   # Plantillas Thymeleaf organizadas por roles
    │   ├── admin/                       # Vistas para administradores
    │   │   ├── dashboard.html           # Panel principal de administración
    │   │   ├── editreport.html          # Edición de reportes
    │   │   ├── editsolicitude.html      # Edición de solicitudes
    │   │   ├── reports.html             # Gestión de reportes
    │   │   └── solicitudes.html         # Gestión de solicitudes
    │   │
    │   ├── fragments/                   # Fragmentos reutilizables
    │   │   ├── admin/                   # Fragmentos específicos para admin
    │   │   ├── guest/                   # Fragmentos para visitantes
    │   │   ├── layout/                  # Fragmentos de estructura (head, footer)
    │   │   │   ├── footer.html          # Pie de página común
    │   │   │   └── head.html            # Encabezado HTML común
    │   │   ├── navbar/                  # Barras de navegación
    │   │   ├── organization/            # Fragmentos para organizaciones
    │   │   │   └── navbar-organization.html # Barra de navegación para organizaciones
    │   │   ├── solicitudes/             # Fragmentos para solicitudes
    │   │   └── user/                    # Fragmentos para usuarios
    │   │
    │   ├── guest/                       # Vistas públicas
    │   │   ├── index.html               # Página principal
    │   │   ├── login.html               # Página de inicio de sesión
    │   │   └── register.html            # Página de registro
    │   │
    │   ├── organization/                # Vistas para organizaciones
    │   │   └── dashboard.html           # Panel de organización
    │   │
    │   ├── solicitude/                  # Vistas para solicitudes
    │   │   ├── newsolicitude.html       # Crear nueva solicitud
    │   │   └── view-requests.html       # Ver solicitudes
    │   │
    │   ├── user/                        # Vistas para usuarios
    │   │   ├── profile.html             # Perfil de usuario
    │   │   └── welcome.html             # Página de bienvenida
    │   │
    │   ├── error.html                   # Página de error
    │   └── messages.html                # Plantilla para mensajes del sistema
    │
    └── application.properties       # Configuración de la aplicación
