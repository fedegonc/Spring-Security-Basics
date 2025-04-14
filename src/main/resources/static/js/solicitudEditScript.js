function clearSelectInput() {
    document.getElementById('horaRecoleccion').value = '';
}

function clearTimeInput() {
    document.getElementById('customHoraRecoleccion').value = '';
}

function validateTimeInputs() {
    const horaRecoleccion = document.getElementById('horaRecoleccion').value;
    const customHoraRecoleccion = document.getElementById('customHoraRecoleccion').value;

    if (!horaRecoleccion && !customHoraRecoleccion) {
        alert('Por favor seleccione o ingrese una hora de recolección.');
        return false;
    }

    return true;
}

document.getElementById('editForm').addEventListener('submit', function(event) {
    if (!validateTimeInputs()) {
        event.preventDefault();
    }
});

function updateAvailableHours() {
    const day = document.getElementById('diasDisponibles').value;
    const hourSelect = document.getElementById('horaRecoleccion');
    hourSelect.innerHTML = '<option value="" disabled selected>Seleccione la hora de recolección</option>';

    let hours = [];
    if (day === 'saturday') {
        hours = ['09:00', '10:00', '11:00'];
    } else if (day) {
        hours = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00'];
    }

    hours.forEach(hour => {
        const option = document.createElement('option');
        option.value = hour;
        option.textContent = hour;
        hourSelect.appendChild(option);
    });
}


document.addEventListener('DOMContentLoaded', function() {
    var currentValue = document.querySelector('select').getAttribute('data-value'); // Asume que data-value tiene el valor del servidor

    // Cambia el estado visual de los botones
    var toggleActivo = document.getElementById('toggleActivo');
    var toggleDesactivado = document.getElementById('toggleDesactivado');

    if (currentValue === '1') {
        toggleActivo.classList.add('active');
        toggleDesactivado.classList.remove('active');
    } else if (currentValue === '0') {
        toggleActivo.classList.remove('active');
        toggleDesactivado.classList.add('active');
    }

    // Añade evento para cambiar el estado
    toggleActivo.addEventListener('click', function() {
        toggleActivo.classList.add('active');
        toggleDesactivado.classList.remove('active');
        // Enviar valor al servidor o procesar el cambio
    });

    toggleDesactivado.addEventListener('click', function() {
        toggleActivo.classList.remove('active');
        toggleDesactivado.classList.add('active');
        // Enviar valor al servidor o procesar el cambio
    });
});