function togglePassword(input, icon) {
  input.type = input.type === "password" ? "text" : "password";
  icon.classList.toggle("bi-eye-slash-fill", input.type === "password");
  icon.classList.toggle("bi-eye-fill", input.type === "text");
}