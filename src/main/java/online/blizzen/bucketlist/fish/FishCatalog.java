package online.blizzen.bucketlist.fish;

import online.blizzen.bucketlist.variant.NamedVarieties;
import online.blizzen.bucketlist.variant.TropicalFishVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * The full tropical-fish collection, <b>generated in code</b> — no data files, nothing the
 * server can see. 12 types (2 sizes x 6 patterns), each a 16x16 base/pattern-color grid =
 * {@value TropicalFishVariant#TOTAL} variants.
 *
 * <p>This is the flagship content and the data source the collection screen renders from.
 * Bucketlist is a standalone client mod: this is plain Java data built at class-load, never
 * a datapack and never handed to Minecraft's advancement system.
 */
public final class FishCatalog {

	/** One collectible variant (a leaf in a type's grid). */
	public record Leaf(int variant, int baseColor, int patternColor, boolean named) {}

	/** One fish type (a tab): a (size, pattern) pair with its 256-variant grid. */
	public record Type(int index, String name, int size, int pattern, List<Leaf> leaves) {}

	private static final List<Type> TYPES = build();

	public static List<Type> types() {
		return TYPES;
	}

	public static int totalVariants() {
		return TropicalFishVariant.TOTAL;
	}

	public static int namedTotal() {
		return NamedVarieties.total();
	}

	private static List<Type> build() {
		List<Type> types = new ArrayList<>(TropicalFishVariant.TYPES);
		for (int t = 0; t < TropicalFishVariant.TYPES; t++) {
			int size = t / TropicalFishVariant.PATTERNS;
			int pattern = t % TropicalFishVariant.PATTERNS;
			List<Leaf> leaves = new ArrayList<>(TropicalFishVariant.COLORS * TropicalFishVariant.COLORS);
			for (int base = 0; base < TropicalFishVariant.COLORS; base++) {
				for (int patternColor = 0; patternColor < TropicalFishVariant.COLORS; patternColor++) {
					int variant = new TropicalFishVariant(size, pattern, base, patternColor).pack();
					leaves.add(new Leaf(variant, base, patternColor, NamedVarieties.isNamed(variant)));
				}
			}
			types.add(new Type(t, TropicalFishVariant.TYPE_NAMES[t], size, pattern, List.copyOf(leaves)));
		}
		return List.copyOf(types);
	}

	private FishCatalog() {}
}
