// Script para los menús desplegables - Versión simplificada
window.addEventListener('DOMContentLoaded', function() {
  console.log('Menu dropdown script loaded');
  
  // Menú de idioma
  var languageButton = document.getElementById('languageDropdown');
  var languageMenu = document.getElementById('language-dropdown-menu');
  
  if (languageButton && languageMenu) {
    languageButton.addEventListener('click', function(event) {
      event.preventDefault();
      event.stopPropagation();
      languageMenu.classList.toggle('hidden');
    });
  }
  
  // Menú de usuario
  var userButton = document.getElementById('userDropdownButton');
  var userMenu = document.getElementById('user-dropdown-menu');
  
  if (userButton && userMenu) {
    userButton.addEventListener('click', function(event) {
      event.preventDefault();
      event.stopPropagation();
      userMenu.classList.toggle('hidden');
    });
  }
  
  // Cerrar al hacer clic fuera
  document.addEventListener('click', function(event) {
    if (languageMenu && !languageButton.contains(event.target) && !languageMenu.contains(event.target)) {
      languageMenu.classList.add('hidden');
    }
    if (userMenu && !userButton.contains(event.target) && !userMenu.contains(event.target)) {
      userMenu.classList.add('hidden');
    }
  });
});
