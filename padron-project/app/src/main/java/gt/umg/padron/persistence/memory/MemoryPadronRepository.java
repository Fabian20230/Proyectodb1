package gt.umg.padron.persistence.memory;

import gt.umg.padron.api.dto.RegistroRequest;
import gt.umg.padron.domain.LugarVotacion;
import gt.umg.padron.domain.Opcion;
import gt.umg.padron.persistence.PadronRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SOLO PARA DESARROLLO: simula los procedimientos almacenados con datos en memoria (se pierden al apagar)
 * para poder probar la app y el frontend sin base de datos. No sustituye las reglas en SQL Server.
 * Se activa con el perfil "memoria" (app.storage=memory).
 */
@Repository
@ConditionalOnProperty(name = "app.storage", havingValue = "memory")
public class MemoryPadronRepository implements PadronRepository {
    private static final String[] NOMBRES_MUNICIPIOS = {
            "JUTIAPA", "EL PROGRESO", "SANTA CATARINA MITA", "AGUA BLANCA", "ASUNCIÓN MITA", "YUPILTEPEQUE",
            "ATESCATEMPA", "JEREZ", "EL ADELANTO", "ZAPOTITLÁN", "COMAPA", "JALPATAGUA", "CONGUACO",
            "MOYUTA", "PASACO", "SAN JOSÉ ACATEMPA", "QUESADA"};
    private static final LocalDate CORTE_RN3 = LocalDate.of(2009, 1, 1); // nacidos el 01/01/2009 o antes
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final DateTimeFormatter DMA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private record Registro(Map<String, Object> fila, LocalDate nacimiento) {}

    private final List<Opcion> municipios = new ArrayList<>();
    private final List<LugarVotacion> lugares = new ArrayList<>();
    private final List<Opcion> generos = List.of(new Opcion(1, "MASCULINO"), new Opcion(2, "FEMENINO"));
    private final List<Opcion> afiliados = List.of(new Opcion(1, "AFILIADO"), new Opcion(2, "NO ESTÁ AFILIADO"));
    private final Map<String, Registro> porNumero = new ConcurrentHashMap<>();
    private final Map<String, Registro> porCui = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public MemoryPadronRepository() {
        for (int i = 0; i < NOMBRES_MUNICIPIOS.length; i++) {
            municipios.add(new Opcion(i + 1, NOMBRES_MUNICIPIOS[i]));
            lugares.add(new LugarVotacion(i + 1, "CENTRO DE VOTACIÓN " + NOMBRES_MUNICIPIOS[i] + " (EJEMPLO)", i + 1));
        }
        // Ciudadano ficticio para probar la consulta: DPI 1000000000101 / boleta PRUEBA0001 / nacimiento 15/03/1985
        guardar("PRUEBA0001", "1000000000101", LocalDate.of(2020, 1, 10), LocalDate.of(1985, 3, 15),
                "ANA MARÍA EJEMPLO PRUEBA", 2, "SI", "SI", 1, 1, "SI", 1, 2);
    }

    @Override
    public List<Opcion> municipios() {
        return municipios;
    }

    @Override
    public List<Opcion> generos() {
        return generos;
    }

    @Override
    public List<Opcion> condicionesAfiliado() {
        return afiliados;
    }

    @Override
    public List<LugarVotacion> lugaresVotacion() {
        return lugares;
    }

