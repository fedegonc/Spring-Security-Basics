document.addEventListener('DOMContentLoaded', function () {
    const dateFields = document.querySelectorAll('.date-field');
    const estadoFields = document.querySelectorAll('.estado-field');

    const requestedText = /*[[${'date.requested'}]]*/ 'solicitado'; // Fallback in case Thymeleaf doesn't replace it
    const estadoTranslations = {
        'EN_ESPERA': /*[[#{estado.EN_ESPERA}]]*/ 'En espera',
        'ACEPTADA': /*[[#{estado.ACEPTADA}]]*/ 'Aceptada',
        'RECHAZADA': /*[[#{estado.RECHAZADA}]]*/ 'Rechazada',
        'EN_REVISION': /*[[#{estado.EN_REVISION}]]*/ 'En revisión'
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

    const viewCardsBtn = document.getElementById('view-cards-btn');
    const viewTableBtn = document.getElementById('view-table-btn');
    const cardView = document.getElementById('card-view');
    const tableView = document.getElementById('table-view');

    viewCardsBtn.addEventListener('click', () => {
        cardView.classList.remove('hidden');
        tableView.classList.add('hidden');
        viewCardsBtn.classList.add('btn-primary');
        viewCardsBtn.classList.remove('btn-secondary');
        viewTableBtn.classList.add('btn-secondary');
        viewTableBtn.classList.remove('btn-primary');
    });

    viewTableBtn.addEventListener('click', () => {
        cardView.classList.add('hidden');
        tableView.classList.remove('hidden');
        viewTableBtn.classList.add('btn-primary');
        viewTableBtn.classList.remove('btn-secondary');
        viewCardsBtn.classList.add('btn-secondary');
        viewCardsBtn.classList.remove('btn-primary');
    });
});