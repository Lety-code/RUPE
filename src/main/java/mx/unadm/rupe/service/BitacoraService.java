package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Bitacora;
import mx.unadm.rupe.repository.BitacoraRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BitacoraService {
    private final BitacoraRepository bitacoraRepository;

    public BitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public void registrar(String usuario, String modulo, String accion, String ip) {
        Bitacora b = new Bitacora();
        b.setUsuario(usuario);
        b.setModulo(modulo);
        b.setAccion(accion);
        b.setIp(ip);
        bitacoraRepository.save(b);
    }

    public List<Bitacora> listar() {
        return bitacoraRepository.findAllByOrderByFechaHoraDesc();
    }
}
