# Sistema de CSS Unificado

Este directorio contiene el sistema de estilos CSS unificado para la aplicación de Reciclaje Sólido.

## Estructura del Sistema

```
css/
├── main.css                 # Archivo principal que importa todos los módulos
├── README.md                # Esta documentación
├── components/              # Componentes reutilizables de UI
│   ├── accessibility.css    # Estilos específicos para accesibilidad
│   ├── cards.css            # Componentes de tarjetas (unificando cards.css y tarjetas.css)
│   └── navigation.css       # Estilos para menús y navegación
└── layouts/                 # Estructuras de página y disposiciones
    └── base.css             # Layouts base (contenedores, fondos, etc.)
```

## Objetivos de la Refactorización

1. **Eliminar Redundancias**: Consolidar múltiples archivos CSS con funcionalidad similar.
2. **Mejorar el Mantenimiento**: Organizar el código en módulos temáticos.
3. **Estandarizar el Diseño**: Utilizar variables CSS para mantener consistencia.
4. **Optimizar el Rendimiento**: Reducir el tamaño total de los archivos CSS.
5. **Mejorar la Accesibilidad**: Mantener y reforzar las mejoras de accesibilidad existentes.

## Cómo Usar este Sistema

### Importación en HTML/Thymeleaf

Solo necesitas incluir el archivo principal `main.css` en tus templates:

```html
<link rel="stylesheet" th:href="@{/css/main.css}">
```

### Uso de Variables CSS

El sistema define variables CSS globales en `main.css` para mantener consistencia:

```css
/* Ejemplo de uso de variables */
.mi-elemento {
    color: var(--color-primary);
    padding: var(--spacing-md);
    border-radius: var(--border-radius-sm);
}
```

### Componentes Disponibles

#### Tarjetas (cards.css)

- `.card-base`: Clase base para todas las tarjetas
- `.solicitud-card`: Tarjetas de solicitudes
- `.pub-card`: Tarjetas de publicaciones
- `.metric-card`: Tarjetas de métricas

#### Navegación (navigation.css)

- Estilos para navbar, dropdowns y navegación móvil
- Clases utilitarias para botones y enlaces

#### Accesibilidad (accessibility.css)

- `.skip-link`: Enlaces para saltar al contenido principal
- `.sr-only-focusable`: Elementos visibles solo con foco de teclado
- Mejoras de contraste, foco y tamaños de interacción

#### Layouts (base.css)

- `.app-background`: Fondo principal de la aplicación
- `.glass-card`: Contenedores con efecto de vidrio
- `.content-container`: Contenedor principal de contenido

## Extensiones Futuras

Para extender este sistema, seguir estas pautas:

1. **Nuevos Componentes**: Crear archivos CSS en la carpeta `components/`
2. **Nuevos Layouts**: Crear archivos CSS en la carpeta `layouts/`
3. **Nuevas Variables**: Añadirlas al bloque `:root` en `main.css`
4. **Importaciones**: Actualizar `main.css` para importar nuevos módulos

## Notas de Mantenimiento

- Mantener los nombres de clase coherentes con la arquitectura existente
- Documentar nuevos componentes o cambios importantes
- Preservar las mejoras de accesibilidad ya implementadas
