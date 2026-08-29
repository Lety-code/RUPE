package mx.unadm.rupe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class ArchivoService {
    @Value("${rupe.upload-dir:uploads}")
    private String uploadDir;

    public String guardarImagen(MultipartFile file, String subcarpeta) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename() == null ? "imagen" : file.getOriginalFilename();
        String ext = obtenerExtension(original);
        if (!ext.matches("(?i)\\.(png|jpg|jpeg|webp)")) {
            throw new IllegalArgumentException("Sólo se permiten imágenes PNG, JPG, JPEG o WEBP.");
        }
        Path dir = Paths.get(uploadDir, subcarpeta).toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String nombre = UUID.randomUUID() + ext.toLowerCase(Locale.ROOT);
        Path destino = dir.resolve(nombre);
        file.transferTo(destino.toFile());
        return "/uploads/" + subcarpeta + "/" + nombre;
    }

    private String obtenerExtension(String nombre) {
        int pos = nombre.lastIndexOf('.');
        return pos >= 0 ? nombre.substring(pos) : "";
    }
}
