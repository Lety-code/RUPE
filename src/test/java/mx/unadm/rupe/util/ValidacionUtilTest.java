package mx.unadm.rupe.util;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionUtilTest {

    @Test
    void textoAceptaAcentosEnieYGuion() {
        assertDoesNotThrow(() ->
                ValidacionUtil.validarTextoObligatorio(
                        "José María-Luís",
                        "Nombre"
                )
        );
    }

    @Test
    void textoRechazaNumeros() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarTextoObligatorio(
                        "Nombre123",
                        "Nombre"
                )
        );
    }

    @Test
    void telefonoAceptaDiezDigitos() {
        assertDoesNotThrow(() ->
                ValidacionUtil.validarTelefono("5569847512")
        );
    }

    @Test
    void telefonoRechazaMenosDeDiezDigitos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarTelefono("55698475")
        );
    }

    @Test
    void telefonoRechazaLetras() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarTelefono("55698A7512")
        );
    }

    @Test
    void colorAceptaAcentosYEnie() {
        assertDoesNotThrow(() ->
                ValidacionUtil.validarColor("Café marrón")
        );
    }

    @Test
    void colorRechazaNumeros() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarColor("Café123")
        );
    }

    @Test
    void senasAceptaAcentosEnieComaPuntoYNumeros() {
        assertDoesNotThrow(() ->
                ValidacionUtil.validarSenas(
                        "Cicatriz pequeña en pata, una oreja más oscura que la otra. Collar azul número 2."
                )
        );
    }

    @Test
    void senasRechazaEtiquetasHtml() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarSenas(
                        "Tiene una mancha <script>alert('x')</script>"
                )
        );
    }

    @Test
    void senasRechazaTextoMuyCorto() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarSenas("abc")
        );
    }

    @Test
    void fechaAceptaFechaValidaNoFutura() {
        String fecha =
                LocalDate.now().minusDays(1).toString();

        assertDoesNotThrow(() ->
                ValidacionUtil.validarFechaExtravio(fecha)
        );
    }

    @Test
    void fechaRechazaFechaFutura() {
        String fecha =
                LocalDate.now().plusDays(1).toString();

        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarFechaExtravio(fecha)
        );
    }

    @Test
    void fechaRechazaFormatoIncorrecto() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidacionUtil.validarFechaExtravio(
                        "05/09/2026"
                )
        );
    }

    @Test
    void estaVacioDetectaNuloVacioYEspacios() {
        assertTrue(ValidacionUtil.estaVacio(null));
        assertTrue(ValidacionUtil.estaVacio(""));
        assertTrue(ValidacionUtil.estaVacio("   "));
        assertFalse(ValidacionUtil.estaVacio("RUPE"));
    }
}
