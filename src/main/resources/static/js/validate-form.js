function validateForm(form, event, valid) {
    if (!form.checkValidity() || valid) {
        event.preventDefault();
        event.stopPropagation();
    }
    form.classList.add("was-validated");
}