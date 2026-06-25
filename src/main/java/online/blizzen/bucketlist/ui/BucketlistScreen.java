package online.blizzen.bucketlist.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import online.blizzen.bucketlist.store.CollectionStore;
import online.blizzen.bucketlist.variant.NamedVarieties;
import online.blizzen.bucketlist.variant.TropicalFishVariant;
import online.blizzen.bucketlist.BucketlistClient;

import java.util.Set;

/**
 * Collection screen. The full version clones the vanilla advancement screen (dirt bg,
 * task/goal/challenge frames, connecting lines, procedural live-fish icons, one tab per
 * pack root). This interim version shows the live per-server progress counts so the
 * detection + persistence slice is testable; the tree render is the next slice.
 *
 * <p>Reminder for the tree render: entity/overlay draws on top of GUI content need the
 * {@code context.draw()} + {@code entityVertexConsumers.draw()} flush BEFORE and AFTER —
 * z-test alone is unreliable across the 1.21.4 GUI render layers.
 */
public class BucketlistScreen extends Screen {

	public BucketlistScreen() {
		super(Text.translatable("bucketlist.screen.title"));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		CollectionStore store = BucketlistClient.store();
		int collected = 0;
		int named = 0;
		if (store != null && store.isOpen()) {
			Set<Integer> variants = store.collectedVariants();
			collected = variants.size();
			for (int v : variants) {
				if (NamedVarieties.isNamed(v)) {
					named++;
				}
			}
		}

		int cx = this.width / 2;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 24, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.translatable("bucketlist.progress.global", collected, TropicalFishVariant.TOTAL),
				cx, 48, 0x55FFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.translatable("bucketlist.progress.named", named, NamedVarieties.total()),
				cx, 62, 0xFFD55F);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Tree view with fish icons lands next slice"),
				cx, 90, 0x808080);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
