import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState, type ReactNode, type FormEvent } from "react";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Sistema de Registro y Consulta de Padrón Electoral" },
      { name: "description", content: "Simulación académica de registro y consulta del padrón electoral de Jutiapa." },
      { property: "og:title", content: "Sistema de Registro y Consulta de Padrón Electoral" },
      { property: "og:description", content: "Simulación académica de registro y consulta del padrón electoral de Jutiapa." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary" },
    ],
  }),
  component: Index,
});

type Item = { id: number | string; nombre: string };
type Lugar = Item & { idMunicipio: number | string };
type Catalogos = { municipios: Item[]; generos: Item[]; lugaresVotacion: Lugar[]; condicionesAfiliado: Item[] };

const API = `${import.meta.env["VITE_API_URL"] ?? ""}/api`;
const isDemo = () => typeof window !== "undefined" && window.location.hostname.includes("lovable");

const DEMO_CAT: Catalogos = {
  municipios: ["Jutiapa", "El Progreso", "Asunción Mita", "Jalpatagua"].map((n, i) => ({ id: i + 1, nombre: n })),
  generos: [{ id: 1, nombre: "Masculino" }, { id: 2, nombre: "Femenino" }],
  lugaresVotacion: [
    { id: 1, nombre: "Escuela Oficial Urbana Mixta No. 1", idMunicipio: 1 },
    { id: 2, nombre: "Instituto Nacional de Educación Básica", idMunicipio: 2 },
    { id: 3, nombre: "Escuela Rural Mixta Aldea San José", idMunicipio: 3 },
  ],
  condicionesAfiliado: [{ id: 1, nombre: "No afiliado" }, { id: 2, nombre: "Afiliado" }],
};
const DEMO_CAPTCHA =
  "data:image/svg+xml;utf8," +
  encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="140" height="44"><rect width="140" height="44" fill="#eee"/><text x="20" y="30" font-size="22" font-family="monospace" fill="#333">A7K2Q</text></svg>');
const DEMO_RESULT: Record<string, string> = {
  estado: "ACTIVO", no_empadronamiento: "000123", cui: "1234567890101", fecha_emision_dpi: "10/01/2020",
  nombre_completo: "Juan Carlos Pérez López", genero: "Masculino", lee_escribe: "SI", vidente: "SI",
  departamento_vecindad: "JUTIAPA", municipio_vecindad: "Jutiapa", departamento_residencia: "JUTIAPA",
  municipio_residencia: "Jutiapa", tiene_dir_geoelectoral: "SI", lugar_votacion: "Escuela Oficial Urbana Mixta No. 1",
  condicion_afiliado: "No afiliado",
};

async function call(path: string, body?: unknown) {
  const r = await fetch(`${API}${path}`, body === undefined ? undefined : {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body),
  });
  let data: any = {};
  try { data = await r.json(); } catch { /* sin cuerpo */ }
  return { status: r.status, data };
}

const input = "w-full border border-input bg-background px-2 py-1.5 text-sm focus:outline-none focus:border-primary";
const today = () => new Date().toISOString().slice(0, 10);

function Field({ label, children, hint }: { label: string; children: ReactNode; hint?: string }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-foreground">{label}</span>
      {children}
      {hint && <span className="mt-1 block text-xs text-muted-foreground">{hint}</span>}
    </label>
  );
}
function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <fieldset className="border border-border p-4">
      <legend className="px-1 text-sm font-semibold text-primary">{title}</legend>
      <div className="grid gap-3 sm:grid-cols-2">{children}</div>
    </fieldset>
  );
}
function Sel({ value, onChange, items, required = true }: { value: string; onChange: (v: string) => void; items: Item[]; required?: boolean }) {
  return (
    <select className={input} value={value} onChange={(e) => onChange(e.target.value)} required={required}>
      <option value="">— Seleccione —</option>
      {items.map((i) => <option key={i.id} value={i.id}>{i.nombre}</option>)}
    </select>
  );
}
const SN = [{ id: "SI", nombre: "SI" }, { id: "NO", nombre: "NO" }];
const ErrorBox = ({ msg }: { msg: string }) => (
  <div className="border border-destructive bg-background p-3 text-sm text-destructive">{msg}</div>
);
const Btn = ({ children, secondary, ...p }: any) => (
  <button {...p} className={secondary
    ? "border border-border bg-background px-4 py-2 text-sm text-foreground hover:bg-muted"
    : "bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-50"}>{children}</button>
);

