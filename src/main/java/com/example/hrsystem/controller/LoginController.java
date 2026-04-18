package com.example.hrsystem.controller;

import com.example.hrsystem.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    public static final String SESSION_USER = "AUTH_USER";

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(@RequestParam(value = "logout", required = false) String logout,
                            HttpSession session,
                            Model model) {
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/reports";
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Сеанс завершено.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        if (isAuthenticatedAgainstRepository(username, password) || isDemoCredentials(username, password)) {
            session.setAttribute(SESSION_USER, username.trim());
            session.setMaxInactiveInterval(8 * 60 * 60);
            return "redirect:/reports";
        }

        model.addAttribute("error", "Невірний логін або пароль.");
        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    private boolean isAuthenticatedAgainstRepository(String username, String password) {
        try {
            return userRepository.findByUsernameIgnoreCase(username.trim())
                    .map(user -> password.equals(user.getPassword()))
                    .orElse(false);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isDemoCredentials(String username, String password) {
        return "admin".equalsIgnoreCase(username.trim()) && "12345".equals(password);
    }
}
