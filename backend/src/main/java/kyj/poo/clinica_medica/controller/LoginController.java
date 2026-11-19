package kyj.poo.clinica_medica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String exibirTelaLogin() {
        return "login";
    }
}