package io.github.augustoravazoli.inventorymanagementsystem.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String retrieveRegisterUserPage(Model model) {
        model.addAttribute("user", new UserForm());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UserForm user, Model model) {
        try {
            userService.registerUser(user.toEntity());
            model.addAttribute("email", user.getEmail());
        } catch (UserEmailTakenException e) {
            model.addAttribute("duplicatedEmail", true);
            model.addAttribute("user", user);
            return "user/register";
        }
        return "user/verify-account";
    }

    @GetMapping("/verify-account")
    public String verifyAccount(@Valid @RequestParam(name = "token") @NotBlank String token) {
        try {
            userService.verifyAccount(token);
        } catch (TokenExpiredException e) {
            return "user/account-expired";
        }
        return "user/account-verified";
    }

    @PostMapping("/resend-verification-email")
    public String resendVerificationEmail(@Valid @RequestParam(name = "email") @Email @NotBlank String email, Model model) {
        userService.resendVerificationEmail(email);
        model.addAttribute("email", email);
        return "user/verify-account";
    }

    @GetMapping("/request-password-reset")
    public String retrieveRequestPasswordResetPage(Model model) {
        model.addAttribute("email", null);
        return "user/request-password-reset-form";
    }

    @PostMapping("/request-password-reset")
    public String requestPasswordReset(@Valid @ModelAttribute(name = "email") @Email @NotBlank String email, Model model) {
        try {
            userService.sendPasswordResetEmail(email);
        } catch (NonexistentUserException e) {
            model.addAttribute("userNotFound", true);
            model.addAttribute("email", email);
            return "user/request-password-reset-form";
        }
        return "user/request-password-reset-success";
    }

    @GetMapping("/reset-password")
    public String retrieveResetPasswordPage(@Valid @RequestParam(name = "token") @NotBlank String token, Model model) {
        try {
            userService.validatePasswordResetToken(token);
            model.addAttribute("token", token);
        } catch (TokenExpiredException e) {
            return "user/expired-password-reset-request";
        }
        return "user/reset-password-form";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @RequestParam(name = "new-password") @NotBlank String newPassword,
            @Valid @RequestParam(name = "token") @NotBlank String token
    ) {
        try {
            userService.resetPassword(newPassword, token);
        } catch (TokenExpiredException e) {
            return "user/expired-password-reset-request";
        }
        return "user/password-updated";
    }

    @PostMapping("/update-password")
    public String updateUserPassword(
            @Valid @RequestParam(name = "password") @NotBlank String password,
            @Valid @RequestParam(name = "new-password") @NotBlank String newPassword,
            @AuthenticationPrincipal User user
    ) {
        try {
            userService.updatePassword(password, newPassword, user);
        } catch (PasswordMismatchException e) {
            return "redirect:/settings?update-password-error";
        }
        return "redirect:/settings?update-password-success";
    }

    @PostMapping("/delete-account")
    public String disableUser(@AuthenticationPrincipal User user) {
        userService.disableUser(user);
        return "forward:/logout";
    }

}
