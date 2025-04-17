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
- **Frontend**: Thymeleaf, Bootstrap, TailwindCSS (en migración)
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
└── resources/
    ├── static/              # Recursos estáticos (CSS, JS, imágenes)
    ├── templates/           # Plantillas Thymeleaf
    │   ├── admin/           # Vistas de administración
    │   ├── auth/            # Vistas de autenticación
    │   ├── fragments/       # Componentes reutilizables
    │   ├── org/             # Vistas de organizaciones
    │   ├── solicitude/      # Vistas de solicitudes
    │   └── user/            # Vistas de usuarios
    ├── language/            # Archivos de internacionalización
    └── application.properties  # Configuración de la aplicación
```

### Frontend

- **Sistema de fragmentos**: Componentes Thymeleaf reutilizables (headers, forms, layouts)
- **Diseño responsivo**: Compatible con dispositivos móviles y escritorio
- **Estilos consistentes**: Implementación basada en Bootstrap con migración progresiva a TailwindCSS

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

## Principios de Diseño

El proyecto sigue los principios KISS (Keep It Simple, Stupid), DRY (Don't Repeat Yourself) y SOLID, enfocándose en la simplicidad, modularidad y reutilización de código.
