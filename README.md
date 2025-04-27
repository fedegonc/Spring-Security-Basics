# Sistema de Comunicación para Reciclaje - EcoSolicitud

Este proyecto es un sistema de comunicación que conecta a la comunidad con los centros de acopio de materiales reciclables, facilitando el proceso de reciclaje y contribuyendo a la sostenibilidad ambiental.

## Propósito del Sistema

Facilitar la comunicación entre ciudadanos y centros de reciclaje, permitiendo a los usuarios registrar solicitudes de recolección de materiales reciclables, con una gestión eficiente por parte de las organizaciones participantes.

## Características Principales

- **Sistema de roles**: Usuarios (ciudadanos), Organizaciones (centros de reciclaje) y Administradores
- **Solicitudes de reciclaje**: Creación, gestión y seguimiento
- **Perfiles personalizados**: Gestión por usuario y organización
- **Reportes de problemas**: Sistema de feedback y soporte
- **Seguridad**: Autenticación y autorización con Spring Security

## Tecnologías

- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Tailwind CSS
- **Base de datos**: MySQL/H2
- **Herramientas**: Maven, Git

## Arquitectura del Sistema

### Estructura Simplificada
```
src/main/
├── java/
│   └── com.example.registrationlogindemo/
│       ├── config/              # Configuración (seguridad, web)
│       ├── controller/          # Controladores MVC (estructura plana)
│       ├── dto/                 # Objetos de transferencia de datos
│       ├── entity/              # Entidades JPA
│       ├── repository/          # Repositorios de acceso a datos
│       ├── service/             # Interfaces de servicios
│       └── service/impl/        # Implementaciones de servicios
└── resources/
    ├── static/              # Recursos estáticos (CSS, JS, imágenes)
    ├── templates/           # Plantillas Thymeleaf
    │   ├── fragments/       # Componentes reutilizables
    │   ├── layouts/         # Plantillas base
    │   └── pages/           # Páginas organizadas por roles
    │       ├── admin/       # Páginas de administración
    │       ├── all/         # Páginas accesibles por todos
    │       ├── error.html   # Página de error general
    │       ├── guest/       # Páginas para invitados
    │       ├── org/         # Páginas para organizaciones
    │       └── user/        # Páginas para usuarios
    └── application.properties  # Configuración de la aplicación
```

## Sistema de Roles

El sistema implementa tres roles principales, cada uno con responsabilidades específicas:

### 1. Usuario (USER)
- **Descripción**: Ciudadanos o usuarios comunes que desean reciclar materiales
- **Funciones**:
  - Crear solicitudes de recolección de materiales reciclables
  - Ver el estado de sus solicitudes
  - Actualizar su perfil
  - Reportar problemas
  - Recibir notificaciones sobre cambios en sus solicitudes
- **Rutas principales**: `/user/**`

### 2. Organización (ORGANIZATION)
- **Descripción**: Centros de reciclaje o entidades que procesan materiales reciclables
- **Funciones**:
  - Recibir y procesar solicitudes de recolección
  - Actualizar el estado de las solicitudes
  - Gestionar su perfil organizacional
  - Ver estadísticas de sus operaciones
- **Rutas principales**: `/org/**`

### 3. Administrador (ADMIN)
- **Descripción**: Gestores del sistema con acceso completo
- **Funciones**:
  - Administrar usuarios y organizaciones (CRUD completo)
  - Supervisar todas las solicitudes
  - Ver estadísticas globales del sistema
  - Gestionar reportes y problemas
- **Rutas principales**: `/admin/**`

## Mapa de Controladores y Rutas

### Controladores Principales

La aplicación implementa una estructura plana de controladores (sin subcarpetas), siguiendo el principio KISS :

#### 1. AuthController
- **Rutas**: `/login`, `/register`, `/error`, `/init`
- **Funciones**:
  - Gestión de registro de usuarios
  - Autenticación y login
  - Manejo de errores
  - Redirección inicial según rol

#### 2. BaseController
- **Rutas**: `/`, `/index`, `/about`, `/contact`
- **Funciones**:
  - Páginas públicas
  - Información general del sistema

#### 3. UserController
- **Rutas**: `/user/**`
  - `/user/inicio` - Dashboard de usuario
  - `/user/profile` - Gestión de perfil
  - `/user/solicitudes` - Lista de solicitudes propias
