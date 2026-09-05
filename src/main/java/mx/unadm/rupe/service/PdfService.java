package mx.unadm.rupe.service;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import mx.unadm.rupe.model.FichaPdf;
import mx.unadm.rupe.model.ReporteExtravio;
import mx.unadm.rupe.repository.FichaPdfRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
    private final FichaPdfRepository fichaPdfRepository;
    private final QrService qrService;

    public PdfService(FichaPdfRepository fichaPdfRepository,
                  QrService qrService) {
    this.fichaPdfRepository = fichaPdfRepository;
    this.qrService = qrService;
}

    public byte[] generarFicha(ReporteExtravio r) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("RUPE - Registro Único de Perros Extraviados"));
            document.add(new Paragraph("Ficha pública del reporte"));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Folio: " + r.getFolio()));
            document.add(new Paragraph("Estado: " + r.getEstado()));
            document.add(new Paragraph("Nombre del perro: " + r.getPerro().getNombre()));
            document.add(new Paragraph("Raza: " + valor(r.getPerro().getRaza())));
            document.add(new Paragraph("Color: " + r.getPerro().getColor()));
            document.add(new Paragraph("Tamaño: " + valor(r.getPerro().getTamano())));
            document.add(new Paragraph("Sexo: " + valor(r.getPerro().getSexo())));
            document.add(new Paragraph("Señas particulares: " + r.getPerro().getSenasParticulares()));
            document.add(new Paragraph("Fecha de extravío: " + r.getFechaExtravio()));
            document.add(new Paragraph("Lugar de extravío: " + r.getLugarExtravio()));
            document.add(new Paragraph("Descripción: " + valor(r.getDescripcion())));
            
            document.add(new Paragraph(" "));
document.add(new Paragraph("Código QR del reporte:"));

byte[] qrBytes = qrService.generarQrPng(r.getFolio());
Image qrImage = Image.getInstance(qrBytes);
qrImage.scaleToFit(150, 150);
qrImage.setAlignment(Image.ALIGN_CENTER);
document.add(qrImage);

document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Nota: Por privacidad, no se muestran teléfono, correo ni dirección del tutor."));
            document.close();

            FichaPdf ficha = new FichaPdf();
            ficha.setReporte(r);
            ficha.setRutaPdf("ficha_" + r.getFolio() + ".pdf");
            fichaPdfRepository.save(ficha);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF.", ex);
        }
    }

    private String valor(String v) {
        return v == null || v.isBlank() ? "No especificado" : v;
    }
}
