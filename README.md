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

## Frontend: Estructura y Organización

### Estructura de Templates
```
templates/
├── admin/                        # Vistas para administradores
│   ├── crear-usuario.html        # Formulario de creación de usuarios
│   ├── dashboard.html            # Panel principal de administración
│   ├── edit.html                 # Edición de usuarios
│   ├── editreport.html           # Edición de reportes
│   ├── editsolicitude.html       # Edición de solicitudes
│   ├── newreport.html            # Creación de reportes
│   ├── newsolicitude.html        # Creación de solicitudes
│   ├── profile.html              # Perfil de administrador
│   ├── reports.html              # Listado de reportes
│   ├── solicitudes.html          # Listado de solicitudes
│   └── users.html                # Gestión de usuarios
│
├── error.html                    # Página de error genérica
│
├── fragments/                    # Componentes reutilizables
│   ├── header-breadcrumbs.html   # Navegación de migas de pan
│   ├── header-logo-nav.html      # Logo y navegación principal
│   ├── header-mobile.html        # Menú móvil
│   ├── header-user-lang.html     # Menú de usuario e idioma
│   ├── header.html               # Cabecera principal (combina los anteriores)
│   ├── layout/                   # Componentes de layout
│   │   ├── body.html             # Estructura del cuerpo
│   │   ├── breadcrumb.html       # Navegación de migas de pan alternativa
│   │   ├── config-options.html   # Opciones de configuración
│   │   └── footer.html           # Pie de página
│   ├── logo.html                 # Logo de la aplicación
│   ├── messages.html             # Mensajes del sistema
│   ├── shared/                   # Layouts compartidos
│   │   └── layout.html           # Layout principal de la aplicación
│   ├── solicitudes/              # Componentes de solicitudes
│   │   ├── dialogo.html          # Diálogos de solicitudes
│   │   ├── edit-solicitud.html   # Formulario de edición
│   │   ├── formulario-solicitud.html # Formulario de creación
│   │   ├── metricas-solicitudes.html # Métricas y estadísticas
│   │   └── vista-solicitudes.html # Vista de listado
│   └── user/                     # Componentes de usuario
│       ├── estadisticas-user.html # Estadísticas de usuario
│       ├── metricas-user.html    # Métricas de usuario
│       ├── profile-form.html     # Formulario de perfil
│       ├── publicaciones-user.html # Publicaciones de usuario
│       ├── solicitudes-tarjetas.html # Tarjetas de solicitudes
│       └── usuario-bienvenida.html # Mensaje de bienvenida
│
├── guest/                        # Vistas para invitados
│   ├── error.html                # Página de error para invitados
│   ├── index.html                # Página principal para invitados
│   ├── login.html                # Página de inicio de sesión
│   └── register.html             # Página de registro
│
├── org/                          # Vistas para organizaciones
│   ├── dashboard.html            # Panel principal de organización
│   └── profile.html              # Perfil de organización
│
├── solicitude/                   # Vistas de solicitudes
│   ├── editsolicitude.html       # Edición de solicitudes
│   └── newsolicitude.html        # Creación de solicitudes
│
└── user/                         # Vistas para usuarios normales
    ├── profile.html              # Perfil de usuario
    ├── report-problem.html       # Reporte de problemas
    ├── view-requests.html        # Visualización de solicitudes
    └── welcome.html              # Página de bienvenida
```

### Patrones de Diseño Frontend

#### Sistema de Layouts
La aplicación utiliza un sistema de layouts basado en Thymeleaf que permite:
- **Reutilización de código**: Todos los templates extienden de un layout base (`fragments/shared/layout.html`)
- **Composición de componentes**: Uso de fragmentos para construir interfaces complejas
- **Consistencia visual**: Estructura unificada en todas las páginas

#### Fragmentos Principales
1. **Header**: Sistema modular compuesto por:
   - `header.html`: Contenedor principal que orquesta los demás fragmentos
   - `header-logo-nav.html`: Logo y navegación principal
   - `header-user-lang.html`: Menú de usuario e idioma
   - `header-mobile.html`: Menú para dispositivos móviles
   - `header-breadcrumbs.html`: Sistema de navegación por migas de pan

2. **Componentes de Solicitudes**:
   - Formularios reutilizables para creación y edición
   - Vistas de listado y detalle estandarizadas
   - Componentes de métricas y estadísticas

3. **Componentes de Usuario**:
   - Formularios de perfil
   - Tarjetas de estadísticas
   - Elementos de bienvenida

#### Estrategia de Estilos
- **TailwindCSS**: Framework principal para estilos
- **Componentes personalizados**: Clases específicas para mantener consistencia
- **Diseño responsive**: Adaptación a diferentes tamaños de pantalla

#### Optimizaciones Implementadas
- **Reducción de duplicación**: Consolidación de fragmentos comunes
- **Simplificación de layouts**: Estructura clara y mantenible
- **Mejora de accesibilidad**: Implementación de elementos como skip links

#### Áreas de Mejora
- Consolidar fragmentos redundantes (especialmente en cabeceras)
- Estandarizar la estructura de componentes entre diferentes roles
- Mejorar la reutilización de código JavaScript

## Mapeo de Endpoints y Plantillas Thymeleaf

### Controladores y Endpoints

#### AuthController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/error` | Página de error |
| GET | `/register` | Formulario de registro |
| POST | `/register/save` | Procesamiento de registro |
| GET | `/login` | Página de inicio de sesión |
| GET | `/init` | Redirección inicial según el rol del usuario |

