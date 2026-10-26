package gt.umg.padron.api.dto;

/** Fechas en formato AAAA-MM-DD. leeEscribe, vidente y tieneDirGeoelectoral: "SI" o "NO". */
public record RegistroRequest(String cui, String fechaEmisionDpi, String fechaNacimiento,
                              String primerNombre, String segundoNombre,
                              String primerApellido, String segundoApellido,
                              Integer idGenero, String leeEscribe, String vidente,
                              Integer idMunicipioVecindad, Integer idMunicipioResidencia,
                              String tieneDirGeoelectoral, Integer idLugarVotacion,
                              Integer idCondicionAfiliado) {}
