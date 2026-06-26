# Bucketlist — Design

A standalone client-side mod for Fabric 1.21.4 — **not a datapack**. It lets you collect all
3,072 tropical fish variants on any server with nothing installed server-side. This document
records the decisions and the staged plan.

## 1. The core constraint

A client-only mod **cannot grant real Minecraft advancements** — advancements are
server-authoritative (the server owns the tree, the criteria, and the toasts). So "the same
effect as the TropicalFish datapack" is re-implemented entirely client-side as a **personal
collection tracker**: the mod watches what you collect locally, stores progress in a client
file, and renders its own advancement-style screen and toasts. No reliance on server
advancements at all.

## 2. Decisions (from the design interview)

| # | Decision |
| --- | --- |
| Target | Client-side personal collection tracker that *looks/feels* like advancement tabs but owns 100% of its data. |
| Platform | **Fabric, MC 1.21.4** (matches the author's stack and live servers). Single-version, no Stonecutter. |
| Catch trigger | **Bucketing** — a *Bucket of Tropical Fish* (mirrors the datapack's `filled_bucket`/`inventory_changed` semantics). |
| Detection fidelity | **(A) inventory scan** — "variants collected" (any bucket that passes through), not strict "personally caught". Robust on laggy servers. |
| Persistence scope | **Per-server** (multiplayer: `host:port`; singleplayer: world folder), mirroring the datapack's per-world nature. First-collected timestamp stamped per variant. |
| Completion | **Dual goal**: 22 named varieties **and** the full 3,072. Organized by the 12 types (2 shapes x 6 patterns), each a 16x16 base/pattern-color grid. |
| UI fidelity | **Clone the vanilla advancement screen** (dirt bg, `task`/`goal`/`challenge` frames, connecting lines, tree walk, toasts), with **procedural live-fish icons** (no shipped textures). |
| Content format | **Generated in code** (`FishCatalog`), no data files. *Revised 2026-06-26*: the original plan was a generic advancement-JSON engine (so packs could be authored in an advancement editor), but a datapack-shaped format on a *client* mod caused confusion and a real footgun (see §6), and the fish set is purely combinatorial so it needs no authoring. Bucketlist is a standalone client mod, not a datapack engine. Any "import an advancement-editor pack" feature, if ever wanted, becomes a clearly-separate optional extra the mod parses itself — never a server datapack. |
| Inventory scope | **(B) deep scan** — player inventory **+** the `container` component of any held shulker box (no need to open it) **+** any open container screen. Stash-hunter friendly. |
| Entry + feedback | **Configurable keybind** to open (bound in vanilla Controls; unbound by default) **+ tiered/coalesced toasts** (single new variant → one toast; bulk scan → one "+N" summary + row/type/named/challenge completion toasts). |
| Settings | **Auto-generated** settings file with sane defaults (`config/bucketlist.json`); editable but never required. Plug-and-play: install jar → press keybind → works. The user never authors any file. |
| Naming / license | **`Bucketlist`** (modid `bucketlist`), **GPL-3.0-or-later**, public repo under `Blizzen`, crediting the MIT original. Strict semver, `0.MINOR.PATCH`, no `-alpha`/`-beta` suffixes. |

## 3. Architecture

```
online.blizzen.bucketlist
  Bucketlist            constants (MOD_ID, LOGGER)
  BucketlistClient      ClientModInitializer: keybind + tick hook; store lifecycle; scan→store→toast
  variant/
    TropicalFishVariant pack/unpack the variant int; type indexing; names; describe()
    NamedVarieties      the 22 named varieties (from TropicalFishEntity.COMMON_VARIANTS)
  fish/
    FishCatalog         the 3072-variant collection, BUILT IN CODE (12 types × 16 × 16)
  detect/
    CollectionScanner   deep-scan → ScanResult (set of packed variant ints)
    ScanResult
    Diagnostics         JSONL scan events for debugging
  store/
    CollectionStore     per-server JSON persistence
  toast/
    ToastQueue          tiered + coalesced toast feedback (vanilla SystemToast)
  ui/
    BucketlistScreen    advancement-style screen with procedural fish icons (renders FishCatalog)
```

Data flow each tick / inventory change:
`CollectionScanner.scan()` → `ScanResult` → `CollectionStore` records newly-collected
variants → debounced `ToastQueue` emits feedback → `BucketlistScreen` renders `FishCatalog`
against the store's collected set.

### The fish collection (mirrors AntoninHuaut/TropicalFish), generated in code

`FishCatalog` builds 12 types (2 sizes × 6 patterns), each a 16×16 base/pattern-color grid =
3072 variants, flagging the 22 named ones. The screen presents a Global overview tab plus one
tab per type; "collected" is read from the per-server store. No JSON, no datapack, no files.

## 4. MVP (v0.1) and what's deferred

**v0.1:**
1. Fabric 1.21.4 skeleton, modid `bucketlist`, GPLv3. ✅
2. `FishCatalog`: the 3072-variant collection generated in code. ✅
3. Deep-scan detector (player inv + held shulkers + open container). ✅
4. Per-server persistence JSON. ✅
5. Tiered + debounced toasts. ✅
6. Live collection counts in the screen. ✅ (interim)
7. Advancement-style screen (tabs, tree, frames, lines, procedural fish icons) rendering
   `FishCatalog` against the store. ◑ **next slice** — keybind + interim count screen in place.

**Deferred / dropped:**
- ~~Generic advancement-JSON engine + advancement-editor packs~~ — dropped from the core
  (see §2 "Content format"); could return later as an optional, clearly-separate extra.
- Entity-sighting "seen" tier; stricter "personally caught" sub-stat.
- Modrinth/CurseForge distribution; multi-version / other loaders.

## 5. Implementation notes

- **Mixin-free.** Detection (`ClientTickEvents`/`ScreenEvents`), toasts (`ToastManager`),
  screen (`Screen`), keybind (`KeyBindingHelper`), persistence (file IO) all use Fabric API
  + client hooks. Nothing in the fragile-`@Inject` risk class. Add a mixin later only if
  needed, and runtime-test it.
- **Overlay flush.** The screen draws entity icons over GUI content — needs the
  `context.draw()` + `entityVertexConsumers.draw()` flush BEFORE and AFTER each batch;
  z-test alone is unreliable across 1.21.4 GUI render layers.
- **No required files; no data files at all.** The mod writes only its own settings +
  per-server save data (with defaults). It ships **no** advancement/datapack content — the
  fish collection is `FishCatalog` in code.
- **Never put advancement content at jar-root `data/`** (the reason the format went
  code-only). A Fabric mod's jar-root `data/` directory is auto-loaded as a real datapack by
  the integrated/singleplayer server. When the fish advancements briefly lived there, the
  server granted them for real — and because each leaf's vanilla criterion was the loose
  "have any tropical fish bucket," picking up one bucket granted *every* leaf at once, firing
  a storm of vanilla "Advancement Made!" toasts (the v0.1 "5 toasts from one catch" bug).
  Generating in code removes this entire failure class: there is nothing for any server to
  load.

## 6. Verifications — CLOSED against decompiled 1.21.4 source

All three were confirmed by reading `net.minecraft.entity.passive.TropicalFishEntity`
(yarn 1.21.4+build.8, via `gradlew genSources`):

- **Variant packing** ✅ — `getVariantId` is
  `variety.getId() & 65535 | baseColor.getId()<<16 | patternColor.getId()<<24`, and
  `Variety.id = size.id | pattern<<8`. That is exactly
  `size | pattern<<8 | base<<16 | patternColor<<24` — matches `TropicalFishVariant.pack`
  and the generator. The bucket stores it in `BUCKET_ENTITY_DATA` under
  `"BucketVariantTag"` (`copyDataToStack`, `BUCKET_VARIANT_TAG_KEY`).
- **Type ordering** ✅ — `Variety` enum is KOB,SUNSTREAK,SNOOPER,DASHER,BRINELY,SPOTTY
  (SMALL 0–5), then FLOPPER,STRIPEY,GLITTER,BLOCKFISH,BETTY,CLAYFISH (LARGE 0–5) — exactly
  the `TYPES[]` order with `typeIndex = size*6 + pattern`.
- **The 22 named varieties** ✅ — `COMMON_VARIANTS` gives the exact set; baked into
  `online.blizzen.bucketlist.variant.NamedVarieties`.

Remaining (only matters for editor round-tripping; the engine renders its own visuals):
advancement `display.icon` / `background` schema for 1.21.4.
