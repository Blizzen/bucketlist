package online.blizzen.bucketlist.store;

import java.util.Set;

/**
 * Per-server collection persistence.
 *
 * <p>Keyed by server identity — multiplayer: {@code host:port}; singleplayer: world folder
 * name. Stored as JSON under {@code config/bucketlist/<server>.json}, recording a
 * first-collected timestamp per variant. The mod creates and manages this file itself; the
 * user never authors it.
 */
public final class CollectionStore {

	// TODO(v0.1): resolve the active server key, load/save JSON, and expose the collected
	// variant set. markCollected returns true the first time a variant is seen (drives toasts).
	public Set<Integer> collectedVariants() {
		return Set.of();
	}

	public boolean markCollected(int variant) {
		return false;
	}
}
