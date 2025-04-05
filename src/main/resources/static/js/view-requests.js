/**
 * Script para la página de visualización de solicitudes
 * Gestiona el formateo de fechas y cálculo de estadísticas
 */
document.addEventListener('DOMContentLoaded', function () {
  // Formatear fechas
  const dateFields = document.querySelectorAll('.date-field');
  dateFields.forEach(field => {
    const rawDate = field.textContent.trim();
    if (rawDate) {
      const date = new Date(rawDate);
      if (!isNaN(date)) {
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();
        field.textContent = `${day}/${month}/${year}`;
      }
    }
  });
  
  // Calcular contadores
  const solicitudes = document.querySelectorAll('.solicitud-card');
  const totalCount = solicitudes.length;
  let activeCount = 0;
  let completedCount = 0;
  
  solicitudes.forEach(solicitud => {
    const statusBadge = solicitud.querySelector('.badge');
    if (statusBadge) {
      const estado = statusBadge.textContent.trim();
      // Estados activos: EN_ESPERA, EN_REVISION o cualquier otro que no sea ACEPTADA o RECHAZADA
      if (estado === 'EN_ESPERA' || estado === 'EN_REVISION' || 
          (estado !== 'ACEPTADA' && estado !== 'RECHAZADA')) {
        activeCount++;
      } else if (estado === 'ACEPTADA') {
        completedCount++;
      }
    }
  });
  
  // Actualizar contadores
  document.getElementById('total-requests-count').textContent = totalCount;
  document.getElementById('active-requests-count').textContent = activeCount;
  document.getElementById('completed-requests-count').textContent = completedCount;
  
  // Actualizar barras de progreso
  if (totalCount > 0) {
    const activePercent = (activeCount / totalCount) * 100;
    const completedPercent = (completedCount / totalCount) * 100;
    
    document.getElementById('active-progress').style.width = activePercent + '%';
    document.getElementById('completed-progress').style.width = completedPercent + '%';
  }
});
