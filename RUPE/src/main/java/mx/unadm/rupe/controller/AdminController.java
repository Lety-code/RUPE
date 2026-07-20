package mx.unadm.rupe.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.unadm.rupe.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/* Controlador administrativo con login, CAPTCHA, usuarios y bitácora. */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UsuarioService usuarios;
    private final ReporteService reportes;
    private final BitacoraService bitacora;
    private final CaptchaService captcha;

    public AdminController(UsuarioService usuarios, ReporteService reportes, BitacoraService bitacora, CaptchaService captcha) {
        this.usuarios = usuarios;
        this.reportes = reportes;
        this.bitacora = bitacora;
        this.captcha = captcha;
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        model.addAttribute("pregunta", captcha.generar(session));
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam String correo, @RequestParam String password, @RequestParam String respuesta,
                            HttpSession session, HttpServletRequest request, Model model) {
        if (!captcha.validar(session, respuesta)) {
            model.addAttribute("error", "CAPTCHA incorrecto.");
            model.addAttribute("pregunta", captcha.generar(session));
            return "login";
        }
        var usuario = usuarios.login(correo, password, request.getRemoteAddr());
        if (usuario.isEmpty()) {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            model.addAttribute("pregunta", captcha.generar(session));
            return "login";
        }
        session.setAttribute("usuario", usuario.get());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) return "redirect:/admin/login";
        model.addAttribute("total", reportes.total());
        return "admin_dashboard";
    }

    @GetMapping("/usuarios")
    public String usuarios(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) return "redirect:/admin/login";
        model.addAttribute("usuarios", usuarios.listar());
        return "admin_usuarios";
    }

    @GetMapping("/bitacora")
    public String bitacora(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) return "redirect:/admin/login";
        model.addAttribute("acciones", bitacora.listar());
        return "admin_bitacora";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
