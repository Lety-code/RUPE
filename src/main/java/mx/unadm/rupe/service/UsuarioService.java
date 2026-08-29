package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> validarAcceso(String correo, String password) {
        if (correo == null || password == null) return Optional.empty();
        Optional<Usuario> usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim());
        if (usuario.isEmpty() || !usuario.get().isActivo()) return Optional.empty();
        if (passwordEncoder.matches(password, usuario.get().getPassword())) {
            return usuario;
        }
        return Optional.empty();
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
}
