# Apuntes de Desarrollo Frontend

## Problemas identificados

### 1. Fragilidad en el Frontend
- **Mezcla de frameworks CSS**: Combinación de Bootstrap y Tailwind genera inconsistencias visuales y de comportamiento.
- **Paradigmas de navegación múltiples**: Diferentes templates usan distintos componentes de navegación (header vs navbar-user).
- **Interdependencias no documentadas**: Los fragmentos Thymeleaf tienen parámetros requeridos sin documentación clara.
- **Organización de código inconsistente**: Scripts dispersos y falta de separación clara de responsabilidades.

### 2. Errores comunes
- Fragmentos con parámetros específicos (`solicitudes-tarjetas`) usados inconsistentemente en diferentes vistas.
- Uso de strings literales ('solicitude') donde se esperaban objetos (`${solicitude}`).
- Scripts JS duplicados o embebidos en múltiples archivos HTML.

## Recomendaciones

### 1. Estandarización
- **Unificar framework CSS**: Completar la transición a Tailwind CSS, eliminando gradualmente Bootstrap.
- **Crear hoja de estilos base**: Definir componentes y utilidades comunes reutilizables.

### 2. Documentación
- **Documentar fragmentos Thymeleaf**:
  ```html
  <!-- 
    Fragment: solicitudes-tarjetas
    Parámetros:
    - titulo (String): Título a mostrar
    - showHeader (boolean): Visibilidad de cabecera
    - showActions (boolean): Visibilidad de acciones
    - solicitudes (List<Solicitude>): Datos a mostrar
    - path (String): Ruta base para enlaces
  -->
  ```

### 3. Estructura y organización
- **Sistema de componentes estructurado**:
  - `/fragments/layout/` - Componentes estructurales (header, footer, etc.)
  - `/fragments/ui/` - Componentes de UI reutilizables
  - `/fragments/[role]/` - Componentes específicos por rol

- **Organización de JS**:
  - `/js/common/` - Funcionalidades compartidas
  - `/js/pages/` - Scripts específicos por página
  - `/js/components/` - Scripts para componentes reutilizables

### 4. Validaciones y pruebas
- Agregar validaciones Thymeleaf para detectar errores temprano.
- Crear una página de referencia de componentes como documentación viva.

### 5. Buenas prácticas
- Separar HTML (estructura), CSS (presentación) y JS (comportamiento).
- Mantener consistencia en nomenclatura y estructura.
- Evitar duplicación de código y favorecer la composición de componentes.

## Próximos pasos
1. Completar unificación de frameworks CSS a Tailwind.
2. Documentar componentes existentes.
3. Reorganizar assets estáticos (JS, CSS).
4. Implementar validaciones para prevenir errores.
