package mx.unadm.rupe.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidacionUtil {

    /*
     * Letras Unicode:
     * \p{L} = cualquier letra (incluye á, é, í, ó, ú, ñ, ü, etc.)
     * \p{M} = marcas combinadas de acentuación.
     */
    private static final Pattern TEXTO_NOMBRE = Pattern.compile(
            "^[\\p{L}\\p{M}\\s'’\\-]{2,120}$"
    );

    private static final Pattern COLOR = Pattern.compile(
            "^[\\p{L}\\p{M}\\s'’/\\-]{3,80}$"
    );

    private static final Pattern TELEFONO = Pattern.compile(
            "^[0-9]{10}$"
    );

    private static final Pattern FECHA_HTML = Pattern.compile(
            "^[0-9]{4}-[0-9]{2}-[0-9]{2}$"
    );

    private ValidacionUtil() {
    }

    public static void validarTextoObligatorio(
            String valor,
            String campo) {

        if (estaVacio(valor)) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " es obligatorio."
            );
        }

        String limpio = valor.trim();

        if (limpio.length() < 2 || limpio.length() > 120) {
            throw new IllegalArgumentException(
                    "El campo " + campo
                    + " debe tener entre 2 y 120 caracteres."
            );
        }

        if (!TEXTO_NOMBRE.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El campo " + campo
                    + " acepta letras, espacios, acentos, ñ, diéresis, "
                    + "guion y apóstrofe."
            );
        }
    }

    public static void validarTelefono(String telefono) {

        if (estaVacio(telefono)) {
            throw new IllegalArgumentException(
                    "El teléfono del tutor es obligatorio."
            );
        }

        String limpio = telefono.trim();

        if (!TELEFONO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El teléfono debe contener exactamente 10 números."
            );
        }
    }

    public static void validarColor(String color) {

        if (estaVacio(color)) {
            throw new IllegalArgumentException(
                    "El color del perro es obligatorio."
            );
        }

        String limpio = color.trim();

        if (!COLOR.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El color del perro acepta letras, espacios, acentos, "
                    + "ñ, diéresis, guion y diagonal."
            );
        }
    }

    public static void validarSenas(String senas) {

        if (estaVacio(senas)) {
            throw new IllegalArgumentException(
                    "Las señas particulares son obligatorias."
            );
        }

        String limpio = senas.trim();

        if (limpio.length() < 5) {
            throw new IllegalArgumentException(
                    "Las señas particulares deben tener "
                    + "al menos 5 caracteres."
            );
        }

        if (limpio.length() > 300) {
            throw new IllegalArgumentException(
                    "Las señas particulares no pueden "
                    + "exceder 300 caracteres."
            );
        }

        boolean contieneLetra =
                limpio.codePoints().anyMatch(Character::isLetter);

        if (!contieneLetra) {
            throw new IllegalArgumentException(
                    "Las señas particulares deben incluir "
                    + "una descripción con letras."
            );
        }

        /*
         * Se permite texto natural: letras, acentos, números,
         * comas, puntos, guiones, paréntesis y otros signos comunes.
         * Sólo se rechazan < y > para evitar etiquetas HTML.
         */
        if (limpio.contains("<") || limpio.contains(">")) {
            throw new IllegalArgumentException(
                    "Las señas particulares no permiten los signos < o >."
            );
        }

        boolean contieneControlNoPermitido =
                limpio.codePoints().anyMatch(c ->
                        Character.isISOControl(c)
                        && c != '\n'
                        && c != '\r'
                        && c != '\t'
                );

        if (contieneControlNoPermitido) {
            throw new IllegalArgumentException(
                    "Las señas particulares contienen "
                    + "caracteres no permitidos."
            );
        }
    }

    public static LocalDate validarFechaExtravio(
            String fechaTexto) {

        if (estaVacio(fechaTexto)) {
            throw new IllegalArgumentException(
                    "La fecha de extravío es obligatoria."
            );
        }

        String limpia = fechaTexto.trim();

        if (!FECHA_HTML.matcher(limpia).matches()) {
            throw new IllegalArgumentException(
                    "La fecha debe tener formato válido "
                    + "con año de 4 dígitos, por ejemplo 2026-08-04."
            );
        }

        try {
            LocalDate fecha = LocalDate.parse(limpia);
            LocalDate hoy = LocalDate.now();

            if (fecha.getYear() < 1900) {
                throw new IllegalArgumentException(
                        "El año de la fecha no es válido."
                );
            }

            if (fecha.isAfter(hoy)) {
                throw new IllegalArgumentException(
                        "La fecha de extravío no puede ser futura."
                );
            }

            return fecha;

        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La fecha de extravío no es válida."
            );
        }
    }

    public static boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
