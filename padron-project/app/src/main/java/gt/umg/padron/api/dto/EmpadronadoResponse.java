package gt.umg.padron.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Las llaves JSON coinciden con las columnas que devuelve sp_consultar_empadronado. */
public record EmpadronadoResponse(
        @JsonProperty("estado") String estado,
        @JsonProperty("no_empadronamiento") String noEmpadronamiento,
        @JsonProperty("cui") String cui,
        @JsonProperty("fecha_emision_dpi") String fechaEmisionDpi,
        @JsonProperty("nombre_completo") String nombreCompleto,
        @JsonProperty("genero") String genero,
        @JsonProperty("lee_escribe") String leeEscribe,
        @JsonProperty("vidente") String vidente,
        @JsonProperty("departamento_vecindad") String departamentoVecindad,
        @JsonProperty("municipio_vecindad") String municipioVecindad,
        @JsonProperty("departamento_residencia") String departamentoResidencia,
        @JsonProperty("municipio_residencia") String municipioResidencia,
        @JsonProperty("tiene_dir_geoelectoral") String tieneDirGeoelectoral,
        @JsonProperty("lugar_votacion") String lugarVotacion,
        @JsonProperty("condicion_afiliado") String condicionAfiliado) {}