function Index() {
  const [tab, setTab] = useState<"registro" | "consulta">("registro");
  const [cat, setCat] = useState<Catalogos | null>(null);
  const [catErr, setCatErr] = useState("");

  useEffect(() => {
    if (isDemo()) { setCat(DEMO_CAT); return; }
    call("/catalogos").then(({ status, data }) => status === 200 ? setCat(data) : setCatErr(data.mensaje ?? "No se pudieron cargar los catálogos."))
      .catch(() => setCatErr("No fue posible conectar con el servidor. Intente más tarde."));
  }, []);

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <header className="mb-6 border-b border-border pb-4">
        <h1 className="text-xl font-semibold text-foreground">Sistema de Registro y Consulta de Padrón Electoral</h1>
        <p className="text-sm text-muted-foreground">Simulación académica · Departamento de Jutiapa{isDemo() && " · Vista previa con datos de ejemplo"}</p>
      </header>
      <nav className="mb-6 flex gap-1 border-b border-border print:hidden">
        {(["registro", "consulta"] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`-mb-px border px-4 py-2 text-sm ${tab === t ? "border-border border-b-background bg-background font-semibold text-primary" : "border-transparent text-muted-foreground hover:text-foreground"}`}>
            {t === "registro" ? "Registro" : "Consulta"}
          </button>
        ))}
      </nav>
      {catErr && <div className="mb-4"><ErrorBox msg={catErr} /></div>}
      {tab === "registro" ? <Registro cat={cat} /> : <Consulta />}
    </div>
  );
}

const EMPTY = {
  cui: "", fechaEmisionDpi: "", primerNombre: "", segundoNombre: "", primerApellido: "", segundoApellido: "",
  fechaNacimiento: "", idGenero: "", leeEscribe: "", vidente: "", idMunicipioVecindad: "", idMunicipioResidencia: "",
  tieneDirGeoelectoral: "", idLugarVotacion: "", idCondicionAfiliado: "",
};

