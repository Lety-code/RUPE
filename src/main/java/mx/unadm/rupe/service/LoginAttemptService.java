package mx.unadm.rupe.service;

import mx.unadm.rupe.model.IntentoLogin;
import mx.unadm.rupe.repository.IntentoLoginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {
    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_VENTANA = 15;
    private static final int MINUTOS_BLOQUEO = 15;

    private final IntentoLoginRepository intentoLoginRepository;

    public LoginAttemptService(IntentoLoginRepository intentoLoginRepository) {
        this.intentoLoginRepository = intentoLoginRepository;
    }

    public boolean estaBloqueado(String correo) {
        if (correo == null || correo.isBlank()) return false;
        LocalDateTime desde = LocalDateTime.now().minusMinutes(MINUTOS_VENTANA);
        long fallos = intentoLoginRepository.countByCorreoIgnoreCaseAndExitosoFalseAndFechaHoraAfter(correo.trim(), desde);
        return fallos >= MAX_INTENTOS;
    }

    @Transactional
    public void registrarExitoso(String correo, String ip) {
        IntentoLogin intento = new IntentoLogin();
        intento.setCorreo(correo == null ? "" : correo.trim());
        intento.setIp(ip);
        intento.setExitoso(true);
        intentoLoginRepository.save(intento);
        intentoLoginRepository.deleteByCorreoIgnoreCaseAndExitosoFalse(correo == null ? "" : correo.trim());
    }

    public void registrarFallo(String correo, String ip) {
        IntentoLogin intento = new IntentoLogin();
        intento.setCorreo(correo == null ? "" : correo.trim());
        intento.setIp(ip);
        intento.setExitoso(false);
        if (estaBloqueado(correo)) {
            intento.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        intentoLoginRepository.save(intento);
    }
}
