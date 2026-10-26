package gt.umg.padron.api.controller;

import gt.umg.padron.api.dto.*;
import gt.umg.padron.service.PadronService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmpadronamientoController {
    private final PadronService service;

    public EmpadronamientoController(PadronService service) {
        this.service = service;
    }

    @PostMapping("/empadronamientos")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/consulta")
    public EmpadronadoResponse consultar(@RequestBody ConsultaRequest request) {
        return service.consultar(request);
    }
}
