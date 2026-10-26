package gt.umg.padron.service;

import gt.umg.padron.api.dto.*;

public interface PadronService {
    CatalogosResponse catalogos();

    CaptchaResponse generarCaptcha();

    RegistroResponse registrar(RegistroRequest request);

    EmpadronadoResponse consultar(ConsultaRequest request);
}
