package gt.umg.padron.api.dto;

/** imagen es un data URL (data:image/png;base64,...). */
public record CaptchaResponse(String token, String imagen) {}
