// js/header.js
document.addEventListener('DOMContentLoaded', function() {
  // Menú de idioma
  var languageBtn = document.getElementById('languageDropdown');
  var languageMenu = document.getElementById('language-dropdown-menu');
  if (languageBtn && languageMenu) {
    languageBtn.onclick = function(e) {
      e.preventDefault();
      languageMenu.classList.toggle('hidden');
    };
  }

  // Menú de usuario
  var userBtn = document.getElementById('userDropdownButton');
  var userMenu = document.getElementById('user-dropdown-menu');
  if (userBtn && userMenu) {
    userBtn.onclick = function(e) {
      e.preventDefault();
      userMenu.classList.toggle('hidden');
    };
  }

  // Cerrar dropdowns al hacer clic fuera
  document.addEventListener('click', function(e) {
    if (languageBtn && languageMenu && !languageBtn.contains(e.target) && !languageMenu.contains(e.target)) {
      languageMenu.classList.add('hidden');
    }
    if (userBtn && userMenu && !userBtn.contains(e.target) && !userMenu.contains(e.target)) {
      userMenu.classList.add('hidden');
    }
  });

  // Menú móvil
  var mobileMenuButton = document.getElementById('mobileMenuButton');
  var mobileMenu = document.getElementById('mobileMenu');
  if (mobileMenuButton && mobileMenu) {
    mobileMenuButton.onclick = function() {
      mobileMenu.classList.toggle('hidden');
    };
  }
});
