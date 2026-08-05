package mx.unadm.rupe.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class CaptchaService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public String generarCaptcha() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public boolean validar(String esperado, String respuesta) {
        if (esperado == null || respuesta == null) {
            return false;
        }
        return esperado.trim().equalsIgnoreCase(respuesta.trim());
    }
}
