package gt.umg.padron.service;

import gt.umg.padron.api.dto.CaptchaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Código de verificación: se genera en el servidor, se dibuja como imagen y se usa una sola vez. */
@Service
public class CaptchaService {
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin O, 0, I, 1
    private static final int LARGO = 5;
    private static final long VIGENCIA_MS = 5 * 60 * 1000L;

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

    @Value("${app.captcha.debug:false}")
    private boolean debug;

    private record Entrada(String codigo, long expira) {}

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entrada> codigos = new ConcurrentHashMap<>();

    public CaptchaResponse generar() {
        long ahora = System.currentTimeMillis();
        codigos.values().removeIf(e -> e.expira() < ahora);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LARGO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        String token = UUID.randomUUID().toString();
        if (debug) log.info("[captcha de prueba] token={} codigo={}", token, sb);
        codigos.put(token, new Entrada(sb.toString(), ahora + VIGENCIA_MS));
        return new CaptchaResponse(token, "data:image/png;base64," + dibujar(sb.toString()));
    }

    /** Valida y descarta el código (un solo uso). */
    public boolean validar(String token, String codigo) {
        if (token == null || codigo == null) return false;
        Entrada e = codigos.remove(token);
        return e != null && e.expira() >= System.currentTimeMillis()
                && e.codigo().equalsIgnoreCase(codigo.trim());
    }

    private String dibujar(String texto) {
        int w = 170, h = 54;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 6; i++) {
            g.drawLine(random.nextInt(w), random.nextInt(h), random.nextInt(w), random.nextInt(h));
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < texto.length(); i++) {
            AffineTransform original = g.getTransform();
            g.rotate((random.nextDouble() - 0.5) * 0.5, 20 + i * 28, h / 2.0);
            g.drawString(String.valueOf(texto.charAt(i)), 14 + i * 28, 38);
            g.setTransform(original);
        }
        g.dispose();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
