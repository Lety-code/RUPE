package mx.unadm.rupe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Clase principal.
 * Inicia el sistema RUPE en http://localhost:8080
 */
@SpringBootApplication
public class RupeApplication {
    public static void main(String[] args) {
        SpringApplication.run(RupeApplication.class, args);
    }
}
