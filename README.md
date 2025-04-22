# Sistema de Comunicación para Reciclaje - Frontera de la Paz Sustentable

Este proyecto es un sistema de comunicación que conecta a la comunidad con los centros de acopio de materiales reciclables, siendo parte del proyecto más amplio "Frontera de la Paz Sustentable".

## Propósito del Sistema

Facilitar la comunicación entre ciudadanos y centros de reciclaje, permitiendo a los usuarios registrar solicitudes de recolección o entrega de materiales reciclables, con una gestión eficiente por parte de las organizaciones participantes.

## Características Principales

- **Multirol**: Usuarios, Administradores, Organizaciones e Invitados
- **Solicitudes de reciclaje**: Creación, gestión y seguimiento
- **Perfiles personalizados**: Gestión por usuario y organización
- **Multiidioma**: Español y Portugués (interfaz bilingüe)
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
│       ├── controller/          # Controladores MVC
│       ├── dto/                 # Objetos de transferencia de datos
│       ├── entity/              # Entidades JPA
│       ├── repository/          # Repositorios de acceso a datos
│       ├── service/             # Interfaces de servicios
│       └── service/impl/        # Implementaciones de servicios
│           ├── auth/            # Autenticación y autorización
│           ├── notification/    # Notificaciones y mensajes flash
│           ├── rolemanagement/  # Gestión de roles y permisos
│           ├── solicitude/      # Gestión de solicitudes de reciclaje
│           └── validation/      # Validación de formularios y entidades
└── resources/
    ├── static/              # Recursos estáticos (CSS, JS, imágenes)
    ├── templates/           # Plantillas Thymeleaf
    │   ├── fragments/       # Componentes reutilizables
    │   ├── layouts/         # Plantillas base
    │   └── pages/           # Páginas organizadas por roles
    │       ├── admin/       # Páginas de administración
    │       ├── all/         # Páginas accesibles por todos los roles
    │       ├── error/       # Páginas de error
    │       ├── guest/       # Páginas para invitados
    │       ├── organization/# Páginas para organizaciones
    │       └── user/        # Páginas para usuarios regulares
    ├── language/            # Archivos de internacionalización
    └── application.properties  # Configuración de la aplicación
