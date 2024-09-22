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