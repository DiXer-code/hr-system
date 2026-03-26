package com.example.hrsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // Показує сторінку логіну
    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    // Обробляє натискання кнопки "Увійти"
    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {

        // Перевіряємо логін та пароль
        if ("admin".equals(username) && "12345".equals(password)) {
            // Якщо все правильно - пускаємо на співробітників
            return "redirect:/employees";
        } else {
            // Якщо помилка - відправляємо повідомлення назад на сторінку
            model.addAttribute("error", "Невірний логін або пароль!");
            return "login";
        }
    }

    // Обробляє натискання "Вихід"
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}