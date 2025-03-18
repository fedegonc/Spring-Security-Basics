
// Gráfico de Barras para Usuarios Activos
const ctxUsers = document.getElementById('usersChart').getContext('2d');
const usersChart = new Chart(ctxUsers, {
    type: 'bar',
    data: {
        labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio'],
        datasets: [{
            label: 'Usuarios Activos',
            data: [120, 150, 180, 200, 170, 220],
            backgroundColor: 'rgba(75, 192, 192, 0.6)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 1
        }]
    },
    options: {
        scales: {
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Cantidad de Usuarios'
                }
            }
        },
        plugins: {
            legend: {
                display: true,
                position: 'top'
            }
        }
    }
});

// Gráfico de Torta para Estado de Solicitudes
const ctxPie = document.getElementById('solicitudesPieChart').getContext('2d');
const solicitudesPieChart = new Chart(ctxPie, {
    type: 'pie',
    data: {
        labels: ['Solicitudes Hechas', 'Solicitudes Aceptadas', 'Solicitudes Rechazadas', 'En Proceso'],
        datasets: [{
            label: 'Estado de Solicitudes',
            data: [50, 30, 10, 10],
            backgroundColor: ['blue', 'green', 'orange', 'yellow'],
            borderColor: ['white'],
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'bottom',
            }
        }
    }
});

// Gráfico de Barras para Solicitudes
const ctxBar = document.getElementById('solicitudesBarChart').getContext('2d');
const solicitudesBarChart = new Chart(ctxBar, {
    type: 'bar',
    data: {
        labels: ['Aceptadas', 'Creadas', 'Rechazadas', 'En Proceso'],
        datasets: [{
            label: 'Estado de Solicitudes',
            data: [30, 50, 10, 10],
            backgroundColor: 'rgba(255, 159, 64, 0.6)',
            borderColor: 'rgba(255, 159, 64, 1)',
            borderWidth: 1
        }]
    },
    options: {
        scales: {
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Número de Solicitudes'
                }
            }
        },
        plugins: {
            legend: {
                display: true,
                position: 'top'
            }
        }
    }
});

// Gráfico Combinado para Tendencias
const ctxCombined = document.getElementById('combinedChart').getContext('2d');
const combinedChart = new Chart(ctxCombined, {
    type: 'line',
    data: {
        labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio'],
        datasets: [
            {
                label: 'Usuarios Activos',
                data: [120, 150, 180, 200, 170, 220],
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderWidth: 2,
                fill: false
            },
            {
                label: 'Solicitudes Recibidas',
                data: [30, 45, 55, 40, 60, 70],
                borderColor: 'rgba(255, 99, 132, 1)',
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderWidth: 2,
                fill: false
            }
        ]
    },
    options: {
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Meses'
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Cantidad'
                }
            }
        },
        plugins: {
            legend: {
                position: 'top'
            }
        }
    }
});

function updateClock() {
    const now = new Date();
    const days = ["Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];
    const day = days[now.getDay()];
    const date = now.getDate().toString().padStart(2, '0');
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const year = now.getFullYear();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const seconds = now.getSeconds().toString().padStart(2, '0');

    const clock = document.getElementById('clock');
    clock.innerHTML = `${day}, ${date}/${month}/${year} ${hours}:${minutes}:${seconds}`;
}

// Actualiza el reloj cada segundo
setInterval(updateClock, 1000);

// Inicializa el reloj inmediatamente al cargar la página
updateClock();