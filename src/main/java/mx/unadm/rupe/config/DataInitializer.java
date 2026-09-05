package mx.unadm.rupe.config;

import mx.unadm.rupe.model.ConfiguracionSistema;
import mx.unadm.rupe.model.Rol;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.ConfiguracionSistemaRepository;
import mx.unadm.rupe.repository.RolRepository;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionSistemaRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                           ConfiguracionSistemaRepository configuracionRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracionRepository = configuracionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol admin = rolRepository.findByNombre("ADMINISTRADOR").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("ADMINISTRADOR");
            r.setDescripcion("Usuario con acceso al panel administrativo.");
            return rolRepository.save(r);
        });
        
        Rol capturista = rolRepository.findByNombre("CAPTURISTA").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("CAPTURISTA");
            r.setDescripcion("Usuario autorizado para capturar y actualizar información.");
            r.setActivo(true);
            return rolRepository.save(r);
});

        Rol consulta = rolRepository.findByNombre("CONSULTA").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("CONSULTA");
            r.setDescripcion("Usuario con acceso de consulta a la información.");
            r.setActivo(true);
            return rolRepository.save(r);
});

        if (!usuarioRepository.existsByCorreoIgnoreCase("usadminrupe@rupe.com")) {
            Usuario u = new Usuario();
            u.setNombre("Administrador RUPE");
            u.setCorreo("usadminrupe@rupe.com");
            u.setPassword(passwordEncoder.encode("rupe987"));
            u.setActivo(true);
            u.setRol(admin);
            usuarioRepository.save(u);
        }

        crearConfiguracion("APP_NOMBRE", "RUPE", "Nombre corto del sistema");
        crearConfiguracion("LOGIN_MAX_INTENTOS", "5", "Intentos permitidos antes de bloqueo temporal");
        crearConfiguracion("FOLIO_PREFIJO", "RUPE", "Prefijo usado en los folios");
    }

    private void crearConfiguracion(String clave, String valor, String descripcion) {
        configuracionRepository.findByClave(clave).orElseGet(() -> {
            ConfiguracionSistema c = new ConfiguracionSistema();
            c.setClave(clave);
            c.setValor(valor);
            c.setDescripcion(descripcion);
            return configuracionRepository.save(c);
        });
    }
}
