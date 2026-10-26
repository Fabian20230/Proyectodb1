package gt.umg.padron.persistence;

import gt.umg.padron.api.dto.RegistroRequest;
import gt.umg.padron.domain.LugarVotacion;
import gt.umg.padron.domain.Opcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PadronRepository {
    List<Opcion> municipios();

    List<Opcion> generos();

    List<Opcion> condicionesAfiliado();

    List<LugarVotacion> lugaresVotacion();

    /** Una fila: codigo (0 = éxito), mensaje, no_empadronamiento. */
    Map<String, Object> registrar(RegistroRequest r, LocalDate emision, LocalDate nacimiento);

    /** Vacío si no hay coincidencia. */
    Optional<Map<String, Object>> consultar(String tipoDocumento, String numero, LocalDate nacimiento);
}
