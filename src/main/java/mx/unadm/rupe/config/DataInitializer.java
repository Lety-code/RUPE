package mx.unadm.rupe.config;

import mx.unadm.rupe.model.Rol;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.RolRepository;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        Rol admin = rolRepository.findByNombre("ADMINISTRADOR").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("ADMINISTRADOR");
            r.setDescripcion("Usuario con acceso al panel administrativo.");
            return rolRepository.save(r);
        });

        usuarioRepository.findByCorreo("usadminrupe@rupe.com").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre("Administrador RUPE");
            u.setCorreo("usadminrupe@rupe.com");
            u.setPassword("rupe987");
            u.setActivo(true);
            u.setRol(admin);
            return usuarioRepository.save(u);
        });
    }
}
