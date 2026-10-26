package gt.umg.padron.service;

import gt.umg.padron.api.dto.*;
import gt.umg.padron.api.mapper.EmpadronadoMapper;
import gt.umg.padron.exception.ReglaNegocioException;
import gt.umg.padron.persistence.PadronRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

@Service
public class PadronServiceImpl implements PadronService {
    private static final DateTimeFormatter DMA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final PadronRepository repo;
    private final CaptchaService captcha;

    public PadronServiceImpl(PadronRepository repo, CaptchaService captcha) {
        this.repo = repo;
        this.captcha = captcha;
    }

    @Override
    public CatalogosResponse catalogos() {
        return new CatalogosResponse(repo.municipios(), repo.generos(),
                repo.lugaresVotacion(), repo.condicionesAfiliado());
    }

    @Override
    public CaptchaResponse generarCaptcha() {
        return captcha.generar();
    }

    @Override
    public RegistroResponse registrar(RegistroRequest r) {
        if (faltan(r)) {
            throw new ReglaNegocioException("Complete todos los campos obligatorios.");
        }
        LocalDate emision = fechaIso(r.fechaEmisionDpi());
        LocalDate nacimiento = fechaIso(r.fechaNacimiento());

        Map<String, Object> fila = repo.registrar(r, emision, nacimiento);
        int codigo = ((Number) fila.get("codigo")).intValue();
        String mensaje = String.valueOf(fila.get("mensaje"));
        if (codigo != 0) {
            throw new ReglaNegocioException(mensaje); // la regla la valida la BD; aquí solo se muestra
        }
        return new RegistroResponse(String.valueOf(fila.get("no_empadronamiento")).trim(), mensaje);
    }

    @Override
    public EmpadronadoResponse consultar(ConsultaRequest c) {
        if (!captcha.validar(c.captchaToken(), c.captchaCodigo())) {
            throw new ReglaNegocioException("El código de verificación es incorrecto. Se generó uno nuevo.");
        }
        String tipo = c.tipoDocumento() == null ? "" : c.tipoDocumento().trim().toUpperCase();
        if (!tipo.equals("EMPADRONAMIENTO") && !tipo.equals("DPI")) {
            throw new ReglaNegocioException("Seleccione el tipo de documento.");
        }
        if (blank(c.numero())) {
            throw new ReglaNegocioException("Ingrese el número de documento.");
        }
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(c.fechaNacimiento() == null ? "" : c.fechaNacimiento().trim(), DMA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("Fecha de nacimiento inválida. Use día/mes/año (ejemplo: 15/03/1985).");
        }
        // Mensaje genérico: no revela nada sobre ningún ciudadano.
        return repo.consultar(tipo, c.numero().trim().toUpperCase(), nacimiento)
                .map(EmpadronadoMapper::toResponse)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.NOT_FOUND,
                        "No se encontró un empadronamiento con los datos ingresados."));
    }

    private static LocalDate fechaIso(String s) {
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("Las fechas no tienen un formato válido.");
        }
    }

    private static boolean faltan(RegistroRequest r) {
        return blank(r.cui()) || blank(r.fechaEmisionDpi()) || blank(r.fechaNacimiento())
                || blank(r.primerNombre()) || blank(r.primerApellido())
                || blank(r.leeEscribe()) || blank(r.vidente()) || blank(r.tieneDirGeoelectoral())
                || r.idGenero() == null || r.idMunicipioVecindad() == null || r.idMunicipioResidencia() == null
                || r.idLugarVotacion() == null || r.idCondicionAfiliado() == null;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
