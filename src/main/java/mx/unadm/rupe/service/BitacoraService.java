package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Bitacora;
import mx.unadm.rupe.repository.BitacoraRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/* Servicio para guardar y consultar bitácora. */
@Service
public class BitacoraService {
    private final BitacoraRepository repo;

    public BitacoraService(BitacoraRepository repo) {
        this.repo = repo;
    }

    public void registrar(String usuario, String modulo, String accion, String ip) {
        repo.save(new Bitacora(usuario, modulo, accion, ip));
    }

    public List<Bitacora> listar() {
        return repo.findAll();
    }
}
