# Frontend (Lovable)

1. Genera la interfaz en Lovable con `prompt_lovable.md` y sincroniza con GitHub.
2. Copia aquí el código del proyecto (o clona el repositorio dentro de esta carpeta).
3. Desarrollo por separado: copia `.env.example` a `.env.local` y ejecuta
   `npm install` y `npm run dev -- --port 5173` (Java debe estar en 8080).
4. Compilar para la demo: **borra `.env.local`**, ejecuta `npm run build` y copia el contenido de `dist/`
   a `../app/src/main/resources/static/`. Así la web y la API salen del mismo puerto (8080).
