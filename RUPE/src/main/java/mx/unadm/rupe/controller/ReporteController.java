package mx.unadm.rupe.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.service.ReporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/* Controlador de registro y consulta de reportes. */
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("reporte", new ReporteExtravio());
        return "registrar";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ReporteExtravio reporte, HttpServletRequest request) {
        ReporteExtravio guardado = service.guardar(reporte, request.getRemoteAddr());
        return "redirect:/reportes/confirmacion/" + guardado.getFolio();
    }

    @GetMapping("/confirmacion/{folio}")
    public String confirmacion(@PathVariable String folio, Model model) {
        model.addAttribute("folio", folio);
        return "confirmacion";
    }

    @GetMapping("/consulta")
    public String consulta() {
        return "consulta";
    }

    @PostMapping("/consulta")
    public String consultar(@RequestParam String folio, Model model) {
        var reporte = service.buscarFolio(folio.trim());
        if (reporte.isEmpty()) {
            model.addAttribute("error", "No se encontró un reporte con ese folio.");
            return "consulta";
        }
        model.addAttribute("reporte", reporte.get());
        return "reporte_detalle";
    }
}
