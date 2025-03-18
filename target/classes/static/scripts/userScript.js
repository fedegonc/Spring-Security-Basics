document.addEventListener('DOMContentLoaded', function () {
    const dateFields = document.querySelectorAll('.date-field');
    const estadoFields = document.querySelectorAll('.estado-field');

    const requestedText = 'Solicitado'; // Fallback in case Thymeleaf doesn't replace it
    const estadoTranslations = {
        'EN_ESPERA': 'En espera',
        'ACEPTADA': 'Aceptada',
        'RECHAZADA': 'Rechazada',
        'EN_REVISION': 'En revisión'
    };

    dateFields.forEach(field => {
        const rawDate = field.textContent.trim();
        if (rawDate) {
            const date = new Date(rawDate);
            const day = date.getDate();
            const month = date.getMonth() + 1;
            const year = date.getFullYear().toString().slice(-2);
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');

            const formattedDate = `${requestedText}: ${day}/${month}/${year} - ${hours}:${minutes} hs`;
            field.textContent = formattedDate;
        }
    });

    estadoFields.forEach(field => {
        const estado = field.textContent.trim();
        if (estadoTranslations[estado]) {
            field.textContent = estadoTranslations[estado];
        }
    });


});

