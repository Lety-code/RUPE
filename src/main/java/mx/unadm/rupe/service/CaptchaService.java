package mx.unadm.rupe.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/*
    Servicio CAPTCHA del sistema RUPE.

    Este CAPTCHA es diferente al matemático.
    Ahora genera un código de texto alfanumérico de 5 caracteres.

    Ejemplo:
    Código mostrado: R7K2P
    Respuesta esperada: R7K2P
*/
@Service
public class CaptchaService {

    private static final String CLAVE_CAPTCHA = "captcha_texto";
    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public String generar(HttpSession session) {
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int posicion = random.nextInt(CARACTERES.length());
            codigo.append(CARACTERES.charAt(posicion));
        }

        String captcha = codigo.toString();
        session.setAttribute(CLAVE_CAPTCHA, captcha);

        return captcha;
    }

    public boolean validar(HttpSession session, String respuesta) {
        Object correcto = session.getAttribute(CLAVE_CAPTCHA);

        if (correcto == null || respuesta == null) {
            return false;
        }

        return respuesta.trim().equalsIgnoreCase(correcto.toString());
    }
}
