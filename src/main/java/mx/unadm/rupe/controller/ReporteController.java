package mx.unadm.rupe.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService reporteService;
    private final PistaService pistaService;
    private final BitacoraService bitacoraService;
    private final QrService qrService;
    private final PdfService pdfService;

    public ReporteController(ReporteService reporteService, PistaService pistaService, BitacoraService bitacoraService, QrService qrService, PdfService pdfService) {
        this.reporteService = reporteService;
        this.pistaService = pistaService;
        this.bitacoraService = bitacoraService;
        this.qrService = qrService;
        this.pdfService = pdfService;
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
                          @RequestParam String color,
                          @RequestParam(required = false) String tamano,
                          @RequestParam(required = false) String sexo,
                          @RequestParam String senas,
                          @RequestParam String fechaExtravio,
                          @RequestParam String lugarExtravio,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam(required = false) MultipartFile foto,
                          Model model,
                          HttpServletRequest request) {
        try {
            ReporteExtravio reporte = reporteService.crearReporte(
                    tutorNombre, tutorTelefono, tutorCorreo, tutorDireccion,
                    perroNombre, raza, color, tamano, sexo, senas,
                    fechaExtravio, lugarExtravio, descripcion, foto
            );
            bitacoraService.registrar("PUBLICO", "REPORTES", "Registro de reporte " + reporte.getFolio(), request.getRemoteAddr());
            model.addAttribute("reporte", reporte);
            model.addAttribute("qrUrl", qrService.contenidoParaFolio(reporte.getFolio()));
            return "confirmacion";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "registrar";
        }
    }

    @GetMapping("/consulta")
    public String consulta() {
        return "consulta";
    }

    @PostMapping("/consulta")
    public String consultar(@RequestParam String folio, Model model, HttpServletRequest request) {
        return mostrarReporte(folio, model, request, true);
    }

    @GetMapping("/{folio}")
    public String detalle(@PathVariable String folio, Model model, HttpServletRequest request) {
        return mostrarReporte(folio, model, request, false);
    }

    private String mostrarReporte(String folio, Model model, HttpServletRequest request, boolean desdeConsulta) {
        Optional<ReporteExtravio> reporte = reporteService.buscarPorFolio(folio);
        if (reporte.isEmpty()) {
            model.addAttribute("error", "No se encontró un reporte con el folio indicado.");
            return desdeConsulta ? "consulta" : "error";
        }
        bitacoraService.registrar("PUBLICO", "CONSULTA", "Consulta de folio " + folio, request.getRemoteAddr());
        model.addAttribute("reporte", reporte.get());
        model.addAttribute("pistas", pistaService.listarPorFolio(reporte.get().getFolio()));
        model.addAttribute("qrUrl", qrService.contenidoParaFolio(reporte.get().getFolio()));
        return "reporte_detalle";
    }

    @PostMapping("/{folio}/pistas")
    public String registrarPista(@PathVariable String folio,
                                 @RequestParam String lugar,
                                 @RequestParam(required = false) String fecha,
                                 @RequestParam(required = false) String hora,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam(required = false, defaultValue = "false") boolean resguardado,
                                 @RequestParam(required = false) MultipartFile foto,
                                 Model model,
                                 HttpServletRequest request) {
        try {
            pistaService.registrarPista(folio, lugar, fecha, hora, descripcion, resguardado, foto);
            bitacoraService.registrar("PUBLICO", "PISTAS", "Registro de pista para folio " + folio, request.getRemoteAddr());
            return "redirect:/reportes/" + folio;
        } catch (IOException | IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarReporte(folio, model, request, false);
        }
    }

    @GetMapping("/{folio}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable String folio) {
        byte[] png = qrService.generarQrPng(folio);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/{folio}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String folio) {
        ReporteExtravio reporte = reporteService.buscarPorFolio(folio)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado."));
        byte[] pdf = pdfService.generarFicha(reporte);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ficha_" + folio + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
