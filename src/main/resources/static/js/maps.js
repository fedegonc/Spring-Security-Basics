// Espera a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function () {
    // Inicializa el mapa y establece las coordenadas y el nivel de zoom
    var map = L.map('map').setView([-30.88250225738402, -55.53590209874656], 16);

    // Agrega la capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Agrega un marcador en la ubicación específica
    L.marker([-30.88250225738402, -55.53590209874656]).addTo(map)
        .bindPopup('Associação de Catadores Novo Horizonte')
        .openPopup();
});
// Espera a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function () {
    // Inicializa el mapa y establece las coordenadas y el nivel de zoom
    var map = L.map('map2').setView([-30.937886022823736, -55.53291678413656], 16);

    // Agrega la capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Agrega un marcador en la ubicación específica
    L.marker([-30.937886022823736, -55.53291678413656]).addTo(map)
        .bindPopup('Cooperativa Renacer del Norte')
        .openPopup();
});