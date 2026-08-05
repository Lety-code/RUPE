package mx.unadm.rupe.controller;

import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.service.BitacoraService;
import mx.unadm.rupe.service.ReporteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService reporteService;
    private final BitacoraService bitacoraService;

    public ReporteController(ReporteService reporteService, BitacoraService bitacoraService) {
        this.reporteService = reporteService;
        this.bitacoraService = bitacoraService;
    }

    @GetMapping("/nuevo")
    public String nuevo() {
        return "registrar";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String tutorNombre,
                          @RequestParam String tutorTelefono,
                          @RequestParam(required = false) String tutorCorreo,
                          @RequestParam(required = false) String tutorDireccion,
                          @RequestParam String perroNombre,
                          @RequestParam(required = false) String raza,
                          @RequestParam(required = false) String color,
                          @RequestParam(required = false) String tamano,
                          @RequestParam(required = false) String sexo,
                          @RequestParam(required = false) String senas,
                          @RequestParam(required = false) LocalDate fechaExtravio,
                          @RequestParam String lugarExtravio,
                          @RequestParam(required = false) String descripcion,
                          Model model,
                          HttpServletRequest request) {
        try {
            ReporteExtravio reporte = reporteService.crearReporte(
                    tutorNombre, tutorTelefono, tutorCorreo, tutorDireccion,
                    perroNombre, raza, color, tamano, sexo, senas,
                    fechaExtravio, lugarExtravio, descripcion
            );
            bitacoraService.registrar("PUBLICO", "REPORTES", "Registro de reporte " + reporte.getFolio(), request.getRemoteAddr());
            model.addAttribute("reporte", reporte);
            return "confirmacion";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "registrar";
        } catch (Exception ex) {
            model.addAttribute("error", "No fue posible guardar el reporte. Revise los datos e intente nuevamente.");
            return "registrar";
        }
    }

    @GetMapping("/consulta")
    public String consulta() {
        return "consulta";
    }

    @PostMapping("/consulta")
    public String consultar(@RequestParam String folio, Model model, HttpServletRequest request) {
        Optional<ReporteExtravio> reporte = reporteService.buscarPorFolio(folio);
        if (reporte.isPresent()) {
            bitacoraService.registrar("PUBLICO", "REPORTES", "Consulta de folio " + folio, request.getRemoteAddr());
            model.addAttribute("reporte", reporte.get());
            return "reporte_detalle";
        }
        model.addAttribute("error", "No se encontró un reporte con el folio indicado.");
        return "consulta";
    }

    @GetMapping("/{folio}")
    public String detalle(@PathVariable String folio, Model model) {
        Optional<ReporteExtravio> reporte = reporteService.buscarPorFolio(folio);
        if (reporte.isEmpty()) {
            model.addAttribute("error", "No se encontró el folio.");
            return "consulta";
        }
        model.addAttribute("reporte", reporte.get());
        return "reporte_detalle";
    }
}
