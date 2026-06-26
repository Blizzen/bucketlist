# Bucketlist

A **standalone client-side mod** for Minecraft (Fabric 1.21.4).

Bucketlist tracks your collection of all **3,072 tropical fish variants** on *any* server —
including vanilla servers — with **nothing installed server-side**. It watches what you
collect locally, stores your progress per-server, and shows it in its own
advancement-style screen with its own toasts.

It is **not a datapack** and does not use one. The whole fish collection is generated in
plain Java inside the mod; the mod reads *your* client inventory and tracks *your* progress
locally. The server is never involved, never sees the mod's data, and needs nothing
installed. That is the entire point — it works on multiplayer servers you don't control.

> Inspired by [AntoninHuaut/TropicalFish](https://github.com/AntoninHuaut/TropicalFish)
> (a server-side datapack). Bucketlist re-implements the *experience* as a standalone,
> client-only mod so it works on servers you don't control. See
> [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md).

## Status

🚧 **`0.1.0` in progress.** Working: deep-scan detection, per-server persistence, live
collection counts, and tiered/debounced toasts. Remaining for `0.1.0`: the advancement-style
collection screen (tree + procedural live-fish icons). See [docs/DESIGN.md](docs/DESIGN.md)
for the full design and the staged plan.

## How it works (the short version)

| Concern | Approach |
| --- | --- |
| Getting "the same effect" client-side | A self-owned collection tracker that *looks* like the advancement tabs but owns 100% of its data — no server advancements, no datapack. |
| What counts as "caught" | Collecting a **Bucket of Tropical Fish** (its variant is read from the item's `bucket_entity_data`). |
| Where it looks | Deep scan: player inventory **+** held shulker boxes' contents **+** any open container (great for stash hunting). |
| Progress storage | Per-server JSON the mod manages itself (`config/bucketlist/<server>.json`). You never edit a file. |
| The UI | A vanilla-style advancement screen (tree + frames + lines + dirt), with **procedurally tinted live fish** as icons — no shipped textures. |
| The fish data | Generated in code (`FishCatalog`): 12 types × 16 × 16 = 3,072 variants. No JSON, no data files. |

## Building

Requires JDK 21.

```sh
./gradlew build
```

The built jar lands in `build/libs/`.

## License

[GPL-3.0-or-later](LICENSE).
