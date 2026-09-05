document.addEventListener("DOMContentLoaded", function () {
    /*
     * RUPE - Validaciones del formulario de registro.
     * Versión corregida:
     * - Acepta acentos, diéresis y ñ.
     * - Permite guion y apóstrofe en nombres/raza/color.
     * - Señas particulares permite texto natural, números y puntuación común.
     * - El teléfono se normaliza a 10 dígitos sin recortarlo por espacios o guiones.
     */

    const letrasNombre = /^[\p{L}\p{M}\s'-]+$/u;
    const telefono10 = /^[0-9]{10}$/;

    // Puntuación común permitida en una descripción.
    // Se excluyen < y > para evitar que se escriban etiquetas HTML.
    const senasPermitidas =
        /^[\p{L}\p{M}\p{N}\s.,;:()'"¿?¡!#°/+_-]+$/u;

    function setMsg(elemento, mensaje) {
        if (elemento) {
            elemento.setCustomValidity(mensaje);
        }
    }

    function clearMsg(elemento) {
        if (elemento) {
            elemento.setCustomValidity("");
        }
    }

    function validarTextoNombre(elemento, nombreCampo, minimo) {
        if (!elemento) {
            return true;
        }

        const valor = elemento.value.trim();

        if (valor.length === 0) {
            if (elemento.required) {
                setMsg(elemento, nombreCampo + " es obligatorio.");
                return false;
            }

            clearMsg(elemento);
            return true;
        }

        if (valor.length < minimo) {
            setMsg(
                elemento,
                nombreCampo + " debe tener al menos "
                + minimo + " caracteres."
            );
            return false;
        }

        if (!letrasNombre.test(valor)) {
            setMsg(
                elemento,
                nombreCampo
                + " acepta letras, espacios, acentos, ñ, diéresis, "
                + "guion y apóstrofe."
            );
            return false;
        }

        clearMsg(elemento);
        return true;
    }

    const camposTexto = [
        {
            selector: "input[name='tutorNombre']",
            nombre: "El nombre del tutor",
            minimo: 2
        },
        {
            selector: "input[name='perroNombre']",
            nombre: "El nombre del perro",
            minimo: 2
        },
        {
            selector: "input[name='raza']",
            nombre: "La raza",
            minimo: 2
        },
        {
            selector: "input[name='color']",
            nombre: "El color del perro",
            minimo: 3
        }
    ];

    camposTexto.forEach(function (config) {
        const elemento = document.querySelector(config.selector);

        if (!elemento) {
            return;
        }

        elemento.addEventListener("input", function () {
            validarTextoNombre(
                elemento,
                config.nombre,
                config.minimo
            );
        });

        elemento.addEventListener("blur", function () {
            validarTextoNombre(
                elemento,
                config.nombre,
                config.minimo
            );
        });
    });

    const senas = document.querySelector("textarea[name='senas']");

    function validarSenas() {
        if (!senas) {
            return true;
        }

        const valor = senas.value.trim();

        if (valor.length === 0) {
            setMsg(
                senas,
                "Las señas particulares son obligatorias."
            );
            return false;
        }

        if (valor.length < 5) {
            setMsg(
                senas,
                "Las señas particulares deben tener "
                + "al menos 5 caracteres."
            );
            return false;
        }

        if (valor.length > 300) {
            setMsg(
                senas,
                "Las señas particulares no pueden "
                + "exceder 300 caracteres."
            );
            return false;
        }

        if (!/[\p{L}\p{M}]/u.test(valor)) {
            setMsg(
                senas,
                "Las señas particulares deben incluir "
                + "una descripción con letras."
            );
            return false;
        }

        if (!senasPermitidas.test(valor)) {
            setMsg(
                senas,
                "Las señas particulares permiten letras, números, "
                + "acentos y signos comunes como punto, coma, "
                + "guion, paréntesis y dos puntos."
            );
            return false;
        }

        clearMsg(senas);
        return true;
    }

    if (senas) {
        senas.addEventListener("input", validarSenas);
        senas.addEventListener("blur", validarSenas);
    }

    const telefono =
        document.querySelector("input[name='tutorTelefono']");

    function normalizarTelefono() {
        if (!telefono) {
            return "";
        }

        /*
         * Se toman todos los dígitos escritos o pegados.
         * El HTML ya no debe usar maxlength="10", porque maxlength
         * cuenta espacios/guiones antes de que podamos limpiarlos.
         */
        const soloDigitos =
            telefono.value.replace(/[^0-9]/g, "");

        telefono.value = soloDigitos.slice(0, 10);

        return telefono.value;
    }

    function validarTelefono() {
        if (!telefono) {
            return true;
        }

        const valor = normalizarTelefono();

        if (!telefono10.test(valor)) {
            setMsg(
                telefono,
                "El teléfono debe contener exactamente 10 números."
            );
            return false;
        }

        clearMsg(telefono);
        return true;
    }

    if (telefono) {
        telefono.addEventListener("input", validarTelefono);
        telefono.addEventListener("blur", validarTelefono);
        telefono.addEventListener("paste", function () {
            setTimeout(validarTelefono, 0);
        });
    }

    const fecha =
        document.querySelector("input[name='fechaExtravio']");

    function validarFecha() {
        if (!fecha) {
            return true;
        }

        if (!fecha.value) {
            setMsg(
                fecha,
                "La fecha de extravío es obligatoria."
            );
            return false;
        }

        if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha.value)) {
            setMsg(
                fecha,
                "La fecha debe tener un año de 4 dígitos."
            );
            return false;
        }

        const fechaSeleccionada =
            new Date(fecha.value + "T00:00:00");

        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);

        if (fechaSeleccionada > hoy) {
            setMsg(
                fecha,
                "La fecha de extravío no puede ser futura."
            );
            return false;
        }

        clearMsg(fecha);
        return true;
    }

    if (fecha) {
        fecha.addEventListener("input", validarFecha);
        fecha.addEventListener("change", validarFecha);
        fecha.addEventListener("blur", validarFecha);
    }

    const formulario =
        document.querySelector(
            "form[action='/reportes/guardar']"
        );

    if (formulario) {
        formulario.addEventListener(
            "submit",
            function (event) {
                let valido = true;
                let primerInvalido = null;

                camposTexto.forEach(function (config) {
                    const elemento =
                        document.querySelector(
                            config.selector
                        );

                    const campoValido =
                        validarTextoNombre(
                            elemento,
                            config.nombre,
                            config.minimo
                        );

                    if (!campoValido && !primerInvalido) {
                        primerInvalido = elemento;
                    }

                    valido = campoValido && valido;
                });

                const senasValidas = validarSenas();

                if (!senasValidas && !primerInvalido) {
                    primerInvalido = senas;
                }

                valido = senasValidas && valido;

                const telefonoValido = validarTelefono();

                if (!telefonoValido && !primerInvalido) {
                    primerInvalido = telefono;
                }

                valido = telefonoValido && valido;

                const fechaValida = validarFecha();

                if (!fechaValida && !primerInvalido) {
                    primerInvalido = fecha;
                }

                valido = fechaValida && valido;

                if (!valido) {
                    event.preventDefault();

                    if (primerInvalido) {
                        primerInvalido.reportValidity();
                        primerInvalido.focus();
                    }
                }
            }
        );
    }
});
