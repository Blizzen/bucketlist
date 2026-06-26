# Bucketlist

A **client-side advancement engine** for Minecraft (Fabric 1.21.4).

Bucketlist tracks your collection of all **3,072 tropical fish variants** on *any* server —
including vanilla servers — with **nothing installed server-side**. It watches what you
collect locally, stores your progress per-server, and shows it in its own
advancement-style screen with its own toasts.

Under the hood it's a generic engine: it loads **standard Minecraft advancement JSON**
(real datapack layout) and evaluates the criteria client-side. The tropical-fish pack is
the flagship bundled content — but you can drop in your own advancement pack (built in any
advancement editor) and have it tracked client-side too.

> Inspired by [AntoninHuaut/TropicalFish](https://github.com/AntoninHuaut/TropicalFish)
> (a server-side datapack). Bucketlist re-implements the *experience* as a standalone,
> client-only mod so it works on servers you don't control. See
> [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md).

## Status

🚧 **Scaffold / pre-release (`0.1.0` in progress).** The project structure, build, generic
engine architecture, and the flagship fish-pack generator are in place. The engine wiring
(pack loading, scanning, persistence, screen rendering, toasts) is stubbed and lands in
`0.1.0`. See [docs/DESIGN.md](docs/DESIGN.md) for the full design and the staged plan.

## How it works (the short version)

| Concern | Approach |
| --- | --- |
| Getting "the same effect" client-side | A self-owned collection tracker that *looks* like the advancement tabs but owns 100% of its data — no server advancements involved. |
| What counts as "caught" | Collecting a **Bucket of Tropical Fish** (its variant is read from the item's `bucket_entity_data`). |
| Where it looks | Deep scan: player inventory **+** held shulker boxes' contents **+** any open container (great for stash hunting). |
| Progress storage | Per-server JSON the mod manages itself (`config/bucketlist/<server>.json`). You never edit a file. |
| The UI | A vanilla-style advancement screen (tree + frames + lines + dirt), with **procedurally tinted live fish** as icons — no shipped textures. |
| The pack format | Standard advancement JSON in real datapack layout, so any advancement editor's output works. |

## Building

Requires JDK 21.

```sh
./gradlew build
```

The built jar lands in `build/libs/`.

## Regenerating the flagship fish pack

The bundled pack is generated from a single program (the source of truth). It lives at
`src/main/resources/bucketlist/packs/tropicalfish/` — deliberately **not** under jar-root
`data/`, because a Fabric mod's jar-root `data/` is auto-loaded as a real datapack by the
(integrated) server, which would grant the advancements for real. Bucketlist is a client
engine, so its packs are client-read resources.

```sh
# tiny committed sample fixture (global tab + flopper tab, 2x2 colors)
java tools/gen/FishPackGenerator.java src/main/resources/bucketlist/packs/tropicalfish --sample

# the full 3,072-leaf tree
java tools/gen/FishPackGenerator.java src/main/resources/bucketlist/packs/tropicalfish
```

## License

[GPL-3.0-or-later](LICENSE).
