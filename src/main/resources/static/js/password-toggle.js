/**
 * password-toggle.js
 * Script simple para alternar la visibilidad de campos de contraseña
 */

document.addEventListener('DOMContentLoaded', function() {
  // Usando delegación de eventos para capturar clics en botones de toggle
  document.addEventListener('click', function(event) {
    // Verificar si el clic fue en un botón de toggle o dentro de uno
    const toggleButton = event.target.closest('#togglePassword');
    
    if (toggleButton) {
      // Prevenir la acción por defecto para evitar redirecciones
      event.preventDefault();
      
      // Encontrar el campo de contraseña asociado (asumimos que está en el mismo formulario)
      const form = toggleButton.closest('form');
      if (!form) return;
      
      const passwordField = form.querySelector('#password');
      if (!passwordField) return;
      
      // Alternar el tipo del campo
      const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
      passwordField.setAttribute('type', type);
      
      // Alternar el ícono
      const icon = toggleButton.querySelector('#toggleIcon');
      if (icon) {
        if (type === 'text') {
          icon.classList.remove('bi-eye-slash');
          icon.classList.add('bi-eye');
        } else {
          icon.classList.remove('bi-eye');
          icon.classList.add('bi-eye-slash');
        }
      }
    }
  });
});
