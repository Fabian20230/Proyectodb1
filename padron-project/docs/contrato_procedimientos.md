# Contrato de procedimientos almacenados (Persona 2 ↔ Persona 3)

La aplicación solo llama estos procedimientos. Todos deben iniciar con `SET NOCOUNT ON`.
Los nombres de parámetros y columnas deben respetarse tal cual (están en el bloque de contexto 8.2).

## 1. dbo.sp_registrar_empadronamiento
Parámetros:
`@cui CHAR(13)`, `@fecha_emision_dpi DATE`, `@fecha_nacimiento DATE`,
`@primer_nombre`, `@segundo_nombre`, `@primer_apellido`, `@segundo_apellido` (VARCHAR),
`@id_genero INT`, `@lee_escribe CHAR(2)`, `@vidente CHAR(2)`,
`@id_municipio_vecindad INT`, `@id_municipio_residencia INT`,
`@tiene_dir_geoelectoral CHAR(2)`, `@id_lugar_votacion INT`, `@id_condicion_afiliado INT`.

Notas: `@segundo_nombre` y `@segundo_apellido` pueden llegar como cadena vacía (usar `NULLIF(x,'')`).
El procedimiento valida RN-1 a RN-6, genera el No. de empadronamiento, convierte a mayúsculas e inserta
ciudadano + empadronamiento en una sola transacción (estado ACTIVO por omisión).

Devuelve **una fila**: `codigo INT` (0 = éxito), `mensaje VARCHAR(200)` (claro para el usuario),
`no_empadronamiento CHAR(10)` (NULL si hubo error).
Alternativa aceptada: `THROW 50001, 'mensaje claro', 1;` ante una regla violada (la API muestra el mensaje).

## 2. dbo.sp_consultar_empadronado
Parámetros: `@tipo_documento VARCHAR(15)` ('EMPADRONAMIENTO' o 'DPI'), `@numero VARCHAR(13)`, `@fecha_nacimiento DATE`.
Sin coincidencia: **cero filas** (no error). Con coincidencia: **una fila** con estas columnas:
`estado`, `no_empadronamiento`, `cui`, `fecha_emision_dpi` (VARCHAR dd/mm/aaaa, `CONVERT(VARCHAR(10), x, 103)`),
`nombre_completo`, `genero`, `lee_escribe`, `vidente`,
`departamento_vecindad`, `municipio_vecindad`, `departamento_residencia`, `municipio_residencia`,
`tiene_dir_geoelectoral`, `lugar_votacion`, `condicion_afiliado`.

## 3. Catálogos (sin parámetros)
| Procedimiento | Columnas |
|---|---|
| dbo.sp_listar_municipios | id, nombre |
| dbo.sp_listar_generos | id, nombre |
| dbo.sp_listar_condiciones_afiliado | id, nombre |
| dbo.sp_listar_lugares_votacion | id, nombre, id_municipio |

## 4. Permisos (Persona 1)
Cuenta SQL de la aplicación (p. ej. `app_padron`) con `GRANT EXECUTE` solo sobre estos 6 procedimientos
y sin acceso directo a las tablas.
