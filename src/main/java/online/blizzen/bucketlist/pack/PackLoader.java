package online.blizzen.bucketlist.pack;

/**
 * Loads advancement packs from standard datapack layout
 * ({@code data/<namespace>/advancement/<path>.json}).
 *
 * <ul>
 *   <li>v0.1: the bundled fish pack from the mod jar.</li>
 *   <li>v0.2: drop-in packs from {@code config/bucketlist/packs/<name>/} — the
 *       "advancement editor" linkage: any datapack you author in an editor is tracked
 *       client-side with no server cooperation.</li>
 * </ul>
 */
public final class PackLoader {
	private PackLoader() {}

	// TODO(v0.1): walk resources under data/<ns>/advancement/, parse each file
	// (parent / display / criteria / requirements) plus the additive
	// "gu:bucket_variant" and "gu:icon" hints, link parents->children, and assemble
	// AdvancementPack instances.
	public static AdvancementPack loadBundledFishPack() {
		throw new UnsupportedOperationException("TODO(v0.1): bundled pack loading");
	}
}
