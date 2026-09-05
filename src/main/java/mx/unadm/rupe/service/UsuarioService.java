package mx.unadm.rupe.service;

import mx.unadm.rupe.model.Rol;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.RolRepository;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> validarAcceso(String correo, String password) {

        if (correo == null || password == null) {
            return Optional.empty();
        }

        Optional<Usuario> usuario =
                usuarioRepository.findByCorreoIgnoreCase(correo.trim());

        if (usuario.isEmpty() || !usuario.get().isActivo()) {
            return Optional.empty();
        }

        if (passwordEncoder.matches(password, usuario.get().getPassword())) {
            return usuario;
        }

        return Optional.empty();
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public Usuario crearUsuario(String nombre,
                                String correo,
                                String password,
                                Long rolId) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del usuario es obligatorio.");
        }

        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria.");
        }

        if (rolId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol.");
        }

        String correoLimpio = correo.trim();

        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese correo.");
        }

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El rol seleccionado no existe."));

        if (!rol.isActivo()) {
            throw new IllegalArgumentException(
                    "El rol seleccionado está inactivo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correoLimpio);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarActivo(Long id, boolean activo) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no existe."));

        if ("usadminrupe@rupe.com".equalsIgnoreCase(usuario.getCorreo())
                && !activo) {
            throw new IllegalArgumentException(
                    "El administrador principal no puede desactivarse.");
        }

        usuario.setActivo(activo);

        return usuarioRepository.save(usuario);
    }
}