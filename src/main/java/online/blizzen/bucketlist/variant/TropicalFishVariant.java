package online.blizzen.bucketlist.variant;

/**
 * A tropical fish variant: 2 body sizes x 6 patterns x 16 base colors x 16 pattern colors
 * = {@value #TOTAL} total combinations.
 *
 * <p>The "type" of a fish (its tab in the UI) is the (size, pattern) pair — 12 of them,
 * named kob/sunstreak/.../clayfish.
 */
public record TropicalFishVariant(int size, int pattern, int baseColor, int patternColor) {
	public static final int SIZES = 2;
	public static final int PATTERNS = 6;
	public static final int COLORS = 16;
	public static final int TYPES = SIZES * PATTERNS;          // 12
	public static final int TOTAL = TYPES * COLORS * COLORS;   // 3072

	/**
	 * Packs into the integer carried by a Bucket of Tropical Fish in its
	 * {@code minecraft:bucket_entity_data} {@code BucketVariantTag}.
	 *
	 * <p>VERIFY-AT-RUNTIME: this uses the historical vanilla layout
	 * {@code size | pattern<<8 | baseColor<<16 | patternColor<<24}. Confirm against an
	 * actual 1.21.4 bucket before relying on it (see docs/DESIGN.md "Open verifications").
	 */
	public int pack() {
		return (size & 0xFF)
				| ((pattern & 0xFF) << 8)
				| ((baseColor & 0xFF) << 16)
				| ((patternColor & 0xFF) << 24);
	}

	public static TropicalFishVariant unpack(int packed) {
		return new TropicalFishVariant(
				packed & 0xFF,
				(packed >> 8) & 0xFF,
				(packed >> 16) & 0xFF,
				(packed >> 24) & 0xFF);
	}

	/** Index in [0, 12): which (size, pattern) tab this variant belongs to. */
	public int typeIndex() {
		return size * PATTERNS + pattern;
	}
}
