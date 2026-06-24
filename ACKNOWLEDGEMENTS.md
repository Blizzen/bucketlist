# Acknowledgements

Bucketlist is inspired by **[AntoninHuaut/TropicalFish](https://github.com/AntoninHuaut/TropicalFish)**
(MIT License), a server-side datapack + resourcepack that lets players collect all 3,072
tropical fish variants via custom advancement tabs.

Bucketlist is an **independent re-implementation**, not a fork or a copy of that project's
code. It targets a different runtime (a client-side Fabric mod rather than a
datapack/resourcepack) and is written from scratch in Java. What it borrows is the *design
and the experience*:

- the goal of collecting every tropical fish variant,
- the two-goal framing (the 22 named varieties + the full 3,072),
- the advancement-tree presentation (Global tab + per-type tabs; `challenge`/`goal`/`task`
  frames), and
- the `inventory_changed` on a tropical-fish bucket detection idea.

Thank you to AntoninHuaut for the original concept.

The TropicalFish project is licensed under the MIT License; Bucketlist is licensed under
GPL-3.0-or-later. The two projects share no source code.
