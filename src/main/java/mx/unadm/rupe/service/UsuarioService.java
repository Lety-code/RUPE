package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> validarAcceso(String correo, String password) {
        if (correo == null || password == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .filter(Usuario::isActivo)
                .filter(u -> u.getPassword().equals(password));
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
}
