package online.blizzen.bucketlist.gen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the bundled tropical-fish advancement pack in standard datapack layout
 * ({@code data/bucketlist/advancement/...}). This is the flagship pack the engine
 * dogfoods: the exact same kind of files a user would drop in, or an advancement editor
 * would export.
 *
 * <p>Run (Java 21+):
 * <pre>
 *   java tools/gen/FishPackGenerator.java &lt;outDir&gt; [--sample]
 * </pre>
 * {@code <outDir>} should be a resources root (files land under
 * {@code <outDir>/data/bucketlist/advancement/}). {@code --sample} emits a tiny fixture
 * (the global tab + the flopper tab, 2 base colors x 2 pattern colors) instead of the full
 * 3,072-leaf tree.
 *
 * <p>Tree shape (mirrors AntoninHuaut/TropicalFish):
 * <pre>
 *   global/root            (challenge)         one Global overview tab
 *     global/&lt;type&gt;          (goal)            12 per-type summary nodes
 *   &lt;type&gt;/root             (goal)             12 per-type tabs
 *     &lt;type&gt;/&lt;base&gt;          (goal)            16 base-color nodes per type
 *       &lt;type&gt;/&lt;base&gt;/pattern_&lt;pc&gt;  (task)    16 leaves per base = 3072 total
 * </pre>
 *
 * <p>VERIFY-AT-RUNTIME: the type-name ordering and the {@link #pack} layout below are the
 * well-known historical vanilla values; confirm against a real 1.21.4 bucket before the
 * full pack is wired into the build.
 */
public final class FishPackGenerator {

	static final String NS = "bucketlist";

	// 16 dye colors, index 0..15.
	static final String[] COLORS = {
			"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
			"light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
	};

	// 12 types = 2 sizes x 6 patterns, index 0..11 (size = i / 6, pattern = i % 6).
	static final String[] TYPES = {
			"kob", "sunstreak", "snooper", "dasher", "brinely", "spotty",        // small body
			"flopper", "stripey", "glitter", "blockfish", "betty", "clayfish"    // large body
	};

	public static void main(String[] args) throws IOException {
		Path out = Path.of(args.length > 0 ? args[0] : ".");
		boolean sample = List.of(args).contains("--sample");

		int[] typeIdx = sample ? new int[] {6} : range(TYPES.length);          // 6 = flopper
		int[] baseIdx = sample ? new int[] {14, 11} : range(COLORS.length);    // red, blue
		int[] patIdx = sample ? new int[] {0, 15} : range(COLORS.length);      // white, black

		Path advRoot = out.resolve("data").resolve(NS).resolve("advancement");
		int files = 0;

		// Global overview tab.
		write(advRoot.resolve("global/root.json"), advancement(
				null, "Tropical Fish", "Collect every tropical fish variant.",
				"challenge", true, repr(0, 0, 0, 0), true));
		files++;
		for (int t : typeIdx) {
			write(advRoot.resolve("global/" + TYPES[t] + ".json"), advancement(
					NS + ":global/root", cap(TYPES[t]), "Complete the " + TYPES[t] + " type.",
					"goal", false, repr(t, 0, 0, 0), false));
			files++;
		}

		// Per-type tabs.
		for (int t : typeIdx) {
			int size = t / TropicalFishVariantSizes();
			int pattern = t % 6;

			write(advRoot.resolve(TYPES[t] + "/root.json"), advancement(
					null, cap(TYPES[t]), "Collect every " + TYPES[t] + " variant.",
					"goal", false, pack(size, pattern, 0, 0), true));
			files++;

			for (int b : baseIdx) {
				String baseNode = NS + ":" + TYPES[t] + "/" + COLORS[b];
				write(advRoot.resolve(TYPES[t] + "/" + COLORS[b] + ".json"), advancement(
						NS + ":" + TYPES[t] + "/root", cap(COLORS[b]) + " body",
						cap(COLORS[b]) + "-bodied " + TYPES[t] + "s.",
						"goal", false, pack(size, pattern, b, 0), false));
				files++;

				for (int p : patIdx) {
					int variant = pack(size, pattern, b, p);
					write(advRoot.resolve(TYPES[t] + "/" + COLORS[b] + "/pattern_" + COLORS[p] + ".json"),
							leaf(baseNode, cap(COLORS[b]) + " / " + cap(COLORS[p]),
									"A " + COLORS[b] + " " + TYPES[t] + " with " + COLORS[p] + " pattern.",
									variant));
					files++;
				}
			}
		}

		System.out.println("Generated " + files + " advancement files under " + advRoot
				+ (sample ? " (sample)" : ""));
	}

	// --- JSON builders -------------------------------------------------------

	/** A non-leaf node (root / type / base). Uses minecraft:impossible so the engine, not a
	 *  vanilla criterion, decides completion (all descendants collected). */
	static String advancement(String parent, String title, String desc, String frame,
			boolean toast, int reprVariant, boolean isRoot) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		if (parent != null) sb.append("  \"parent\": \"").append(parent).append("\",\n");
		sb.append(display(title, desc, frame, toast, isRoot));
		sb.append("  \"criteria\": {\n");
		sb.append("    \"derived\": { \"trigger\": \"minecraft:impossible\" }\n");
		sb.append("  },\n");
		sb.append("  \"gu:icon\": { \"type\": \"tropical_fish\", \"variant\": ").append(reprVariant).append(" }\n");
		sb.append("}\n");
		return sb.toString();
	}

	/** A leaf node: real inventory_changed criterion + the precise variant hint. */
	static String leaf(String parent, String title, String desc, int variant) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"parent\": \"").append(parent).append("\",\n");
		sb.append(display(title, desc, "task", true, false));
		sb.append("  \"criteria\": {\n");
		sb.append("    \"collect\": {\n");
		sb.append("      \"trigger\": \"minecraft:inventory_changed\",\n");
		sb.append("      \"conditions\": { \"items\": [ { \"items\": \"minecraft:tropical_fish_bucket\" } ] }\n");
		sb.append("    }\n");
		sb.append("  },\n");
		sb.append("  \"requirements\": [ [ \"collect\" ] ],\n");
		sb.append("  \"gu:bucket_variant\": ").append(variant).append(",\n");
		sb.append("  \"gu:icon\": { \"type\": \"tropical_fish\", \"variant\": ").append(variant).append(" }\n");
		sb.append("}\n");
		return sb.toString();
	}

	static String display(String title, String desc, String frame, boolean toast, boolean isRoot) {
		StringBuilder sb = new StringBuilder();
		sb.append("  \"display\": {\n");
		sb.append("    \"icon\": { \"id\": \"minecraft:tropical_fish_bucket\" },\n");
		sb.append("    \"title\": \"").append(esc(title)).append("\",\n");
		sb.append("    \"description\": \"").append(esc(desc)).append("\",\n");
		sb.append("    \"frame\": \"").append(frame).append("\",\n");
		sb.append("    \"show_toast\": ").append(toast).append(",\n");
		sb.append("    \"announce_to_chat\": false,\n");
		sb.append("    \"hidden\": false");
		if (isRoot) {
			sb.append(",\n    \"background\": \"minecraft:textures/gui/advancements/backgrounds/stone.png\"\n");
		} else {
			sb.append("\n");
		}
		sb.append("  },\n");
		return sb.toString();
	}

	// --- helpers -------------------------------------------------------------

	/** size | pattern<<8 | base<<16 | patternColor<<24  (VERIFY-AT-RUNTIME). */
	static int pack(int size, int pattern, int base, int patternColor) {
		return (size & 0xFF) | ((pattern & 0xFF) << 8) | ((base & 0xFF) << 16) | ((patternColor & 0xFF) << 24);
	}

	static int repr(int typeIdx, int unusedPattern, int base, int patternColor) {
		return pack(typeIdx / 6, typeIdx % 6, base, patternColor);
	}

	static int TropicalFishVariantSizes() {
		return 6; // types per size; size = typeIndex / 6
	}

	static int[] range(int n) {
		int[] r = new int[n];
		for (int i = 0; i < n; i++) r[i] = i;
		return r;
	}

	static String cap(String s) {
		String t = s.replace('_', ' ');
		return Character.toUpperCase(t.charAt(0)) + t.substring(1);
	}

	static String esc(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	static void write(Path file, String content) throws IOException {
		Files.createDirectories(file.getParent());
		Files.writeString(file, content);
	}

	private FishPackGenerator() {}
}
