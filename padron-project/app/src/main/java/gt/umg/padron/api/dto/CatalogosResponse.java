package gt.umg.padron.api.dto;

import gt.umg.padron.domain.LugarVotacion;
import gt.umg.padron.domain.Opcion;

import java.util.List;

public record CatalogosResponse(List<Opcion> municipios, List<Opcion> generos,
                                List<LugarVotacion> lugaresVotacion, List<Opcion> condicionesAfiliado) {}
