/**
 * Script principal para funcionalidades de la aplicación RSU
 */

// Cerrar el menú responsive al hacer clic en un elemento
document.addEventListener('DOMContentLoaded', function() {
    // Método simple para cerrar cualquier menú abierto cuando se hace clic en un enlace
    document.body.addEventListener('click', function(e) {
        // Verificar si el elemento clickeado es un enlace dentro del navbar
        const target = e.target.closest('a');
        if (target && target.closest('.navbar-collapse')) {
            // Buscar todos los menús colapsables abiertos
            const openMenus = document.querySelectorAll('.navbar-collapse.show');
            
            // Cerrar cada menú usando Bootstrap API
            openMenus.forEach(function(menu) {
                const bsCollapse = new bootstrap.Collapse(menu);
                bsCollapse.hide();
            });
        }
    });
    
    // Enfoque alternativo: cerrar al hacer clic fuera del menú
    document.addEventListener('click', function(e) {
        // Si el clic no fue dentro de un navbar-collapse
        if (!e.target.closest('.navbar-collapse') && !e.target.closest('.navbar-toggler')) {
            // Cerrar todos los menús abiertos
            const openMenus = document.querySelectorAll('.navbar-collapse.show');
            openMenus.forEach(function(menu) {
                const bsCollapse = new bootstrap.Collapse(menu);
                bsCollapse.hide();
            });
        }
    });
});
