package mx.unadm.rupe.config;

import mx.unadm.rupe.model.Rol;
import mx.unadm.rupe.model.Usuario;
import mx.unadm.rupe.repository.RolRepository;
import mx.unadm.rupe.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
 * Crea roles y usuario administrador inicial.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository roles;
    private final UsuarioRepository usuarios;

    public DataInitializer(RolRepository roles, UsuarioRepository usuarios) {
        this.roles = roles;
        this.usuarios = usuarios;
    }

    @Override
    public void run(String... args) {
        Rol admin = roles.findAll().stream()
                .filter(r -> "ADMINISTRADOR".equals(r.getNombre()))
                .findFirst()
                .orElseGet(() -> roles.save(new Rol("ADMINISTRADOR")));

        usuarios.findByCorreo("usadminrupe@rupe.com").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre("Usuario Administrador RUPE");
            u.setCorreo("usadminrupe@rupe.com");
            u.setPassword("rupe987");
            u.setActivo(true);
            u.setRol(admin);
            return usuarios.save(u);
        });
    }
}
