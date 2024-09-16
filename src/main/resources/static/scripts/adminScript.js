function openNav() {
    document.getElementById("mySidenav").style.left = "0";
    document.getElementById("mySidenav").classList.add('open'); // Añade clase 'open'
}

function closeNav() {
    document.getElementById("mySidenav").style.left = "-250px";
    document.getElementById("mySidenav").classList.remove('open'); // Quita clase 'open'
}

// Event listener para el botón
document.getElementById("sidenavToggleNav").addEventListener("click", function () {
    const sidenav = document.getElementById("mySidenav");
    if (sidenav.classList.contains('open')) {
        closeNav();
    } else {
        openNav();
    }
});