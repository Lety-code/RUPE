package mx.unadm.rupe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/* Controlador de inicio. */
@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
