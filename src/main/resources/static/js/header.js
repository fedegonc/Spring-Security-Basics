// js/header.js
document.addEventListener('DOMContentLoaded', function() {
  // Función para manejar todos los dropdowns de manera consistente
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
  
  // Configurar los dropdowns principales
  setupDropdown('userMenuButton', 'userMenu');
  setupDropdown('mobileMenuButton', 'mobileMenu');
  
  // Añadir funcionalidad para cerrar el menú móvil al hacer clic en un enlace
  var mobileMenuLinks = document.querySelectorAll('#mobileMenu a');
  mobileMenuLinks.forEach(function(link) {
    link.addEventListener('click', function() {
      var mobileMenu = document.getElementById('mobileMenu');
      if (mobileMenu) {
        mobileMenu.classList.add('hidden');
      }
    });
  });
});
