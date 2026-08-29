package mx.unadm.rupe.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionUtilTest {

    @Test
    void colorNoAceptaNumeros() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarColor("Negro123"));
    }

    @Test
    void colorNoAceptaVacio() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarColor("   "));
    }

    @Test
    void colorValido() {
        assertDoesNotThrow(() -> ValidacionUtil.validarColor("Dorado"));
    }

    @Test
    void senasNoAceptaSignos() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarSenas("Collar rojo!!!"));
    }

    @Test
    void senasNoAceptaVacio() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarSenas(""));
    }

    @Test
    void fechaNoAceptaAnioConTresDigitos() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarFechaExtravio("026-08-04"));
    }

    @Test
    void fechaNoAceptaVacio() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarFechaExtravio(""));
    }

    @Test
    void telefonoNoAceptaLetras() {
        assertThrows(IllegalArgumentException.class, () -> ValidacionUtil.validarTelefono("55abc45678"));
    }
}
