const sliderTrack = document.querySelector('.slider-track');

// Pausar la animación al pasar el cursor
sliderTrack.addEventListener('mouseenter', () => {
    sliderTrack.style.animationPlayState = 'paused';
});

// Reanudar la animación al quitar el cursor
sliderTrack.addEventListener('mouseleave', () => {
    sliderTrack.style.animationPlayState = 'running';
});
