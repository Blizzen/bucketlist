package online.blizzen.bucketlist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import online.blizzen.bucketlist.ui.BucketlistScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Client entrypoint. Bucketlist is a client-side advancement engine: it loads
 * advancement packs (standard datapack layout), evaluates their criteria against
 * local client state, persists progress per-server, and renders an advancement-style
 * screen with its own toasts — all without any server-side datapack.
 *
 * <p>v0.1 wiring (see docs/DESIGN.md) is stubbed below; this scaffold registers the
 * open keybind and the tick hook only.
 */
public class BucketlistClient implements ClientModInitializer {
	private static KeyBinding openKey;

	@Override
	public void onInitializeClient() {
		Bucketlist.LOGGER.info("Bucketlist starting (client-side advancement engine)");

		openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.bucketlist.open",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN, // unbound by default; the user binds it in the Controls menu
				"key.categories.bucketlist"
		));

		// TODO(v0.1): bootstrap the engine here:
		//   - PackLoader.loadBundledFishPack() (+ later, drop-in packs)
		//   - CollectionStore for the active server
		//   - register CriterionEvaluator(s): InventoryChangedEvaluator first
		//   - construct the CollectionScanner and ToastQueue

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.wasPressed()) {
				client.setScreen(new BucketlistScreen());
			}
			// TODO(v0.1): run the CollectionScanner (deep-scan: player inventory +
			// held shulkers' container component + any open container screen), feed new
			// variants to the CollectionStore + ToastQueue.
		});
	}
}
