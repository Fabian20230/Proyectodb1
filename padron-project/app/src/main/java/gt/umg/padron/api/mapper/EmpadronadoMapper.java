package gt.umg.padron.api.mapper;

import gt.umg.padron.api.dto.EmpadronadoResponse;

import java.util.Map;

public final class EmpadronadoMapper {
    private EmpadronadoMapper() {}

    /** Convierte la fila devuelta por el procedimiento almacenado (columnas) en el DTO de respuesta. */
    public static EmpadronadoResponse toResponse(Map<String, Object> f) {
        return new EmpadronadoResponse(
                s(f, "estado"), s(f, "no_empadronamiento"), s(f, "cui"), s(f, "fecha_emision_dpi"),
                s(f, "nombre_completo"), s(f, "genero"), s(f, "lee_escribe"), s(f, "vidente"),
                s(f, "departamento_vecindad"), s(f, "municipio_vecindad"),
                s(f, "departamento_residencia"), s(f, "municipio_residencia"),
                s(f, "tiene_dir_geoelectoral"), s(f, "lugar_votacion"), s(f, "condicion_afiliado"));
    }

    private static String s(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        return v == null ? null : v.toString().trim();
    }
}