function Registro({ cat }: { cat: Catalogos | null }) {
  const [f, setF] = useState(EMPTY);
  const [err, setErr] = useState("");
  const [ok, setOk] = useState<{ noEmpadronamiento: string; mensaje: string } | null>(null);
  const [busy, setBusy] = useState(false);
  const set = (k: keyof typeof EMPTY) => (v: string) => setF((p) => ({ ...p, [k]: v }));
  const txt = (k: keyof typeof EMPTY, required = true) => (
    <input className={input} value={f[k]} required={required} maxLength={60} onChange={(e) => set(k)(e.target.value)} />
  );
  const num = (v: string) => (v === "" ? null : isNaN(Number(v)) ? v : Number(v));

  async function submit(e: FormEvent) {
    e.preventDefault(); setErr("");
    if (!/^\d{13}$/.test(f.cui)) return setErr("El CUI debe tener exactamente 13 dígitos.");
    if (f.fechaEmisionDpi > today()) return setErr("La fecha de emisión del DPI no puede ser futura.");
    if (f.fechaNacimiento > today()) return setErr("La fecha de nacimiento no puede ser futura.");
    const body = {
      ...f,
      primerNombre: f.primerNombre.trim(), segundoNombre: f.segundoNombre.trim(),
      primerApellido: f.primerApellido.trim(), segundoApellido: f.segundoApellido.trim(),
      idGenero: num(f.idGenero), idMunicipioVecindad: num(f.idMunicipioVecindad), idMunicipioResidencia: num(f.idMunicipioResidencia),
      idLugarVotacion: num(f.idLugarVotacion), idCondicionAfiliado: num(f.idCondicionAfiliado),
    };
    setBusy(true);
    try {
      if (isDemo()) { setOk({ noEmpadronamiento: String(Math.floor(100000 + Math.random() * 900000)), mensaje: "Registro guardado correctamente (ejemplo)." }); return; }
      const { status, data } = await call("/empadronamientos", body);
      if (status === 201) setOk(data);
      else setErr(data.mensaje ?? "No se pudo guardar el registro.");
    } catch { setErr("No fue posible conectar con el servidor. Intente más tarde."); }
    finally { setBusy(false); }
  }

  if (ok) return (
    <div className="border border-border p-8 text-center">
      <p className="text-sm text-muted-foreground">Número de empadronamiento asignado</p>
      <p className="my-3 text-5xl font-bold tracking-wider text-primary">{ok.noEmpadronamiento}</p>
      <p className="mb-6 text-sm text-foreground">{ok.mensaje}</p>
      <Btn onClick={() => { setOk(null); setF(EMPTY); }}>Nuevo registro</Btn>
    </div>
  );

  const lugares = cat?.lugaresVotacion ?? [];
  return (
    <form onSubmit={submit} className="space-y-5">
      {err && <ErrorBox msg={err} />}
      <p className="text-sm text-muted-foreground">Complete los datos de la persona. El número de empadronamiento lo asigna el sistema automáticamente.</p>
      <Section title="1. Identificación">
        <Field label="CUI" hint="13 dígitos, sin espacios ni guiones">
          <input className={input} inputMode="numeric" maxLength={13} required value={f.cui}
            onChange={(e) => set("cui")(e.target.value.replace(/\D/g, ""))} />
        </Field>
        <Field label="Fecha de emisión del DPI">
          <input type="date" className={input} max={today()} required value={f.fechaEmisionDpi} onChange={(e) => set("fechaEmisionDpi")(e.target.value)} />
        </Field>
      </Section>
      <Section title="2. Datos personales">
        <Field label="Primer nombre">{txt("primerNombre")}</Field>
        <Field label="Segundo nombre (opcional)">{txt("segundoNombre", false)}</Field>
        <Field label="Primer apellido">{txt("primerApellido")}</Field>
        <Field label="Segundo apellido (opcional)">{txt("segundoApellido", false)}</Field>
        <Field label="Fecha de nacimiento">
          <input type="date" className={input} max={today()} required value={f.fechaNacimiento} onChange={(e) => set("fechaNacimiento")(e.target.value)} />
        </Field>
        <Field label="Género"><Sel value={f.idGenero} onChange={set("idGenero")} items={cat?.generos ?? []} /></Field>
        <Field label="¿Lee y escribe?"><Sel value={f.leeEscribe} onChange={set("leeEscribe")} items={SN} /></Field>
        <Field label="¿Vidente?"><Sel value={f.vidente} onChange={set("vidente")} items={SN} /></Field>
      </Section>
      <Section title="3. Vecindad y residencia">
        <Field label="Departamento"><input className={`${input} bg-muted`} value="JUTIAPA" readOnly /></Field>
        <Field label="Municipio de vecindad"><Sel value={f.idMunicipioVecindad} onChange={set("idMunicipioVecindad")} items={cat?.municipios ?? []} /></Field>
        <Field label="Municipio de residencia"><Sel value={f.idMunicipioResidencia} onChange={set("idMunicipioResidencia")} items={cat?.municipios ?? []} /></Field>
        <Field label="¿Tiene dirección geoelectoral?"><Sel value={f.tieneDirGeoelectoral} onChange={set("tieneDirGeoelectoral")} items={SN} /></Field>
        <Field label="Lugar de votación"><Sel value={f.idLugarVotacion} onChange={set("idLugarVotacion")} items={lugares} /></Field>
      </Section>
      <Section title="4. Afiliación">
        <Field label="Condición de afiliado"><Sel value={f.idCondicionAfiliado} onChange={set("idCondicionAfiliado")} items={cat?.condicionesAfiliado ?? []} /></Field>
      </Section>
      <div className="flex gap-2">
        <Btn type="submit" disabled={busy || !cat}>{busy ? "Guardando…" : "Guardar registro"}</Btn>
        <Btn type="button" secondary onClick={() => { setF(EMPTY); setErr(""); }}>Limpiar</Btn>
      </div>
    </form>
  );
}

const GROUPS: [string, [string, string][]][] = [
  ["Identificación", [["no_empadronamiento", "No. de empadronamiento"], ["cui", "CUI"], ["fecha_emision_dpi", "Fecha de emisión del DPI"]]],
  ["Datos personales", [["nombre_completo", "Nombre completo"], ["genero", "Género"], ["lee_escribe", "Lee y escribe"], ["vidente", "Vidente"]]],
  ["Vecindad del DPI", [["departamento_vecindad", "Departamento"], ["municipio_vecindad", "Municipio"]]],
  ["Residencia y lugar de votación", [["departamento_residencia", "Departamento"], ["municipio_residencia", "Municipio"], ["tiene_dir_geoelectoral", "Dirección geoelectoral"], ["lugar_votacion", "Lugar de votación"], ["condicion_afiliado", "Condición de afiliado"]]],
];

