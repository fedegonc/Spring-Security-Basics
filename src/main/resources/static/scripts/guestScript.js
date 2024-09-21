window.addEventListener('scroll', function() {
    const navbar = document.getElementById('mainNavbar');
    const logo = document.getElementById('navbarLogo');

    if (window.scrollY > 10) { // Cambia el valor según necesites
        navbar.classList.add('scrolled');
        logo.classList.add('small-logo');
    } else {
        navbar.classList.remove('scrolled');
        logo.classList.remove('small-logo');
    }
});
