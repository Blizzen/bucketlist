package online.blizzen.bucketlist.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Custom advancement-style screen: dirt background, framed nodes (task/goal/challenge),
 * connecting lines, and procedurally tinted live tropical-fish icons. One tab per pack
 * root (a Global overview tab + one tab per fish type), mirroring the vanilla advancement
 * screen but driven entirely by client-side data.
 *
 * <p>Reminder for v0.1 rendering: any entity/overlay drawn on top of GUI content needs the
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

		// TODO(v0.1): tab strip, dirt background, tree walk with connecting lines, framed
		// nodes, procedural live-fish icons, and the X / 3072 + X / 22 progress header.
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Scaffold — engine wiring lands in v0.1"),
				this.width / 2, 40, 0xA0A0A0);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
