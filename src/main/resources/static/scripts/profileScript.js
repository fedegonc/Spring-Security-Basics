function previewFile() {
    const preview = document.getElementById('currentProfileImage') || document.getElementById('defaultProfileImage');
    const file = document.getElementById('file').files[0];
    const reader = new FileReader();

    reader.addEventListener("load", function () {
        preview.src = reader.result;
    }, false);

    if (file) {
        reader.readAsDataURL(file);
    }
}