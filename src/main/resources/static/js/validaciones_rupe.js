document.addEventListener("DOMContentLoaded", function () {
    const letrasEspacios = /^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\s]+$/;
    const telefono10 = /^[0-9]{10}$/;

    function setMsg(el, msg) {
        if (el) el.setCustomValidity(msg);
    }

    function clearMsg(el) {
        if (el) el.setCustomValidity("");
    }

    function validarTexto(el, nombreCampo, minimo) {
        if (!el) return;
        const v = el.value.trim();
        if (v.length === 0 && el.required) {
            setMsg(el, nombreCampo + " es obligatorio.");
        } else if (v.length > 0 && v.length < minimo) {
            setMsg(el, nombreCampo + " debe tener más información.");
        } else if (v.length > 0 && !letrasEspacios.test(v)) {
            setMsg(el, nombreCampo + " sólo acepta letras y espacios. No escriba números ni signos.");
        } else {
            clearMsg(el);
        }
    }

    document.querySelectorAll("input[name='tutorNombre'], input[name='perroNombre'], input[name='raza'], input[name='color']").forEach(function (el) {
        el.addEventListener("input", function () {
            validarTexto(el, el.name === "color" ? "El color del perro" : "Este campo", el.name === "color" ? 3 : 2);
        });
    });

    const senas = document.querySelector("textarea[name='senas']");
    if (senas) {
        senas.addEventListener("input", function () {
            validarTexto(senas, "Las señas particulares", 5);
        });
    }

    const telefono = document.querySelector("input[name='tutorTelefono']");
    if (telefono) {
        telefono.addEventListener("input", function () {
            telefono.value = telefono.value.replace(/[^0-9]/g, "").slice(0, 10);
            if (!telefono10.test(telefono.value)) {
                setMsg(telefono, "El teléfono debe contener exactamente 10 números.");
            } else {
                clearMsg(telefono);
            }
        });
    }

    const fecha = document.querySelector("input[name='fechaExtravio']");
    if (fecha) {
        fecha.addEventListener("input", function () {
            if (!fecha.value) {
                setMsg(fecha, "La fecha de extravío es obligatoria.");
            } else if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha.value)) {
                setMsg(fecha, "La fecha debe tener año de 4 dígitos.");
            } else {
                clearMsg(fecha);
            }
        });
    }
});