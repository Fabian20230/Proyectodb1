package gt.umg.padron.api.controller;

import gt.umg.padron.api.dto.CatalogosResponse;
import gt.umg.padron.service.PadronService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {
    private final PadronService service;

    public CatalogoController(PadronService service) {
        this.service = service;
    }

    @GetMapping
    public CatalogosResponse catalogos() {
        return service.catalogos();
    }
}
