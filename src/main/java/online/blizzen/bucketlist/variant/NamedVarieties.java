package online.blizzen.bucketlist.variant;

import java.util.HashSet;
import java.util.Set;

/**
 * The 22 predefined "named" tropical fish varieties — the curated, recognizable fish that
 * show a proper name in the bucket tooltip. This is the reachable sub-goal alongside the
 * full {@value TropicalFishVariant#TOTAL}.
 *
 * <p>Source of truth: {@code TropicalFishEntity.COMMON_VARIANTS} (decompiled 1.21.4). Each
 * entry is (size, pattern, baseColorId, patternColorId); color ids are vanilla
 * {@code DyeColor} ids (white=0 … black=15).
 */
public final class NamedVarieties {

	/** Packed variant ints of the 22 named varieties. */
	public static final Set<Integer> PACKED;

	static {
		Set<Integer> s = new HashSet<>();
		s.add(p(1, 1, 1, 7));   // stripey  / orange / gray
		s.add(p(1, 0, 7, 7));   // flopper  / gray   / gray
		s.add(p(1, 0, 7, 11));  // flopper  / gray   / blue
		s.add(p(1, 5, 0, 7));   // clayfish / white  / gray
		s.add(p(0, 1, 11, 7));  // sunstreak/ blue   / gray
		s.add(p(0, 0, 1, 0));   // kob      / orange / white
		s.add(p(0, 5, 6, 3));   // spotty   / pink   / light_blue
		s.add(p(1, 3, 10, 4));  // blockfish/ purple / yellow
		s.add(p(1, 5, 0, 14));  // clayfish / white  / red
		s.add(p(0, 5, 0, 4));   // spotty   / white  / yellow
		s.add(p(1, 2, 0, 7));   // glitter  / white  / gray
		s.add(p(1, 5, 0, 1));   // clayfish / white  / orange
		s.add(p(0, 3, 9, 6));   // dasher   / cyan   / pink
		s.add(p(0, 4, 5, 3));   // brinely  / lime   / light_blue
		s.add(p(1, 4, 14, 0));  // betty    / red    / white
		s.add(p(0, 2, 7, 14));  // snooper  / gray   / red
		s.add(p(1, 3, 14, 0));  // blockfish/ red    / white
		s.add(p(1, 0, 0, 4));   // flopper  / white  / yellow
		s.add(p(0, 0, 14, 0));  // kob      / red    / white
		s.add(p(0, 1, 7, 0));   // sunstreak/ gray   / white
		s.add(p(0, 3, 9, 4));   // dasher   / cyan   / yellow
		s.add(p(1, 0, 4, 4));   // flopper  / yellow / yellow
		PACKED = Set.copyOf(s);
	}

	private static int p(int size, int pattern, int base, int patternColor) {
		return new TropicalFishVariant(size, pattern, base, patternColor).pack();
	}

	public static int total() {
		return PACKED.size(); // 22
	}

	public static boolean isNamed(int packedVariant) {
		return PACKED.contains(packedVariant);
	}

	private NamedVarieties() {}
}
