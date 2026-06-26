# Bucketlist — Design

A client-side advancement engine for Fabric 1.21.4. Flagship content: collect all 3,072
tropical fish variants on any server, with nothing installed server-side. This document is
the spec the scaffold was built from; it records the decisions and the staged plan.

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
| Genericity | **Generic engine**, fish as the flagship bundled pack. "Linked to an advancement editor" = it ingests standard advancement JSON. `inventory_changed` evaluator first. |
| Pack format | Standard advancement JSON in **real datapack layout** (`data/<ns>/advancement/*.json`). Bundled in jar + (later) drop-in folder. **Additive** `gu:`-namespaced fields only, so files stay editor-loadable. |
| Predicate eval | **(B) pragmatic evaluator + additive variant hint** (`gu:bucket_variant`). Evaluate the cleanly-expressible predicate fields; ride the awkward nested variant match on the hint. Grow as real packs demand. |
| Flagship pack | **(A) build-time generator → real datapack-layout files** (single code path; the flagship is the loader's own test fixture). |
| Inventory scope | **(B) deep scan** — player inventory **+** the `container` component of any held shulker box (no need to open it) **+** any open container screen. Stash-hunter friendly. |
| Entry + feedback | **Configurable keybind** to open (bound in vanilla Controls; unbound by default) **+ tiered/coalesced toasts** (single new variant → one toast; bulk scan → one "+N" summary + row/type/named/challenge completion toasts). |
| Settings | **Auto-generated** settings file with sane defaults (`config/bucketlist.json`); editable but never required. Plug-and-play: install jar → press keybind → works. The user never authors any file. |
| Naming / license | **`Bucketlist`** (modid `bucketlist`), **GPL-3.0-or-later**, public repo under `Blizzen`, crediting the MIT original. Strict semver, `0.MINOR.PATCH`, no `-alpha`/`-beta` suffixes. |

## 3. Architecture

```
online.blizzen.bucketlist
  Bucketlist            constants (MOD_ID, LOGGER)
  BucketlistClient      ClientModInitializer: keybind + tick hook; bootstraps the engine
  variant/
    TropicalFishVariant pack/unpack the variant int; type indexing
  pack/
    AdvancementPack     a loaded namespace (nodes; tab roots)
    PackLoader          datapack-layout JSON loader (bundled now; drop-in later)
  engine/
    AdvancementNode     decoded node (parent/title/frame/showToast/bucketVariant/children)
  criteria/
    CriterionEvaluator  pluggable per trigger id
    InventoryChangedEvaluator  minecraft:inventory_changed (+ gu:bucket_variant)
  detect/
    CollectionScanner   deep-scan → ScanResult (set of packed variant ints)
    ScanResult
  store/
    CollectionStore     per-server JSON persistence
  toast/
    ToastQueue          tiered + coalesced toast feedback
  ui/
    BucketlistScreen    advancement-style screen with procedural fish icons
```

Data flow each tick / inventory change:
`CollectionScanner.scan()` → `ScanResult` → evaluators mark nodes → `CollectionStore`
records newly-collected variants → `ToastQueue` emits feedback → `BucketlistScreen` renders
current state.

### The bundled fish tree (mirrors AntoninHuaut/TropicalFish)

```
global/root              (challenge)   Global overview tab
  global/<type>          (goal)        12 per-type summaries (minecraft:impossible → engine-derived)
<type>/root              (goal)        12 per-type tabs (root = a tab)
  <type>/<base>          (goal)        16 base-color nodes
    <type>/<base>/pattern_<pc>  (task) 16 leaves → 3072 total
```
Each tab = a root advancement (vanilla behaviour). Leaves carry the real
`minecraft:inventory_changed` criterion plus `gu:bucket_variant`. Generated by
`tools/gen/FishPackGenerator.java`.

## 4. MVP (v0.1) and what's deferred

**v0.1 (the vertical slice):**
1. Fabric 1.21.4 skeleton, modid `bucketlist`, GPLv3. ✅ scaffolded
2. Build-time generator → bundled fish pack. ✅ generator done; sample committed (full run verified at 3,289 files)
3. Generic loader: read datapack-layout advancement JSON (bundled).
4. `inventory_changed` evaluator + `gu:bucket_variant` hint. ◑ interface + evaluator stub in place
5. Deep-scan detector (player inv + held shulkers + open container).
6. Per-server persistence JSON.
7. Advancement-style screen (tree, frames, lines, procedural fish icons) via keybind. ◑ keybind + screen stub in place
8. Tiered/coalesced toasts.

**Deferred (post-MVP):**
- Drop-in user packs from the config folder (the public "advancement editor" linkage).
- Criteria evaluators beyond `inventory_changed`.
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
- **No required files.** The mod writes its own settings + per-server save data with
  defaults; the only user-authored files are *optional* drop-in packs (v0.2).
- **Bundled pack must NOT live at jar-root `data/`.** A Fabric mod's jar-root `data/`
  directory is auto-loaded as a real datapack by the integrated/singleplayer server. If the
  fish advancements live there, the server grants them for real — and because each leaf's
  vanilla criterion is the loose "have any tropical fish bucket," picking up one bucket
  grants *every* leaf at once, firing a storm of vanilla "Advancement Made!" toasts. The
  bundled pack therefore lives at `bucketlist/packs/tropicalfish/` (a datapack-layout folder
  outside `assets/` and `data/`) and is read only by `PackLoader`. (Regression found in
  testing: 4 phantom advancement toasts from a 4-leaf sample pack.)

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