- **Funciones**:
  - Gestión del perfil de usuario
  - Visualización de solicitudes propias

#### 4. SolicitudeController
- **Rutas**: `/user/nueva-solicitud`, `/user/newsolicitude`, `/user/editsolicitude/**`
- **Funciones**:
  - Creación de nuevas solicitudes de reciclaje
  - Edición de solicitudes existentes
  - Eliminación de solicitudes

#### 5. OrganizationController
- **Rutas**: `/org/**`
  - `/org/inicio` - Dashboard de organización
  - `/org/profile` - Gestión de perfil
  - `/org/solicitudes` - Gestión de solicitudes asignadas
- **Funciones**:
  - Gestión del perfil de organización
  - Procesamiento de solicitudes de reciclaje

#### 6. AdministradorController
- **Rutas**: `/admin/**`
  - `/admin/inicio` - Dashboard administrativo
  - `/admin/users` - Gestión de usuarios
  - `/admin/organizations` - Gestión de organizaciones
  - `/admin/solicitudes` - Supervisión de todas las solicitudes
- **Funciones**:
  - Administración completa del sistema
  - Gestión de usuarios y organizaciones
  - Supervisión global de solicitudes

#### 7. ReportController
- **Rutas**: `/reportes/**`
  - `/reportes/nuevo` - Formulario de reporte
  - `/reportes/enviar` - Procesamiento de reportes
- **Funciones**:
  - Gestión de reportes de problemas
  - Accesible para todos los roles autenticados

## Estructura de Plantillas

### Organización de Plantillas

```
templates/
├── fragments/           # Componentes reutilizables (headers, footer, alertas)
├── layouts/             # Layout base
└── pages/               # Páginas por rol (admin, user, org, guest)
```

## Flujo de Trabajo

### Usuario
- Crea solicitudes de reciclaje
- Selecciona organización destino
- Monitorea estado de solicitudes
- Recibe notificaciones de cambios

### Organización
- Recibe y procesa solicitudes
- Actualiza estados
- Gestiona operaciones
- Envía notificaciones a usuarios

### Administrador
- Supervisa todo el sistema
- Gestiona usuarios y organizaciones
- Accede a estadísticas

## Frontend

### Tecnologías Frontend
- **Thymeleaf**: Motor de plantillas para integración con Spring
- **Thymeleaf Layout Dialect**: Sistema de layouts y fragmentos
- **Tailwind CSS**: Framework de utilidades CSS para estilos consistentes

### Características de la Interfaz
- **Diseño Responsivo**: Adaptable a dispositivos móviles y escritorio
- **Accesibilidad**: Cumplimiento de estándares básicos de accesibilidad
- **Interfaz Intuitiva**: Flujos de trabajo claros y sencillos

## Instalación y Configuración

### Requisitos
- Java 11 o superior
- Maven 3.6+
- Base de datos MySQL o H2

```bash
# Clonar el repositorio
git clone https://github.com/fedegonc/Spring-Security-Basics.git

# Configurar la base de datos en application.properties
# Ejecutar el proyecto
mvn spring-boot:run
```

## Notas Importantes

- Este es un MVP (Producto Mínimo Viable) con menos de 5000 líneas de código
- Se priorizan soluciones simples y directas (principio KISS)
- La estructura plana de controladores facilita el mantenimiento
- Se reutiliza código existente siempre que sea posible

## Sistema de Notificaciones

El sistema incluye un módulo de notificaciones para mantener a los usuarios informados sobre el estado de sus solicitudes:

### Características principales

- **Notificaciones por correo electrónico**: Alertas automáticas cuando cambia el estado de una solicitud
- **Notificaciones en plataforma**: Centro de notificaciones con actualizaciones importantes
- **Tipos de notificaciones**:
  - Confirmación de nueva solicitud
  - Cambios de estado (aceptada, en proceso, completada)
  - Mensajes de la organización
  - Recordatorios de recolección

### Implementación

- Servicio centralizado de notificaciones
- Integración con Spring Mail para correos electrónicos
- Almacenamiento de notificaciones en base de datos
- Marcado de notificaciones como leídas/no leídas