#### GuestController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/`, `/index` | Página principal pública |
| GET | `/ambiental` | Información ambiental |
| GET | `/report` | Formulario de reporte para invitados |
| POST | `/report` | Procesamiento de reportes de invitados |

#### UserController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/user/welcome` | Dashboard del usuario |
| GET/POST | `/user/profile` | Ver/actualizar perfil de usuario |
| GET | `/user/delet/{id}` | Eliminar usuario |
| GET | `/user/logout` | Cerrar sesión |
| GET | `/user/users` | Listar usuarios (para usuarios con permisos) |
| GET | `/user/construction`, etc. | Páginas estáticas en construcción |
| GET | `/user/view-requests` | Ver solicitudes del usuario |
| POST | `/user/updatesolicitude/{id}` | Actualizar una solicitud |
| GET | `/user/report-problem` | Formulario para reportar problemas |
| POST | `/user/report` | Enviar reporte de problema |
| POST | `/user/change-password` | Cambiar contraseña |

#### SolicitudeController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/user/newsolicitude` | Nueva solicitud |
| POST | `/user/newsolicitude` | Procesar nueva solicitud |
| GET | `/user/editsolicitude/{id}` | Editar solicitud existente |
| POST | `/user/editsolicitude/{id}` | Actualizar solicitud existente |
| POST | `/user/solicitude/{id}/messages` | Enviar mensaje en una solicitud |
| GET | `/user/deletesolicitude/{id}` | Eliminar solicitud |

#### OrgController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/org/dashboard` | Panel de organización |
| GET | `/org/solicitudes` | Ver solicitudes de la organización |
| GET | `/org/editsolicitude/{id}` | Editar solicitud |
| GET | `/org/deletsolicitude/{id}` | Eliminar solicitud |
| POST | `/org/editsolicitude/{id}` | Procesar edición de solicitud |
| GET | `/org/profile` | Ver perfil de organización |
| POST | `/org/profile` | Actualizar perfil de organización |
| POST | `/org/change-password` | Cambiar contraseña |

#### AdministradorController
| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/admin/dashboard` | Panel de administración |
| GET | `/admin/profile` | Ver perfil de administrador |
| GET | `/admin/edit/{id}` | Editar usuario |
| POST | `/admin/edit/{id}` | Actualizar usuario |
| GET | `/admin/reports` | Ver reportes |
| GET | `/admin/report/{id}` | Ver reporte específico |
| POST | `/admin/report/{id}` | Actualizar reporte |
| GET | `/admin/solicitudes` | Ver solicitudes |
| GET | `/admin/solicitudes/edit/{id}` | Editar solicitud |
| POST | `/admin/solicitudes/edit/{id}` | Actualizar solicitud |
| GET | `/admin/solicitudes/delete/{id}` | Eliminar solicitud |
| GET | `/admin/users` | Gestionar usuarios |

### Plantillas Thymeleaf

#### Plantillas Principales
| Carpeta | Plantilla | Descripción |
|---------|-----------|-------------|
| `/` | `index.html` | Página de inicio |
| `guest/` | `index.html`, `login.html`, `register.html` | Páginas para invitados |
| `user/` | `profile.html`, `report-problem.html`, etc. | Páginas para usuarios normales |
| `org/` | `dashboard.html`, `profile.html` | Páginas para organizaciones |
| `admin/` | `dashboard.html`, `users.html`, etc. | Páginas administrativas |
| `solicitude/` | `newsolicitude.html`, `editsolicitude.html` | Plantillas de solicitudes |
| `fragments/` | `header.html`, `messages.html`, etc. | Componentes reutilizables |

#### Fragmentos Reutilizables
| Carpeta | Fragmento | Descripción |
|---------|-----------|-------------|
| `fragments/layout/` | `head.html`, `footer.html`, etc. | Componentes estructurales |
| `fragments/headers/` | `admin_header.html`, `user_header.html`, etc. | Cabeceras específicas por rol |
| `fragments/user/` | `navbar-user.html`, `profile-form.html`, etc. | Componentes de usuario |
| `fragments/solicitudes/` | `formulario-solicitud.html`, `vista-solicitudes.html`, etc. | Componentes de solicitudes |

## Observaciones de la Arquitectura

- **Redundancia de Funcionalidad**: Existe duplicación de endpoints similares en diferentes controladores (ej. cambio de contraseña).
- **Fragmentación Excesiva**: La separación por roles crea muchas plantillas y controladores similares.
- **Oportunidades de Consolidación**: Potencial para unificar funcionalidades comunes como gestión de perfiles y contraseñas.

### Propuestas de Simplificación (Futuras)

1. **Unificar controladores por funcionalidad** en lugar de por rol
2. **Consolidar fragmentos comunes** entre los diferentes tipos de usuarios
3. **Estandarizar la estructura de endpoints** para acciones similares

## Requisitos del Sistema
- Java 11 o superior
- Maven 3.6+
- Base de datos MySQL o H2

## Instrucciones de Arranque
1. Clonar el repositorio.
2. Configurar la base de datos en `application.properties`.
3. Ejecutar `mvn spring-boot:run` para iniciar la aplicación.

Este README proporciona una visión general del sistema, destacando su arquitectura y funcionalidades clave, con un enfoque en la simplicidad y eficiencia en la gestión de reciclaje.
