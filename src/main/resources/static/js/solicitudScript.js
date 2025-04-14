// Limpia el campo de selección de hora al ingresar una hora manual
function clearSelectInput() {
    document.getElementById('horaRecoleccion').value = '';
}

// Limpia el campo de hora manual al seleccionar una hora
function clearTimeInput() {
    document.getElementById('customHoraRecoleccion').value = '';
}

// Maneja la lógica de selección y entrada de hora
document.getElementById('horaRecoleccion').addEventListener('change', clearTimeInput);
document.getElementById('customHoraRecoleccion').addEventListener('change', clearSelectInput);

// Valida los campos antes de enviar el formulario
function validateTimeInputs() {
    const horaRecoleccion = document.getElementById('horaRecoleccion').value;
    const customHoraRecoleccion = document.getElementById('customHoraRecoleccion').value;

    // Permite el envío si al menos uno de los campos de hora tiene un valor
    if (!horaRecoleccion && !customHoraRecoleccion) {
        alert('Por favor seleccione o ingrese una hora de recolección.');
        return false;
    }

    return true;
}

// Añade un manejador de eventos al formulario para validar antes de enviar
document.getElementById('finalForm').addEventListener('submit', function(event) {
    if (!validateTimeInputs()) {
        // Prevenir el envío del formulario si la validación falla
        event.preventDefault();
    }
});

function previewFile() {
    const preview = document.getElementById('previewImage');
    const file = document.getElementById('file').files[0];
    const reader = new FileReader();

    reader.onloadend = function () {
        preview.src = reader.result;
    }

    if (file) {
        reader.readAsDataURL(file); // lee el archivo como una URL de datos
    } else {
        preview.src = "/img/thumbnail-default.jpg"; // imagen por defecto
    }
}

function updateAvailableHours() {
    const day = document.getElementById('diasDisponibles').value;
    const hourSelect = document.getElementById('horaRecoleccion');

    // Limpiar las opciones actuales
    hourSelect.innerHTML = '<option value="" disabled selected>Seleccione la hora de recolección</option>';

    // Definir horas disponibles según el día
    let hours = [];
    if (day === 'saturday') {
        hours = ['09:00', '10:00', '11:00'];
    } else if (day) {
        hours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'];
    }

    // Agregar las nuevas opciones
    hours.forEach(hour => {
        const option = document.createElement('option');
        option.value = hour;
        option.textContent = hour;
        hourSelect.appendChild(option);
    });
}

function toggleTimeInputs() {
    const manualCheckbox = document.getElementById('manualTimeCheckbox');
    const horaRecoleccion = document.getElementById('horaRecoleccion');
    const customHoraRecoleccion = document.getElementById('customHoraRecoleccion');

    if (manualCheckbox.checked) {
        horaRecoleccion.disabled = true;
        customHoraRecoleccion.disabled = false;
    } else {
        horaRecoleccion.disabled = false;
        customHoraRecoleccion.disabled = true;
    }
}

// Inicializar el estado de los campos de hora
toggleTimeInputs();