package io.github.augustoravazoli.inventorymanagementsystem.user;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
        } catch (UserEmailTakenException e) {
            model.addAttribute("duplicatedEmail", true);
            model.addAttribute("user", user);
            return "user/register";
        }
        return "user/success";
    }

    @PostMapping("/delete-account")
    public String disableUser(@AuthenticationPrincipal User user) {
        userService.disableUser(user);
        return "forward:/logout";
    }

}
