package online.blizzen.bucketlist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import online.blizzen.bucketlist.detect.CollectionScanner;
import online.blizzen.bucketlist.detect.Diagnostics;
import online.blizzen.bucketlist.detect.ScanResult;
import online.blizzen.bucketlist.store.CollectionStore;
import online.blizzen.bucketlist.ui.BucketlistScreen;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Client entrypoint. Bucketlist is a client-side advancement engine: it watches what you
 * collect locally, persists progress per-server, and renders an advancement-style screen
 * with its own toasts — all without any server-side datapack.
 *
 * <p>This v0.1 slice wires detection + per-server persistence + a live-count screen. The
 * full data-driven tree render and tiered toasts are the next slice (see docs/DESIGN.md).
 */
public class BucketlistClient implements ClientModInitializer {

	private static final int SCAN_INTERVAL_TICKS = 20; // ~1s

	private static CollectionStore store;
	private final CollectionScanner scanner = new CollectionScanner();
	private KeyBinding openKey;
	private int tickCounter;
	private Path configDir;
	private String currentServerKey = "unknown";

	/** The active per-server collection store (may be closed when not in a world). */
	public static CollectionStore store() {
		return store;
	}

	@Override
	public void onInitializeClient() {
		Bucketlist.LOGGER.info("Bucketlist starting (client-side advancement engine)");

		configDir = FabricLoader.getInstance().getConfigDir();
		store = new CollectionStore(configDir);

		openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.bucketlist.open",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN, // unbound by default; the user binds it in the Controls menu
				"key.categories.bucketlist"
		));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			currentServerKey = serverKey(client);
			store.open(currentServerKey);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> store.close());

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		while (openKey.wasPressed()) {
			client.setScreen(new BucketlistScreen());
		}

		if (client.world == null || client.player == null || !store.isOpen()) {
			return;
		}
		if (++tickCounter % SCAN_INTERVAL_TICKS != 0) {
			return;
		}

		ScanResult scan = scanner.scan(client);
		long now = System.currentTimeMillis();
		List<Integer> added = new ArrayList<>();
		for (int variant : scan.variants()) {
			if (store.markCollected(variant, now)) {
				added.add(variant);
			}
		}
		if (!added.isEmpty()) {
			store.saveIfDirty();
			Diagnostics.writeScanEvent(configDir, now, currentServerKey, scan, added, store.count());
			// TODO(next slice): feed the ToastQueue for tiered/coalesced toasts.
			Bucketlist.LOGGER.info("Bucketlist: +{} new variant(s); scan found {} distinct from {} bucket stack(s); {} total",
					added.size(), scan.variants().size(), scan.sourceStacks(), store.count());
		}
	}

	/** Multiplayer: server address. Singleplayer: world name. */
	private static String serverKey(MinecraftClient client) {
		ServerInfo info = client.getCurrentServerEntry();
		if (info != null && info.address != null && !info.address.isEmpty()) {
			return info.address;
		}
		if (client.getServer() != null) {
			try {
				return "singleplayer/" + client.getServer().getSaveProperties().getLevelName();
			} catch (Exception ignored) {
				// fall through
			}
		}
		return "unknown";
	}
}
