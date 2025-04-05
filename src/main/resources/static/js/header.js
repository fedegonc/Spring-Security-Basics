/**
 * Script para la funcionalidad del header
 * Gestiona los menús desplegables de navegación, idioma y usuario
 */
document.addEventListener('DOMContentLoaded', function() {
  // Menú hamburguesa
  document.getElementById('mobile-menu-button')?.addEventListener('click', function() {
    document.getElementById('mobile-menu').classList.toggle('hidden');
  });
  
  // Menú de idioma
  document.getElementById('languageDropdown')?.addEventListener('click', function(e) {
    e.stopPropagation();
    document.getElementById('language-dropdown-menu').classList.toggle('hidden');
          
    // Cerrar menú de usuario si está abierto
    const userMenu = document.getElementById('user-dropdown-menu');
    if (userMenu && !userMenu.classList.contains('hidden')) {
      userMenu.classList.add('hidden');
    }
  });
  
  // Menú de usuario
  document.getElementById('userDropdownButton')?.addEventListener('click', function(e) {
    e.stopPropagation();
    document.getElementById('user-dropdown-menu').classList.toggle('hidden');
    
    // Cerrar menú de idioma si está abierto
    const langMenu = document.getElementById('language-dropdown-menu');
    if (langMenu && !langMenu.classList.contains('hidden')) {
      langMenu.classList.add('hidden');
    }
  });
  
  // Cerrar menús al hacer clic en cualquier lugar
  document.addEventListener('click', function() {
    const menus = [
      document.getElementById('language-dropdown-menu'),
      document.getElementById('user-dropdown-menu')
    ];
    
    menus.forEach(menu => {
      if (menu && !menu.classList.contains('hidden')) {
        menu.classList.add('hidden');
      }
    });
  });
  
  // Evitar que los clics dentro de los menús los cierren
  document.querySelectorAll('#language-dropdown-menu, #user-dropdown-menu').forEach(menu => {
    menu?.addEventListener('click', function(e) {
      e.stopPropagation();
    });
  });
});
