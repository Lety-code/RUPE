package mx.unadm.rupe.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrService {
    @Value("${rupe.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public String contenidoParaFolio(String folio) {
        return publicBaseUrl + "/reportes/" + folio;
    }

    public byte[] generarQrPng(String folio) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenidoParaFolio(folio), BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("No se pudo generar el código QR.", ex);
        }
    }
}
