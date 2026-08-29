package mx.unadm.rupe.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidacionUtil {
    private static final Pattern LETRAS_ESPACIOS = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]{2,120}$");
    private static final Pattern COLOR = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]{3,80}$");
    private static final Pattern SENAS = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]{5,300}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9]{10}$");
    private static final Pattern FECHA_HTML = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$");

    private ValidacionUtil() {}

    public static void validarTextoObligatorio(String valor, String campo) {
        if (estaVacio(valor)) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (!LETRAS_ESPACIOS.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("El campo " + campo + " sólo acepta letras y espacios.");
        }
    }

    public static void validarTelefono(String telefono) {
        if (estaVacio(telefono)) {
            throw new IllegalArgumentException("El teléfono del tutor es obligatorio.");
        }
        if (!TELEFONO.matcher(telefono.trim()).matches()) {
            throw new IllegalArgumentException("El teléfono debe contener exactamente 10 números.");
        }
    }

    public static void validarColor(String color) {
        if (estaVacio(color)) {
            throw new IllegalArgumentException("El color del perro es obligatorio.");
        }
        if (!COLOR.matcher(color.trim()).matches()) {
            throw new IllegalArgumentException("El color del perro sólo acepta letras y espacios. No escriba números ni signos.");
        }
    }

    public static void validarSenas(String senas) {
        if (estaVacio(senas)) {
            throw new IllegalArgumentException("Las señas particulares son obligatorias.");
        }
        if (!SENAS.matcher(senas.trim()).matches()) {
            throw new IllegalArgumentException("Las señas particulares sólo aceptan letras y espacios. No escriba números ni signos.");
        }
        if (contieneSoloNumeros(senas)) {
            throw new IllegalArgumentException("Las señas particulares deben incluir una descripción clara, no sólo números.");
        }
    }

    public static LocalDate validarFechaExtravio(String fechaTexto) {
        if (estaVacio(fechaTexto)) {
            throw new IllegalArgumentException("La fecha de extravío es obligatoria.");
        }
        String limpia = fechaTexto.trim();
        if (!FECHA_HTML.matcher(limpia).matches()) {
            throw new IllegalArgumentException("La fecha debe tener formato válido con año de 4 dígitos, por ejemplo 2026-08-04.");
        }
        try {
            LocalDate fecha = LocalDate.parse(limpia);
            int year = fecha.getYear();
            if (year < 1900 || year > LocalDate.now().getYear() + 1) {
                throw new IllegalArgumentException("El año de la fecha no es válido.");
            }
            if (fecha.isAfter(LocalDate.now().plusDays(1))) {
                throw new IllegalArgumentException("La fecha de extravío no puede ser futura.");
            }
            return fecha;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha de extravío no es válida.");
        }
    }

    public static boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static boolean contieneSoloNumeros(String valor) {
        return valor != null && valor.trim().matches("^[0-9\\s]+$");
    }
}
