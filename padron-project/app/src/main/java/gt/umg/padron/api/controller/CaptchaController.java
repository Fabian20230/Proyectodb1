package gt.umg.padron.api.controller;

import gt.umg.padron.api.dto.CaptchaResponse;
import gt.umg.padron.service.PadronService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    private final PadronService service;

    public CaptchaController(PadronService service) {
        this.service = service;
    }

    @GetMapping
    public CaptchaResponse captcha() {
        return service.generarCaptcha();
    }
}
