package mx.unadm.rupe.controller;

import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UsuarioService usuarioService;
    private final CaptchaService captchaService;
    private final BitacoraService bitacoraService;
    private final ReporteService reporteService;

    public AdminController(UsuarioService usuarioService,
                           CaptchaService captchaService,
                           BitacoraService bitacoraService,
                           ReporteService reporteService) {
        this.usuarioService = usuarioService;
        this.captchaService = captchaService;
        this.bitacoraService = bitacoraService;
        this.reporteService = reporteService;
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        String captcha = captchaService.generarCaptcha();
        session.setAttribute("captcha", captcha);
        model.addAttribute("captcha", captcha);
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String password,
                                @RequestParam String captcha,
                                Model model,
                                HttpSession session,
                                HttpServletRequest request) {
        String esperado = (String) session.getAttribute("captcha");

        if (!captchaService.validar(esperado, captcha)) {
            String nuevoCaptcha = captchaService.generarCaptcha();
            session.setAttribute("captcha", nuevoCaptcha);
            model.addAttribute("captcha", nuevoCaptcha);
            model.addAttribute("error", "CAPTCHA incorrecto. Intente nuevamente.");
            return "login";
        }

        Optional<Usuario> usuario = usuarioService.validarAcceso(correo, password);
        if (usuario.isEmpty()) {
            String nuevoCaptcha = captchaService.generarCaptcha();
            session.setAttribute("captcha", nuevoCaptcha);
            model.addAttribute("captcha", nuevoCaptcha);
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";
        }

        session.setAttribute("usuarioAdmin", usuario.get().getCorreo());
        bitacoraService.registrar(usuario.get().getCorreo(), "ADMIN", "Inicio de sesión correcto", request.getRemoteAddr());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("totalReportes", reporteService.total());
        model.addAttribute("reportesAbiertos", reporteService.abiertos());
        model.addAttribute("reportesLocalizados", reporteService.localizados());
        return "admin_dashboard";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {
        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin_usuarios";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model, HttpSession session) {
        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("acciones", bitacoraService.listar());
        return "admin_bitacora";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        Object usuario = session.getAttribute("usuarioAdmin");
        if (usuario != null) {
            bitacoraService.registrar(usuario.toString(), "ADMIN", "Cierre de sesión", request.getRemoteAddr());
        }
        session.invalidate();
        return "redirect:/admin/login?salida=true";
    }

    private boolean sesionActiva(HttpSession session) {
        return session.getAttribute("usuarioAdmin") != null;
    }
}