    /** Imita las reglas RN-1 a RN-6 que en el proyecto real deben vivir en SQL Server. */
    @Override
    public synchronized Map<String, Object> registrar(RegistroRequest r, LocalDate emision, LocalDate nacimiento) {
        String cui = r.cui().trim();
        if (!cui.matches("\\d{13}")) return error(1, "El CUI debe tener exactamente 13 dígitos.");
        if (porCui.containsKey(cui)) return error(2, "Ya existe un empadronamiento con ese CUI.");
        if (emision.isAfter(LocalDate.now())) {
            return error(3, "La fecha de emisión del DPI no puede ser posterior a la fecha de registro.");
        }
        if (nacimiento.isAfter(CORTE_RN3)) {
            return error(4, "La persona debe tener 18 años cumplidos al 01/01/2027.");
        }
        if (r.idGenero() < 1 || r.idGenero() > 2) return error(5, "Solo se permite género MASCULINO o FEMENINO.");
        String lee = r.leeEscribe().trim().toUpperCase(Locale.ROOT);
        String vid = r.vidente().trim().toUpperCase(Locale.ROOT);
        String dir = r.tieneDirGeoelectoral().trim().toUpperCase(Locale.ROOT);
        if (!siNo(lee) || !siNo(vid) || !siNo(dir)) return error(6, "Los campos SI/NO solo admiten SI o NO.");
        if (!municipioValido(r.idMunicipioVecindad()) || !municipioValido(r.idMunicipioResidencia())) {
            return error(7, "Solo se permiten municipios del departamento de JUTIAPA.");
        }
        if (r.idLugarVotacion() < 1 || r.idLugarVotacion() > lugares.size()
                || r.idCondicionAfiliado() < 1 || r.idCondicionAfiliado() > afiliados.size()) {
            return error(8, "Lugar de votación o condición de afiliado no válidos.");
        }
        String numero;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) sb.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
            numero = sb.toString();
        } while (porNumero.containsKey(numero)); // RN-1: aleatorio, 10 caracteres, sin repetir

        String nombre = (r.primerNombre() + " " + r.segundoNombre() + " " + r.primerApellido() + " "
                + r.segundoApellido()).trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT); // RN-6
        guardar(numero, cui, emision, nacimiento, nombre, r.idGenero(), lee, vid,
                r.idMunicipioVecindad(), r.idMunicipioResidencia(), dir, r.idLugarVotacion(), r.idCondicionAfiliado());

        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("codigo", 0);
        ok.put("mensaje", "Empadronamiento registrado correctamente.");
        ok.put("no_empadronamiento", numero);
        return ok;
    }

    @Override
    public Optional<Map<String, Object>> consultar(String tipoDocumento, String numero, LocalDate nacimiento) {
        Registro reg = tipoDocumento.equals("DPI") ? porCui.get(numero) : porNumero.get(numero);
        if (reg == null || !reg.nacimiento().equals(nacimiento)) return Optional.empty();
        return Optional.of(reg.fila());
    }

    private void guardar(String numero, String cui, LocalDate emision, LocalDate nacimiento, String nombre,
                         int idGenero, String lee, String vidente, int idMunVec, int idMunRes,
                         String dir, int idLugar, int idCond) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("estado", "ACTIVO");
        f.put("no_empadronamiento", numero);
        f.put("cui", cui);
        f.put("fecha_emision_dpi", emision.format(DMA));
        f.put("nombre_completo", nombre);
        f.put("genero", generos.get(idGenero - 1).nombre());
        f.put("lee_escribe", lee);
        f.put("vidente", vidente);
        f.put("departamento_vecindad", "JUTIAPA");
        f.put("municipio_vecindad", municipios.get(idMunVec - 1).nombre());
        f.put("departamento_residencia", "JUTIAPA");
        f.put("municipio_residencia", municipios.get(idMunRes - 1).nombre());
        f.put("tiene_dir_geoelectoral", dir);
        f.put("lugar_votacion", lugares.get(idLugar - 1).nombre());
        f.put("condicion_afiliado", afiliados.get(idCond - 1).nombre());
        Registro reg = new Registro(f, nacimiento);
        porNumero.put(numero, reg);
        porCui.put(cui, reg);
    }

    private boolean municipioValido(int id) {
        return id >= 1 && id <= municipios.size();
    }

    private static boolean siNo(String s) {
        return s.equals("SI") || s.equals("NO");
    }

    private static Map<String, Object> error(int codigo, String mensaje) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", codigo);
        m.put("mensaje", mensaje);
        m.put("no_empadronamiento", null);
        return m;
    }
}
