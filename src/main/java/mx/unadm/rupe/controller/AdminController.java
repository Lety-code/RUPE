package mx.unadm.rupe.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final CaptchaService captchaService;
    private final UsuarioService usuarioService;
    private final LoginAttemptService loginAttemptService;
    private final ReporteService reporteService;
    private final BitacoraService bitacoraService;
    private final RespaldoService respaldoService;

    public AdminController(CaptchaService captchaService, UsuarioService usuarioService, LoginAttemptService loginAttemptService,
                           ReporteService reporteService, BitacoraService bitacoraService, RespaldoService respaldoService) {
        this.captchaService = captchaService;
        this.usuarioService = usuarioService;
        this.loginAttemptService = loginAttemptService;
        this.reporteService = reporteService;
        this.bitacoraService = bitacoraService;
        this.respaldoService = respaldoService;
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
        if (loginAttemptService.estaBloqueado(correo)) {
            model.addAttribute("captcha", captchaService.generarCaptcha());
            model.addAttribute("error", "Demasiados intentos incorrectos. Intente nuevamente más tarde.");
            return "login";
        }

        String esperado = (String) session.getAttribute("captcha");
        if (!captchaService.validar(esperado, captcha)) {
            loginAttemptService.registrarFallo(correo, request.getRemoteAddr());
            String nuevoCaptcha = captchaService.generarCaptcha();
            session.setAttribute("captcha", nuevoCaptcha);
            model.addAttribute("captcha", nuevoCaptcha);
            model.addAttribute("error", "CAPTCHA incorrecto. Intente nuevamente.");
            return "login";
        }

        Optional<Usuario> usuario = usuarioService.validarAcceso(correo, password);
        if (usuario.isEmpty()) {
            loginAttemptService.registrarFallo(correo, request.getRemoteAddr());
            String nuevoCaptcha = captchaService.generarCaptcha();
            session.setAttribute("captcha", nuevoCaptcha);
            model.addAttribute("captcha", nuevoCaptcha);
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "login";
        }

        loginAttemptService.registrarExitoso(correo, request.getRemoteAddr());
        session.setAttribute("usuarioAdmin", usuario.get().getCorreo());
        bitacoraService.registrar(usuario.get().getCorreo(), "ADMIN", "Inicio de sesión correcto", request.getRemoteAddr());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("totalReportes", reporteService.total());
        model.addAttribute("reportesAbiertos", reporteService.abiertos());
        model.addAttribute("reportesLocalizados", reporteService.localizados());
        model.addAttribute("reportesCerrados", reporteService.cerrados());
        return "admin_dashboard";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin_usuarios";
    }

    @GetMapping("/reportes")
    public String reportes(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("reportes", reporteService.listarReportes());
        return "admin_reportes";
    }

    @PostMapping("/reportes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String estado, HttpSession session, HttpServletRequest request) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        ReporteExtravio r = reporteService.cambiarEstado(id, estado);
        bitacoraService.registrar((String) session.getAttribute("usuarioAdmin"), "REPORTES", "Cambio de estado a " + estado + " en " + r.getFolio(), request.getRemoteAddr());
        return "redirect:/admin/reportes";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("acciones", bitacoraService.listar());
        return "admin_bitacora";
    }

    @GetMapping("/graficas")
    public String graficas(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("abiertos", reporteService.abiertos());
        model.addAttribute("localizados", reporteService.localizados());
        model.addAttribute("cerrados", reporteService.cerrados());
        return "admin_graficas";
    }

    @GetMapping("/exportar")
    public void exportar(HttpServletResponse response, HttpSession session) throws IOException {
        if (!sesionActiva(session)) {
            response.sendRedirect("/admin/login");
            return;
        }
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=reportes_rupe.csv");
        List<ReporteExtravio> reportes = reporteService.listarReportes();
        PrintWriter writer = response.getWriter();
        writer.println("Folio,Estado,Fecha extravio,Lugar,Perro,Color");
        for (ReporteExtravio r : reportes) {
            writer.printf("%s,%s,%s,%s,%s,%s%n", r.getFolio(), r.getEstado(), r.getFechaExtravio(), limpiar(r.getLugarExtravio()), limpiar(r.getPerro().getNombre()), limpiar(r.getPerro().getColor()));
        }
    }

    @GetMapping("/respaldos")
    public String respaldos(Model model, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        model.addAttribute("respaldos", respaldoService.listar());
        return "admin_respaldos";
    }

    @PostMapping("/respaldos")
    public String crearRespaldo(HttpSession session, HttpServletRequest request) throws IOException {
        if (!sesionActiva(session)) return "redirect:/admin/login";
        String usuario = (String) session.getAttribute("usuarioAdmin");
        respaldoService.crearRespaldo(usuario);
        bitacoraService.registrar(usuario, "RESPALDOS", "Creación de respaldo", request.getRemoteAddr());
        return "redirect:/admin/respaldos";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        String usuario = (String) session.getAttribute("usuarioAdmin");
        if (usuario != null) bitacoraService.registrar(usuario, "ADMIN", "Cierre de sesión", request.getRemoteAddr());
        session.invalidate();
        return "redirect:/";
    }

    private boolean sesionActiva(HttpSession session) {
        return session != null && session.getAttribute("usuarioAdmin") != null;
    }

    private String limpiar(String v) {
        return v == null ? "" : v.replace(",", " ").replace("\n", " ").replace("\r", " ");
    }
}
