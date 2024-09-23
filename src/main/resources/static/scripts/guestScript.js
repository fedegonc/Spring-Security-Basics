function openNav() {
    document.getElementById("mySidenav").style.left = "0";
    document.getElementById("sidenavToggleNav").style.left = "250px";
}

function closeNav() {
    document.getElementById("mySidenav").style.left = "-250px";
    document.getElementById("sidenavToggleNav").style.left = "0";
}

document.getElementById("sidenavToggleNav").addEventListener("click", function () {
    const sidenav = document.getElementById("mySidenav");
    if (sidenav.style.left === "0px") {
        closeNav();
    } else {
        openNav();
    }
});
window.addEventListener('scroll', function() {
    var navbar = document.getElementById("mainNavbar");

    // Si la página ha hecho scroll más de 50px, añadir la clase 'scrolled'
    if (window.scrollY > 50) {
        navbar.classList.add("scrolled");
    } else {
        navbar.classList.remove("scrolled");
    }
});