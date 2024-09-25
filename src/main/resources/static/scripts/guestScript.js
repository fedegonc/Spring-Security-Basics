
window.addEventListener("scroll", function() {
    const navbar = document.getElementById("mainNavbar");
    const logo = document.getElementById("navbarLogo");

    if (window.scrollY > 50) { // Cambia el valor según tu preferencia
        navbar.classList.add("scrolled");
        logo.style.width = "30px"; // Cambia el tamaño del logo al hacer scroll
    } else {
        navbar.classList.remove("scrolled");
        logo.style.width = "40px"; // Vuelve al tamaño original
    }
});
