// js/header.js
document.addEventListener('DOMContentLoaded', function() {
  // Función reutilizable para manejar dropdowns
  function setupDropdown(buttonId, menuId) {
    var button = document.getElementById(buttonId);
    var menu = document.getElementById(menuId);
    
    if (button && menu) {
      // Alternar visibilidad al hacer clic en el botón
      button.onclick = function(e) {
        e.preventDefault();
        menu.classList.toggle('hidden');
      };
      
      // Cerrar al hacer clic fuera
      document.addEventListener('click', function(e) {
        if (button && menu && !button.contains(e.target) && !menu.contains(e.target)) {
          menu.classList.add('hidden');
        }
      });
    }
  }
  
  // Configurar dropdowns
  setupDropdown('languageDropdown', 'language-dropdown-menu');
  setupDropdown('userDropdownButton', 'user-dropdown-menu');
  setupDropdown('mobileMenuButton', 'mobileMenu');
});