```

### Backend

La arquitectura backend sigue un patrón MVC (Modelo-Vista-Controlador) con una clara separación de responsabilidades y un sistema de seguridad basado en roles.

#### Controladores por Rol

La aplicación implementa un enfoque de rutas basadas en funcionalidad, donde cada controlador gestiona las operaciones específicas para un rol determinado:

##### 1. Acceso Público (Sin autenticación)
- **AuthController** (`/login`, `/register`, `/error`)
  - Gestión de registro de usuarios
  - Autenticación y login
  - Manejo de errores generales
  
- **GuestController** (rutas públicas)
  - Páginas de inicio para visitantes
  - Información general del sistema
  - Páginas estáticas como "Acerca de"

##### 2. Rol USER
- **UserController** (`/user/**`)
  - `/user/inicio` - Dashboard personalizado
  - `/user/profile` - Gestión del perfil
  - `/user/solicitudes` - Visualización de solicitudes propias
  - `/user/newsolicitude` - Creación de nuevas solicitudes
  - `/user/report-problem` - Reporte de problemas

- **SolicitudeController** (rutas relacionadas con solicitudes)
  - Creación y gestión de solicitudes de reciclaje
  - Visualización del estado de solicitudes
  - Actualización de solicitudes existentes

##### 3. Rol ORGANIZATION
- **OrgController** (`/org/**`)
  - `/org/inicio` - Dashboard de organización
  - `/org/profile` - Gestión del perfil organizacional
  - `/org/solicitudes` - Gestión de solicitudes asignadas
  - Procesamiento de solicitudes de reciclaje

##### 4. Rol ADMIN
- **AdministradorController** (`/admin/**`)
  - `/admin/inicio` - Dashboard administrativo
  - `/admin/profile` - Gestión del perfil de administrador
  - `/admin/users` - Administración de usuarios
  - `/admin/solicitudes` - Gestión global de solicitudes
  - `/admin/reports` - Administración de reportes
  - Funciones administrativas generales

- **ReportController** (reportes y estadísticas)
  - Generación de reportes del sistema
  - Estadísticas y análisis

##### 5. Controladores Base
- **BaseController** (clase base abstracta)
  - Funcionalidad común para todos los controladores
  - Métodos para cambio de contraseña
  - Manejo de archivos

#### Configuración de Seguridad

La seguridad se implementa en `SpringSecurity.java` con las siguientes reglas:

1. **Rutas públicas**: Accesibles sin autenticación
   ```java
   .requestMatchers("/", "/register/**", "/login/**", "/error", "/img/**", ...).permitAll()
   ```

2. **Rutas protegidas por rol**:
   ```java
   .requestMatchers("/user/**").hasRole("USER")
   .requestMatchers("/admin/**").hasRole("ADMIN")
   .requestMatchers("/org/**").hasRole("ORGANIZATION")
   ```

3. **Otras rutas**: Requieren autenticación
   ```java
   .anyRequest().authenticated()
   ```

#### Servicios Principales

La aplicación implementa una arquitectura orientada a servicios, donde cada componente tiene una responsabilidad específica:

1. **UserService**: Gestión de usuarios y perfiles
2. **AuthService**: Autenticación, autorización y manejo de sesiones
3. **SolicitudeService**: Gestión de solicitudes de reciclaje
4. **OrgService**: Servicios específicos para organizaciones
5. **ReportService**: Generación y gestión de reportes
6. **ValidationAndNotificationService**: Validación de datos y notificaciones
7. **FileStorageService**: Gestión de archivos y almacenamiento
8. **DashboardService**: Métricas y estadísticas para dashboards

#### Flujo de Datos

1. El usuario realiza una solicitud HTTP
2. La configuración de seguridad valida permisos y roles
3. El controlador apropiado recibe la solicitud
4. El controlador delega la lógica de negocio a los servicios
5. Los servicios interactúan con los repositorios para acceder a los datos
6. Los datos procesados se devuelven al controlador
7. El controlador prepara el modelo y selecciona la vista
8. La vista (plantilla Thymeleaf) se renderiza con los datos del modelo
9. La respuesta HTML se envía al usuario

### Frontend

La arquitectura frontend de la aplicación se basa en una estructura simplificada y modular utilizando Thymeleaf Layout Dialect, implementando un sistema de fragmentos reutilizables y utilizando Tailwind CSS como framework de estilos.

#### Tecnologías Frontend
- **Thymeleaf**: Motor de plantillas para integración con Spring
- **Thymeleaf Layout Dialect**: Sistema de layouts y fragmentos para estructura modular
- **Tailwind CSS**: Framework de utilidades CSS para estilos consistentes
- **Flowbite**: Componentes prediseñados para Tailwind CSS

#### Estructura de Directorios

```
templates/
├── fragments/           # Componentes reutilizables
│   ├── admin/           # Fragmentos específicos para administradores
│   ├── footers/         # Pie de página único y simple
│   │   └── simple.html  # Footer básico reutilizable para toda la aplicación
│   ├── guest/           # Fragmentos para usuarios invitados
│   ├── headers/         # Cabeceras según rol de usuario (contienen la lógica compleja)
│   │   ├── admin.html   # Header para administradores
│   │   ├── guest.html   # Header para invitados
│   │   ├── org.html     # Header para organizaciones
│   │   └── user.html    # Header para usuarios regulares
│   ├── org/             # Fragmentos específicos para organizaciones
│   └── user/            # Fragmentos específicos para usuarios
├── layouts/             # Plantillas base
│   └── base.html        # Layout principal con slots para contenido
└── pages/               # Páginas completas
    ├── admin/           # Páginas de administración
    ├── all/             # Páginas accesibles por todos los roles
    ├── guest/           # Páginas para invitados
    ├── org/             # Páginas para organizaciones
    └── user/            # Páginas para usuarios regulares
```

#### Sistema de Layouts

El sistema de layouts se basa en un enfoque simplificado donde:

1. **Estructura base**: Cada página extiende el layout base (`layouts/base.html`)
   - Define la estructura HTML común
   - Incluye los recursos compartidos (CSS, JS)
   - Gestiona la inclusión de headers según el rol

2. **Principios de diseño**:
   - **Simplicidad**: La lógica compleja se concentra únicamente en los headers
   - **Reutilización**: Un único footer simple para toda la aplicación
   - **Eficiencia**: Los breadcrumbs se implementan directamente en cada página
   - **Mantenibilidad**: Se evitan fragmentos innecesarios

3. **Slots disponibles**:
   - **header**: Cabecera de la página (basada en el rol del usuario)
   - **content**: Contenido principal de la página
   - **styles**: Estilos específicos de cada página
   - **scripts**: Scripts específicos de página
   - **alerts**: Sistema de mensajes y notificaciones

#### Ejemplo de Implementación

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/base}"
      th:with="headerType='user', metaDescription='Descripción para SEO'">

<head>
    <title>Título de la Página</title>
    
    <!-- Estilos específicos para esta página -->
    <th:block layout:fragment="styles">
        <style>
            /* Estilos específicos */
        </style>
    </th:block>
</head>

<body>
    <!-- Contenido principal -->
    <section layout:fragment="content">
        <!-- Breadcrumb hardcodeado en la página -->
        <nav class="text-sm mb-4">
            <ol class="list-none p-0 inline-flex">
                <li class="flex items-center">
                    <a th:href="@{/}" class="text-blue-500 hover:text-blue-700">Inicio</a>
                    <svg class="w-3 h-3 mx-2" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path>
                    </svg>
                </li>
                <li class="text-gray-500">Página Actual</li>
            </ol>
        </nav>
        
        <!-- Contenido de la página -->
        <div class="bg-white p-6 rounded-lg shadow-md">
            <h1 class="text-2xl font-bold mb-4">Título del Contenido</h1>
            <p>Contenido de la página...</p>
        </div>
    </section>
    
    <!-- Scripts específicos para esta página -->
    <th:block layout:fragment="scripts">
        <script>
            // Scripts específicos
        </script>
    </th:block>
</body>
</html>
```

#### Características Principales

La demo incluye:

### Backend

- **Seguridad por capas**: Autenticación, autorización y validación
- **Servicios modularizados**: Organización por funcionalidad y responsabilidad
- **Gestión de archivos**: Soporte para subida y almacenamiento de imágenes

## Principales Funcionalidades

### Usuarios
- Registro y autenticación
- Creación y gestión de solicitudes de reciclaje
- Perfil personalizable

### Organizaciones
- Gestión de solicitudes asignadas
- Perfil de organización
- Estadísticas de actividad

### Administradores
- Gestión completa de usuarios y organizaciones
- Administración global de solicitudes
- Reportes y estadísticas del sistema

## Requisitos e Instalación

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
