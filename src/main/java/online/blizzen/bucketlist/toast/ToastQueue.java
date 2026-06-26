package online.blizzen.bucketlist.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import online.blizzen.bucketlist.variant.NamedVarieties;
import online.blizzen.bucketlist.variant.TropicalFishVariant;

import java.util.List;

/**
 * Tiered + coalesced toast feedback. One toast per collection event, chosen by salience:
 *
 * <ul>
 *   <li>completing the full {@value TropicalFishVariant#TOTAL} -> challenge toast</li>
 *   <li>completing all 22 named -> named-complete toast</li>
 *   <li>a single new variant -> a "new variant" toast naming the fish</li>
 *   <li>a bulk discovery (e.g. opening a stash) -> one coalesced "+N" toast</li>
 * </ul>
 *
 * Uses vanilla {@link SystemToast} (no custom rendering). A later slice can swap in a
 * polished advancement-style toast once it can be verified visually.
 */
public final class ToastQueue {

	private ToastQueue() {}

	public static void onCollected(MinecraftClient client, List<Integer> added, int afterNamed, int afterTotal) {
		if (added.isEmpty()) {
			return;
		}

		int addedNamed = (int) added.stream().filter(NamedVarieties::isNamed).count();

		Text title;
		Text description;
		if (afterTotal >= TropicalFishVariant.TOTAL) {
			title = Text.translatable("bucketlist.toast.challenge");
			description = Text.literal(afterTotal + " / " + TropicalFishVariant.TOTAL);
		} else if (afterNamed >= NamedVarieties.total() && addedNamed > 0) {
			title = Text.translatable("bucketlist.toast.named_complete");
			description = Text.literal(afterNamed + " / " + NamedVarieties.total() + " named");
		} else if (added.size() == 1) {
			title = Text.translatable("bucketlist.toast.new_variant");
			description = Text.literal(TropicalFishVariant.unpack(added.get(0)).describe());
		} else {
			title = Text.translatable("bucketlist.toast.bulk", added.size());
			description = Text.literal(afterTotal + " / " + TropicalFishVariant.TOTAL);
		}

		SystemToast.show(client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, title, description);
	}
}
