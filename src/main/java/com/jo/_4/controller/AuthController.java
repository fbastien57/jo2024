package com.jo._4.controller;

import com.jo._4.entity.User;
import com.jo._4.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // --- FORMULAIRE D'INSCRIPTION ---
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "/AllAccess/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model) {

        // 1️⃣ Validation des champs (mot de passe, email, etc.)
        if (result.hasErrors()) {
            return "/AllAccess/register";
        }

        // 2️⃣ Vérification de l’unicité de l’email
        if (userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", null, "Cet email est déjà utilisé");
            return "/AllAccess/register";
        }

        // 3️⃣ Si tout est bon → enregistrement
        userService.registerUser(user);

        // 4️⃣ Redirection vers la page de connexion avec message de succès
        model.addAttribute("successMessage", "Votre compte a été créé avec succès !");
        return "redirect:/AllAccess/login?success";
    }

    // --- FORMULAIRE DE CONNEXION ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "/AllAccess/login";
    }
}
