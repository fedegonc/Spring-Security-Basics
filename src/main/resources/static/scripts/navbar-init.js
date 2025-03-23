/**
 * Script para inicializar los componentes del navbar
 * Este archivo se incluye en todas las páginas para garantizar que los desplegables funcionen correctamente
 */
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar todos los dropdowns de Bootstrap
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
    if (typeof bootstrap !== 'undefined') {
        dropdownElementList.map(function(dropdownToggleEl) {
            return new bootstrap.Dropdown(dropdownToggleEl);
        });
    }
    
    // Inicializar todos los tooltips de Bootstrap (si existen)
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    if (typeof bootstrap !== 'undefined') {
        tooltipTriggerList.map(function(tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
});
