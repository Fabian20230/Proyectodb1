# Padrón Electoral — Proyecto Final Base de Datos I

API Spring Boot que llama **solo procedimientos almacenados** de SQL Server. Estructura basada en el
repositorio de referencia del equipo (capas `api` / `service` / `persistence`, contrato en `openapi.yaml`,
propiedades por perfil). Simulación académica: solo datos ficticios.

## Estructura
```text
padron-project/
├── pom.xml                      (padre; módulo app)
├── openapi.yaml                 (contrato de la API)
├── README.md
├── docs/
│   └── contrato_procedimientos.md   (lo que la BD debe exponer)
├── frontend/                    (proyecto de Lovable; ver frontend/README.md)
└── app/
    ├── pom.xml
    └── src/main/
        ├── java/gt/umg/padron/
        │   ├── PadronApplication.java
        │   ├── api/{controller,dto,mapper}
        │   ├── config/          (CORS)
        │   ├── domain/
        │   ├── exception/       (errores -> mensajes claros)
        │   ├── persistence/sqlserver/   (llama los procedimientos)
        │   ├── persistence/memory/      (simulación SOLO para desarrollo)
        │   └── service/         (validaciones y captcha)
        └── resources/
            ├── application.properties
            ├── application-local.properties
            ├── application-memoria.properties
            └── static/          (aquí va el build del frontend)
```

## Configuración (`application.properties`)
| Qué | Propiedad / variable | Por defecto |
|---|---|---|
| Puerto de la app | `server.port` | 8080 |
| Servidor de BD | `DB_HOST` | localhost |
| Puerto de BD | `DB_PORT` | 1433 |
| Nombre de la BD | `DB_NAME` | PadronJutiapa |
| Usuario / contraseña | `DB_USER`, `DB_PASSWORD` | (obligatorias, nunca en el código) |
| Origen de datos | `app.storage` | sqlserver (`memory` solo con el perfil memoria) |
| Orígenes CORS | `app.cors.allowed-origins` | http://localhost:5173 |

## Ejecutar
Requisitos: JDK 17+, Maven 3.9+.
```powershell
$env:DB_USER="app_padron"; $env:DB_PASSWORD="<contraseña>"
mvn -pl app spring-boot:run                                   # en el servidor (BD en localhost)
mvn -pl app spring-boot:run "-Dspring-boot.run.profiles=local" # pruebas con SQL Server local
```
Jar: `mvn clean package` → `app/target/app-1.0.jar`. Swagger UI: http://localhost:8080/swagger-ui.html

## Probar SIN base de datos (perfil memoria)
```powershell
mvn -pl app spring-boot:run "-Dspring-boot.run.profiles=memoria"
```
Abre http://localhost:8080/swagger-ui.html. Los datos son simulados y se pierden al apagar. El código de
verificación también se imprime en la consola. Ciudadano de prueba: DPI `1000000000101`, boleta `PRUEBA0001`,
nacimiento `15/03/1985`. Este modo imita las reglas solo para desarrollar la app y el frontend: **las reglas
oficiales del proyecto deben vivir en SQL Server** (ver `docs/contrato_procedimientos.md`).

## Frontend
Ver `frontend/README.md`. Resumen: `npm run build` y copiar `dist/` a `app/src/main/resources/static/`.
