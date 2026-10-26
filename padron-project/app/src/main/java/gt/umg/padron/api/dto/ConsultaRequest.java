package gt.umg.padron.api.dto;

/** tipoDocumento: EMPADRONAMIENTO o DPI. fechaNacimiento en formato DD/MM/AAAA. */
public record ConsultaRequest(String tipoDocumento, String numero, String fechaNacimiento,
                              String captchaToken, String captchaCodigo) {}
