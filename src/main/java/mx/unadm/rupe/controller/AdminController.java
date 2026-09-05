package mx.unadm.rupe.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.service.BitacoraService;
import mx.unadm.rupe.service.CaptchaService;
import mx.unadm.rupe.service.LoginAttemptService;
import mx.unadm.rupe.service.ReporteService;
import mx.unadm.rupe.service.RespaldoService;
import mx.unadm.rupe.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public AdminController(CaptchaService captchaService,
                           UsuarioService usuarioService,
                           LoginAttemptService loginAttemptService,
                           ReporteService reporteService,
                           BitacoraService bitacoraService,
                           RespaldoService respaldoService) {
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
            String nuevoCaptcha = captchaService.generarCaptcha();
            session.setAttribute("captcha", nuevoCaptcha);
            model.addAttribute("captcha", nuevoCaptcha);
            model.addAttribute(
                    "error",
                    "Demasiados intentos incorrectos. Intente nuevamente más tarde."
            );
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

        Usuario usuarioAutenticado = usuario.get();

        loginAttemptService.registrarExitoso(correo, request.getRemoteAddr());

        session.setAttribute("usuarioAdmin", usuarioAutenticado.getCorreo());
        session.setAttribute("rolUsuario", usuarioAutenticado.getRol().getNombre());

        bitacoraService.registrar(
                usuarioAutenticado.getCorreo(),
                "ADMIN",
                "Inicio de sesión correcto",
                request.getRemoteAddr()
        );

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("rolUsuario", session.getAttribute("rolUsuario"));
        model.addAttribute("totalReportes", reporteService.total());
        model.addAttribute("reportesAbiertos", reporteService.abiertos());
        model.addAttribute("reportesEnRevision", reporteService.enRevision());
        model.addAttribute("reportesConPista", reporteService.conPista());
        model.addAttribute("reportesResguardados", reporteService.resguardados());
        model.addAttribute("reportesLocalizados", reporteService.localizados());
        model.addAttribute("reportesCerrados", reporteService.cerrados());

        return "admin_dashboard";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        cargarUsuariosYRoles(model);
        return "admin_usuarios";
    }

    @PostMapping("/usuarios")
    public String crearUsuario(@RequestParam String nombre,
                               @RequestParam String correo,
                               @RequestParam String password,
                               @RequestParam Long rolId,
                               Model model,
                               HttpSession session,
                               HttpServletRequest request) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        try {
            Usuario nuevo = usuarioService.crearUsuario(
                    nombre,
                    correo,
                    password,
                    rolId
            );

            String admin = (String) session.getAttribute("usuarioAdmin");

            bitacoraService.registrar(
                    admin,
                    "USUARIOS",
                    "Creación de usuario " + nuevo.getCorreo(),
                    request.getRemoteAddr()
            );

            model.addAttribute("mensaje", "Usuario creado correctamente.");

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        cargarUsuariosYRoles(model);
        return "admin_usuarios";
    }

    @PostMapping("/usuarios/{id}/activo")
    public String cambiarActivoUsuario(@PathVariable Long id,
                                       @RequestParam boolean activo,
                                       Model model,
                                       HttpSession session,
                                       HttpServletRequest request) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        try {
            Usuario usuario = usuarioService.cambiarActivo(id, activo);
            String admin = (String) session.getAttribute("usuarioAdmin");

            bitacoraService.registrar(
                    admin,
                    "USUARIOS",
                    (activo ? "Activación de usuario " : "Desactivación de usuario ")
                    + usuario.getCorreo(),
                    request.getRemoteAddr()
            );

            model.addAttribute(
                    "mensaje",
                    activo
                            ? "Usuario activado correctamente."
                            : "Usuario desactivado correctamente."
            );

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        cargarUsuariosYRoles(model);
        return "admin_usuarios";
    }

    @GetMapping("/reportes")
    public String reportes(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("reportes", reporteService.listarReportes());
        model.addAttribute("rolUsuario", session.getAttribute("rolUsuario"));

        return "admin_reportes";
    }

    @PostMapping("/reportes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                HttpSession session,
                                HttpServletRequest request) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!puedeModificarReportes(session)) {
            return "redirect:/admin/reportes";
        }

        ReporteExtravio reporte = reporteService.cambiarEstado(id, estado);

        bitacoraService.registrar(
                (String) session.getAttribute("usuarioAdmin"),
                "REPORTES",
                "Cambio de estado a " + estado + " en " + reporte.getFolio(),
                request.getRemoteAddr()
        );

        return "redirect:/admin/reportes";
    }

    @GetMapping("/bitacora")
    public String bitacora(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("acciones", bitacoraService.listar());
        return "admin_bitacora";
    }

    @GetMapping("/graficas")
    public String graficas(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("abiertos", reporteService.abiertos());
        model.addAttribute("enRevision", reporteService.enRevision());
        model.addAttribute("conPista", reporteService.conPista());
        model.addAttribute("resguardados", reporteService.resguardados());
        model.addAttribute("localizados", reporteService.localizados());
        model.addAttribute("cerrados", reporteService.cerrados());

        return "admin_graficas";
    }

    @GetMapping("/exportar")
    public void exportar(HttpServletResponse response,
                         HttpSession session) throws IOException {

        if (!sesionActiva(session)) {
            response.sendRedirect("/admin/login");
            return;
        }

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=reportes_rupe.csv"
        );

        List<ReporteExtravio> reportes = reporteService.listarReportes();
        PrintWriter writer = response.getWriter();

        writer.println("Folio,Estado,Fecha extravio,Lugar,Perro,Color");

        for (ReporteExtravio reporte : reportes) {
            writer.printf(
                    "%s,%s,%s,%s,%s,%s%n",
                    limpiar(reporte.getFolio()),
                    limpiar(reporte.getEstado()),
                    reporte.getFechaExtravio() == null ? "" : reporte.getFechaExtravio(),
                    limpiar(reporte.getLugarExtravio()),
                    reporte.getPerro() == null ? "" : limpiar(reporte.getPerro().getNombre()),
                    reporte.getPerro() == null ? "" : limpiar(reporte.getPerro().getColor())
            );
        }

        writer.flush();
    }

    @GetMapping("/respaldos")
    public String respaldos(Model model, HttpSession session) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        var listaRespaldos = respaldoService.listar();

        Map<Long, Boolean> respaldosCompletos = new HashMap<>();
        Map<Long, Boolean> respaldosRestaurablesLocalH2 = new HashMap<>();

        for (var respaldo : listaRespaldos) {
            respaldosCompletos.put(
                    respaldo.getId(),
                    respaldoService.esRespaldoCompleto(respaldo.getId())
            );

            respaldosRestaurablesLocalH2.put(
                    respaldo.getId(),
                    respaldoService.esRespaldoRestaurableLocalH2(respaldo.getId())
            );
        }

        model.addAttribute("respaldos", listaRespaldos);
        model.addAttribute("respaldosCompletos", respaldosCompletos);
        model.addAttribute(
                "respaldosRestaurablesLocalH2",
                respaldosRestaurablesLocalH2
        );

        try {
            model.addAttribute(
                    "tipoBaseDatos",
                    respaldoService.obtenerTipoBaseDatosActual()
            );
        } catch (IOException e) {
            model.addAttribute("tipoBaseDatos", "DESCONOCIDA");
            model.addAttribute(
                    "error",
                    "No se pudo identificar la base de datos activa."
            );
        }

        return "admin_respaldos";
    }

    @PostMapping("/respaldos")
    public String crearRespaldo(HttpSession session,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (!sesionActiva(session)) {
            return "redirect:/admin/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/admin/dashboard";
        }

        String usuario = (String) session.getAttribute("usuarioAdmin");

        try {
            var respaldo = respaldoService.crearRespaldo(usuario);

            bitacoraService.registrar(
                    usuario,
                    "RESPALDOS",
                    "Creación de respaldo completo " + respaldo.getNombreArchivo(),
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Respaldo completo creado correctamente."
            );

        } catch (IOException e) {

            bitacoraService.registrar(
                    usuario,
                    "RESPALDOS",
                    "Error al crear respaldo: " + mensajeSeguro(e),
                    request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se pudo crear el respaldo: " + mensajeSeguro(e)
            );
        }

        return "redirect:/admin/respaldos";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletRequest request) {

        String usuario = (String) session.getAttribute("usuarioAdmin");

        if (usuario != null) {
            bitacoraService.registrar(
                    usuario,
                    "ADMIN",
                    "Cierre de sesión",
                    request.getRemoteAddr()
            );
        }

        session.invalidate();
        return "redirect:/";
    }

    private void cargarUsuariosYRoles(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        model.addAttribute("roles", usuarioService.listarRoles());
    }

    private boolean sesionActiva(HttpSession session) {
        return session != null
                && session.getAttribute("usuarioAdmin") != null;
    }

    private boolean esAdministrador(HttpSession session) {
        return session != null
                && "ADMINISTRADOR".equalsIgnoreCase(
                        String.valueOf(session.getAttribute("rolUsuario"))
                );
    }

    private boolean puedeModificarReportes(HttpSession session) {

        if (session == null) {
            return false;
        }

        String rol = String.valueOf(session.getAttribute("rolUsuario"));

        return "ADMINISTRADOR".equalsIgnoreCase(rol)
                || "CAPTURISTA".equalsIgnoreCase(rol);
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace(",", " ")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private String mensajeSeguro(Exception e) {
        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            return "Error no especificado.";
        }

        return e.getMessage();
    }
}
