package gt.umg.padron.persistence.sqlserver;

import gt.umg.padron.api.dto.RegistroRequest;
import gt.umg.padron.domain.LugarVotacion;
import gt.umg.padron.domain.Opcion;
import gt.umg.padron.persistence.PadronRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Solo llama procedimientos almacenados: las reglas de negocio viven en la base de datos. */
@Repository
@ConditionalOnProperty(name = "app.storage", havingValue = "sqlserver", matchIfMissing = true)
public class SqlServerPadronRepository implements PadronRepository {
    private final JdbcTemplate jdbc;

    public SqlServerPadronRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Opcion> municipios() {
        return opciones("sp_listar_municipios");
    }

    @Override
    public List<Opcion> generos() {
        return opciones("sp_listar_generos");
    }

    @Override
    public List<Opcion> condicionesAfiliado() {
        return opciones("sp_listar_condiciones_afiliado");
    }

    @Override
    public List<LugarVotacion> lugaresVotacion() {
        return jdbc.query("SET NOCOUNT ON; EXEC dbo.sp_listar_lugares_votacion",
                (rs, i) -> new LugarVotacion(rs.getInt("id"), rs.getString("nombre"), rs.getInt("id_municipio")));
    }

    @Override
    public Map<String, Object> registrar(RegistroRequest r, LocalDate emision, LocalDate nacimiento) {
        String sql = "SET NOCOUNT ON; EXEC dbo.sp_registrar_empadronamiento "
                + "@cui = ?, @fecha_emision_dpi = ?, @fecha_nacimiento = ?, "
                + "@primer_nombre = ?, @segundo_nombre = ?, @primer_apellido = ?, @segundo_apellido = ?, "
                + "@id_genero = ?, @lee_escribe = ?, @vidente = ?, "
                + "@id_municipio_vecindad = ?, @id_municipio_residencia = ?, "
                + "@tiene_dir_geoelectoral = ?, @id_lugar_votacion = ?, @id_condicion_afiliado = ?";
        return jdbc.queryForMap(sql,
                r.cui().trim(), Date.valueOf(emision), Date.valueOf(nacimiento),
                r.primerNombre().trim(), nvl(r.segundoNombre()), r.primerApellido().trim(), nvl(r.segundoApellido()),
                r.idGenero(), r.leeEscribe(), r.vidente(),
                r.idMunicipioVecindad(), r.idMunicipioResidencia(),
                r.tieneDirGeoelectoral(), r.idLugarVotacion(), r.idCondicionAfiliado());
    }

    @Override
    public Optional<Map<String, Object>> consultar(String tipoDocumento, String numero, LocalDate nacimiento) {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SET NOCOUNT ON; EXEC dbo.sp_consultar_empadronado "
                        + "@tipo_documento = ?, @numero = ?, @fecha_nacimiento = ?",
                tipoDocumento, numero, Date.valueOf(nacimiento));
        return filas.stream().findFirst();
    }

    private List<Opcion> opciones(String procedimiento) {
        return jdbc.query("SET NOCOUNT ON; EXEC dbo." + procedimiento,
                (rs, i) -> new Opcion(rs.getInt("id"), rs.getString("nombre")));
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }
}
