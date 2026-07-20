package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/* Servicio para login y consulta de usuarios. */
@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final BitacoraService bitacora;

    public UsuarioService(UsuarioRepository usuarios, BitacoraService bitacora) {
        this.usuarios = usuarios;
        this.bitacora = bitacora;
    }

    public Optional<Usuario> login(String correo, String password, String ip) {
        Optional<Usuario> u = usuarios.findByCorreo(correo);
        if (u.isPresent() && Boolean.TRUE.equals(u.get().getActivo()) && u.get().getPassword().equals(password)) {
            bitacora.registrar(correo, "Seguridad", "Inicio de sesión correcto", ip);
            return u;
        }
        bitacora.registrar(correo, "Seguridad", "Intento de inicio incorrecto", ip);
        return Optional.empty();
    }

    public List<Usuario> listar() {
        return usuarios.findAll();
    }
}