function Consulta() {
  const [tipo, setTipo] = useState("EMPADRONAMIENTO");
  const [numero, setNumero] = useState("");
  const [fecha, setFecha] = useState("");
  const [cap, setCap] = useState<{ token: string; imagen: string } | null>(null);
  const [codigo, setCodigo] = useState("");
  const [err, setErr] = useState("");
  const [res, setRes] = useState<Record<string, string> | null>(null);
  const [busy, setBusy] = useState(false);

  async function loadCaptcha() {
    setCodigo("");
    if (isDemo()) return setCap({ token: "demo", imagen: DEMO_CAPTCHA });
    try {
      const { status, data } = await call("/captcha");
      if (status === 200) setCap(data); else setErr(data.mensaje ?? "No se pudo cargar el código de verificación.");
    } catch { setErr("No fue posible cargar el código de verificación."); }
  }
  useEffect(() => { loadCaptcha(); }, []);

  async function buscar(e: FormEvent) {
    e.preventDefault(); setErr(""); setRes(null);
    if (!/^\d{2}\/\d{2}\/\d{4}$/.test(fecha)) return setErr("Escriba la fecha de nacimiento como día/mes/año, por ejemplo 15/03/1985.");
    if (tipo === "DPI" && !/^\d{13}$/.test(numero)) return setErr("El CUI debe tener exactamente 13 dígitos.");
    setBusy(true);
    try {
      if (isDemo()) setRes({ ...DEMO_RESULT, ...(tipo === "DPI" ? { cui: numero } : { no_empadronamiento: numero }) });
      else {
        const { status, data } = await call("/consulta", { tipoDocumento: tipo, numero: numero.trim(), fechaNacimiento: fecha, captchaToken: cap?.token ?? "", captchaCodigo: codigo.trim() });
        if (status === 200) setRes(data); else setErr(data.mensaje ?? "No se encontraron resultados.");
      }
    } catch { setErr("No fue posible conectar con el servidor. Intente más tarde."); }
    finally { setBusy(false); loadCaptcha(); }
  }

  if (res) return (
    <div className="space-y-4">
      <div className="flex items-baseline justify-between border-b border-border pb-2">
        <h2 className="text-lg font-semibold text-foreground">Resultados de la búsqueda</h2>
        <span className="text-sm">Estado del registro: <strong className="text-primary">{res["estado"] ?? "—"}</strong></span>
      </div>
      {GROUPS.map(([title, rows]) => (
        <div key={title} className="border border-border">
          <h3 className="border-b border-border bg-muted px-3 py-1.5 text-sm font-semibold text-foreground">{title}</h3>
          <dl className="divide-y divide-border text-sm">
            {rows.map(([k, l]) => (
              <div key={k} className="grid grid-cols-2 px-3 py-1.5">
                <dt className="text-muted-foreground">{l}</dt><dd className="text-foreground">{res[k] ?? "—"}</dd>
              </div>
            ))}
          </dl>
        </div>
      ))}
      <div className="flex gap-2 print:hidden">
        <Btn onClick={() => { setRes(null); setNumero(""); setFecha(""); }}>Nueva consulta</Btn>
        <Btn secondary onClick={() => window.print()}>Imprimir</Btn>
      </div>
    </div>
  );

  return (
    <form onSubmit={buscar} className="space-y-5">
      {err && <ErrorBox msg={err} />}
      <fieldset className="border border-border p-4">
        <legend className="px-1 text-sm font-semibold text-primary">Datos de búsqueda</legend>
        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="Tipo de documento">
            <select className={input} value={tipo} onChange={(e) => setTipo(e.target.value)}>
              <option value="EMPADRONAMIENTO">Empadronamiento (No. de boleta)</option>
              <option value="DPI">DPI (CUI de 13 dígitos)</option>
            </select>
          </Field>
          <Field label="Número de documento">
            <input className={input} required inputMode="numeric" maxLength={tipo === "DPI" ? 13 : 20} value={numero}
              onChange={(e) => setNumero(tipo === "DPI" ? e.target.value.replace(/\D/g, "") : e.target.value)} />
          </Field>
          <Field label="Fecha de nacimiento" hint="Día/mes/año, por ejemplo 15/03/1985">
            <input className={input} required placeholder="DD/MM/AAAA" maxLength={10} value={fecha} onChange={(e) => setFecha(e.target.value)} />
          </Field>
          <Field label="Código de verificación" hint="Escriba los caracteres que ve en la imagen">
            <div className="flex items-center gap-2">
              {cap ? <img src={cap.imagen} alt="Código de verificación" className="h-11 border border-border" /> : <span className="text-xs text-muted-foreground">Cargando…</span>}
              <button type="button" onClick={loadCaptcha} className="text-xs text-primary underline">Cambiar código</button>
            </div>
            <input className={`${input} mt-2`} required maxLength={10} value={codigo} onChange={(e) => setCodigo(e.target.value)} />
          </Field>
        </div>
      </fieldset>
      <Btn type="submit" disabled={busy}>{busy ? "Buscando…" : "Buscar"}</Btn>
    </form>
  );
}
