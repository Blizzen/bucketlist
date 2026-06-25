package online.blizzen.bucketlist.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.blizzen.bucketlist.Bucketlist;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Per-server collection persistence.
 *
 * <p>Keyed by server identity — multiplayer: {@code host:port}; singleplayer: world name.
 * Stored as JSON under {@code config/bucketlist/<server>.json} with a first-collected
 * timestamp (epoch millis) per variant. The mod creates and manages this file itself; the
 * user never authors it.
 */
public final class CollectionStore {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path dir;
	private Path activeFile;
	private final Map<Integer, Long> collected = new HashMap<>();
	private boolean dirty;

	public CollectionStore(Path configDir) {
		this.dir = configDir.resolve(Bucketlist.MOD_ID);
	}

	/** Switch to a server's collection, flushing any previous one. */
	public void open(String serverKey) {
		close();
		this.activeFile = dir.resolve(sanitize(serverKey) + ".json");
		load();
	}

	public void close() {
		save();
		activeFile = null;
		collected.clear();
		dirty = false;
	}

	public boolean isOpen() {
		return activeFile != null;
	}

	public Set<Integer> collectedVariants() {
		return Collections.unmodifiableSet(collected.keySet());
	}

	public int count() {
		return collected.size();
	}

	/** Records a variant if not already present. Returns true the first time it is seen. */
	public boolean markCollected(int variant, long epochMillis) {
		if (collected.containsKey(variant)) {
			return false;
		}
		collected.put(variant, epochMillis);
		dirty = true;
		return true;
	}

	public void saveIfDirty() {
		if (dirty) {
			save();
		}
	}

	private void load() {
		if (activeFile == null || !Files.exists(activeFile)) {
			return;
		}
		try (Reader r = Files.newBufferedReader(activeFile)) {
			StoreData data = GSON.fromJson(r, StoreData.class);
			if (data != null && data.variants != null) {
				data.variants.forEach((k, v) -> {
					try {
						collected.put(Integer.parseInt(k), v);
					} catch (NumberFormatException ignored) {
						// skip malformed keys
					}
				});
			}
		} catch (Exception e) {
			Bucketlist.LOGGER.warn("Failed to load collection {}", activeFile, e);
		}
	}

	private void save() {
		if (activeFile == null || !dirty) {
			return;
		}
		try {
			Files.createDirectories(dir);
			StoreData data = new StoreData();
			data.variants = new TreeMap<>();
			collected.forEach((k, v) -> data.variants.put(String.valueOf(k), v));
			try (Writer w = Files.newBufferedWriter(activeFile)) {
				GSON.toJson(data, w);
			}
			dirty = false;
		} catch (Exception e) {
			Bucketlist.LOGGER.warn("Failed to save collection {}", activeFile, e);
		}
	}

	private static String sanitize(String key) {
		return key.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	/** JSON shape: {@code { "variants": { "<packedInt>": <epochMillis> } }}. */
	private static final class StoreData {
		Map<String, Long> variants;
	}
}
