package online.blizzen.bucketlist.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import online.blizzen.bucketlist.BucketlistClient;
import online.blizzen.bucketlist.fish.FishCatalog;
import online.blizzen.bucketlist.store.CollectionStore;
import online.blizzen.bucketlist.variant.NamedVarieties;
import online.blizzen.bucketlist.variant.TropicalFishVariant;

import java.util.Set;

/**
 * Collection screen: a Pokédex-style grid per fish type plus a live procedural fish preview.
 *
 * <p>Tabs select one of the 12 types; the grid is that type's 16x16 base/pattern-color
 * matrix (collected = full color, uncollected = dimmed). Hovering a cell renders the actual
 * tropical fish for that variant via {@link InventoryScreen#drawEntity} (which brackets its
 * own {@code context.draw()} flushes — the overlay-flush requirement is handled by vanilla).
 */
public class BucketlistScreen extends Screen {

	// ARGB swatches for the 16 dye colors (white=0 .. black=15).
	private static final int[] DYE_RGB = {
			0xFFF9FFFE, 0xFFF9801D, 0xFFC74EBD, 0xFF3AB3DA, 0xFFFED83D, 0xFF80C71F, 0xFFF38BAA, 0xFF474F52,
			0xFF9D9D97, 0xFF169C9C, 0xFF8932B8, 0xFF3C44AA, 0xFF835432, 0xFF5E7C16, 0xFFB02E26, 0xFF1D1D21
	};

	private static final int CELL = 12;
	private static final int GRID = TropicalFishVariant.COLORS * CELL; // 192

	private int selectedType = -1; // -1 == Overview
	private int hoveredVariant = -1;
	private boolean missingOnly;

	private static final int TOGGLE_W = 92;
	private static final int TOGGLE_H = 14;
	private int toggleX;
	private int toggleY;

	// Reused preview entity; re-variant it only when the shown variant changes.
	private TropicalFishEntity previewFish;
	private int previewVariant = Integer.MIN_VALUE;

	private final int[] tabX = new int[TropicalFishVariant.TYPES];
	private final int[] tabY = new int[TropicalFishVariant.TYPES];
	private static final int TAB_W = 56;
	private static final int TAB_H = 18;
	private static final int TYPE_MAX = TropicalFishVariant.COLORS * TropicalFishVariant.COLORS; // 256

	// Overview ("Global") view: selectedType < 0 means overview.
	private final int[] ovCellX = new int[TropicalFishVariant.TYPES];
	private final int[] ovCellY = new int[TropicalFishVariant.TYPES];
	private static final int OV_CELL_W = 86;
	private static final int OV_CELL_H = 78;

	// Distinctive representative (baseColor, patternColor) per type for the overview icons —
	// each type's iconic named-variety colours, indexed by type (Kob..Clayfish).
	private static final int[] REP_BASE = {1, 11, 7, 9, 5, 6, 0, 1, 0, 10, 14, 0};
	private static final int[] REP_PATTERN = {0, 7, 14, 6, 3, 3, 4, 7, 7, 4, 0, 14};
	private static final int OV_BTN_W = 78;
	private static final int OV_BTN_H = 14;
	private int overviewBtnX;
	private int overviewBtnY;

	public BucketlistScreen() {
		super(Text.translatable("bucketlist.screen.title"));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		CollectionStore store = BucketlistClient.store();
		Set<Integer> collected = (store != null && store.isOpen()) ? store.collectedVariants() : Set.of();

		int cx = this.width / 2;

		// Header.
		int named = 0;
		for (int v : collected) {
			if (NamedVarieties.isNamed(v)) {
				named++;
			}
		}
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 8, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.translatable("bucketlist.progress.global", collected.size(), FishCatalog.totalVariants()), cx, 20, 0x55FFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.translatable("bucketlist.progress.named", named, FishCatalog.namedTotal()), cx, 31, 0xFFD55F);

		// "Show: All / Missing" toggle (top-right).
		toggleX = this.width - TOGGLE_W - 6;
		toggleY = 6;
		boolean toggleHover = mouseX >= toggleX && mouseX < toggleX + TOGGLE_W && mouseY >= toggleY && mouseY < toggleY + TOGGLE_H;
		context.fill(toggleX, toggleY, toggleX + TOGGLE_W, toggleY + TOGGLE_H, toggleHover ? 0xFF333333 : 0xFF222222);
		context.drawBorder(toggleX, toggleY, TOGGLE_W, TOGGLE_H, missingOnly ? 0xFF55AAFF : 0xFF000000);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Show: " + (missingOnly ? "Missing" : "All")), toggleX + TOGGLE_W / 2, toggleY + 3, 0xFFFFFF);

		// "Overview" button (top-left); selectedType < 0 == overview.
		overviewBtnX = 6;
		overviewBtnY = 6;
		boolean ovSel = selectedType < 0;
		boolean ovHover = mouseX >= overviewBtnX && mouseX < overviewBtnX + OV_BTN_W && mouseY >= overviewBtnY && mouseY < overviewBtnY + OV_BTN_H;
		context.fill(overviewBtnX, overviewBtnY, overviewBtnX + OV_BTN_W, overviewBtnY + OV_BTN_H, ovSel ? 0xFF4A4A4A : (ovHover ? 0xFF333333 : 0xFF222222));
		context.drawBorder(overviewBtnX, overviewBtnY, OV_BTN_W, OV_BTN_H, ovSel ? 0xFFFFFFFF : 0xFF000000);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Overview"), overviewBtnX + OV_BTN_W / 2, overviewBtnY + 3, 0xFFFFFF);

		int[] perType = new int[TropicalFishVariant.TYPES];
		for (int v : collected) {
			int t = TropicalFishVariant.unpack(v).typeIndex();
			if (t >= 0 && t < perType.length) {
				perType[t]++;
			}
		}
		drawTabs(context, mouseX, mouseY, perType);

		if (selectedType < 0) {
			drawOverview(context, mouseX, mouseY, perType);
			return;
		}

		int gridX = cx - 150;
		int gridY = 96;
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal(TropicalFishVariant.TYPE_NAMES[selectedType] + " — " + perType[selectedType] + " / " + TYPE_MAX),
				cx, gridY - 12, 0xFFFFFF);

		hoveredVariant = -1;
		drawGrid(context, mouseX, mouseY, collected, gridX, gridY);
		drawPreview(context, mouseX, mouseY, collected, gridX, gridY);
	}

	private void drawOverview(DrawContext context, int mouseX, int mouseY, int[] perType) {
		int cols = 4;
		int gap = 6;
		int totalW = cols * OV_CELL_W + (cols - 1) * gap;
		int startX = this.width / 2 - totalW / 2;
		int startY = 98;

		for (int i = 0; i < TropicalFishVariant.TYPES; i++) {
			int col = i % cols;
			int row = i / cols;
			int x = startX + col * (OV_CELL_W + gap);
			int y = startY + row * (OV_CELL_H + gap);
			ovCellX[i] = x;
			ovCellY[i] = y;

			boolean hover = mouseX >= x && mouseX < x + OV_CELL_W && mouseY >= y && mouseY < y + OV_CELL_H;
			boolean complete = perType[i] >= TYPE_MAX;
			int midX = x + OV_CELL_W / 2;
			context.fill(x, y, x + OV_CELL_W, y + OV_CELL_H, hover ? 0xFF2A2A2A : 0xFF1A1A1A);
			context.drawBorder(x, y, OV_CELL_W, OV_CELL_H, complete ? 0xFF55FF55 : (hover ? 0xFFFFFFFF : 0xFF000000));

			// Name + count, centered at the top.
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(TropicalFishVariant.TYPE_NAMES[i]),
					midX, y + 4, complete ? 0x55FF55 : 0xFFFFFF);
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(perType[i] + " / " + TYPE_MAX),
					midX, y + 15, 0xC0C0C0);

			// Big centered representative fish (the type's iconic colours).
			int size = i / TropicalFishVariant.PATTERNS;
			int pattern = i % TropicalFishVariant.PATTERNS;
			ensurePreviewFish(new TropicalFishVariant(size, pattern, REP_BASE[i], REP_PATTERN[i]).pack());
			if (previewFish != null) {
				int fy1 = y + 27;
				int fy2 = y + OV_CELL_H - 9;
				InventoryScreen.drawEntity(context, x + 8, fy1, x + OV_CELL_W - 8, fy2, 70, 0.0F, mouseX, mouseY, previewFish);
			}

			int barX = x + 6;
			int barY = y + OV_CELL_H - 6;
			int barW = OV_CELL_W - 12;
			context.fill(barX, barY, barX + barW, barY + 3, 0xFF101010);
			int fill = Math.round(barW * (perType[i] / (float) TYPE_MAX));
			if (fill > 0) {
				context.fill(barX, barY, barX + fill, barY + 3, complete ? 0xFF55FF55 : 0xFF55AAFF);
			}
		}
	}

	private void drawTabs(DrawContext context, int mouseX, int mouseY, int[] perType) {
		int perRow = 6;
		int rowW = perRow * (TAB_W + 2) - 2;
		int startX = this.width / 2 - rowW / 2;
		for (int i = 0; i < TropicalFishVariant.TYPES; i++) {
			int col = i % perRow;
			int row = i / perRow;
			int x = startX + col * (TAB_W + 2);
			int y = 44 + row * (TAB_H + 2);
			tabX[i] = x;
			tabY[i] = y;

			boolean sel = i == selectedType;
			boolean hover = mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H;
			boolean complete = perType[i] >= TYPE_MAX;
			int bg = sel ? 0xFF4A4A4A : (hover ? 0xFF333333 : 0xFF222222);
			context.fill(x, y, x + TAB_W, y + TAB_H, bg);
			context.drawBorder(x, y, TAB_W, TAB_H, sel ? 0xFFFFFFFF : 0xFF000000);
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(TropicalFishVariant.TYPE_NAMES[i]),
					x + TAB_W / 2, y + 3, sel ? 0xFFFFFF : (complete ? 0x55FF55 : 0xC0C0C0));

			// Per-type progress bar.
			int barX = x + 3;
			int barY = y + TAB_H - 5;
			int barW = TAB_W - 6;
			context.fill(barX, barY, barX + barW, barY + 3, 0xFF101010);
			int fill = Math.round(barW * (perType[i] / (float) TYPE_MAX));
			if (fill > 0) {
				context.fill(barX, barY, barX + fill, barY + 3, complete ? 0xFF55FF55 : 0xFF55AAFF);
			}
		}
	}

	private void drawGrid(DrawContext context, int mouseX, int mouseY, Set<Integer> collected, int gridX, int gridY) {
		int size = selectedType / TropicalFishVariant.PATTERNS;
		int pattern = selectedType % TropicalFishVariant.PATTERNS;

		for (int base = 0; base < TropicalFishVariant.COLORS; base++) {
			for (int pc = 0; pc < TropicalFishVariant.COLORS; pc++) {
				int x = gridX + pc * CELL;
				int y = gridY + base * CELL;
				int variant = new TropicalFishVariant(size, pattern, base, pc).pack();
				boolean got = collected.contains(variant);

				if (missingOnly && got) {
					// In the missing-only view, collected variants become empty slots.
					context.fill(x, y, x + CELL, y + CELL, 0xFF181818);
				} else {
					context.fill(x, y, x + CELL, y + CELL, DYE_RGB[base]);
					context.fill(x + 3, y + 3, x + CELL - 3, y + CELL - 3, DYE_RGB[pc]);
					if (!missingOnly && !got) {
						context.fill(x, y, x + CELL, y + CELL, 0xC8101010); // dim uncollected in the full view
					}
				}

				boolean hover = mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL;
				if (hover) {
					hoveredVariant = variant;
					context.drawBorder(x, y, CELL, CELL, 0xFFFFFFFF);
				}
			}
		}
	}

	private void drawPreview(DrawContext context, int mouseX, int mouseY, Set<Integer> collected, int gridX, int gridY) {
		int size = selectedType / TropicalFishVariant.PATTERNS;
		int pattern = selectedType % TropicalFishVariant.PATTERNS;
		int shown = hoveredVariant >= 0 ? hoveredVariant : new TropicalFishVariant(size, pattern, 0, 0).pack();

		int boxX = gridX + GRID + 16;
		int boxY = gridY;
		int boxW = 96;
		int boxH = 120;
		context.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xC0000000);
		context.drawBorder(boxX, boxY, boxW, boxH, 0xFF555555);

		ensurePreviewFish(shown);
		if (previewFish != null) {
			InventoryScreen.drawEntity(context, boxX + 6, boxY + 6, boxX + boxW - 6, boxY + boxH - 22, 80, 0.0F, mouseX, mouseY, previewFish);
		}

		// Hovered/selected variant label + status, centered under the grid.
		TropicalFishVariant tv = TropicalFishVariant.unpack(shown);
		boolean got = collected.contains(shown);
		String name = tv.describe() + (NamedVarieties.isNamed(shown) ? "  (named)" : "");
		int labelY = gridY + GRID + 8;
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(name), this.width / 2, labelY, got ? 0x55FF55 : 0xC0C0C0);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal(got ? "Collected" : "Not collected"), this.width / 2, labelY + 11, got ? 0x55FF55 : 0x808080);
	}

	private void ensurePreviewFish(int variant) {
		if (this.client == null || this.client.world == null) {
			previewFish = null;
			return;
		}
		if (previewFish == null) {
			// Report as in-water so the renderer skips its "flopping on land" 90deg Z-rotation
			// (TropicalFishEntityRenderer.setupTransforms) and shows the upright swimming pose.
			previewFish = new TropicalFishEntity(EntityType.TROPICAL_FISH, this.client.world) {
				@Override
				public boolean isTouchingWater() {
					return true;
				}
			};
		}
		if (previewVariant != variant) {
			NbtCompound nbt = new NbtCompound();
			nbt.putInt(TropicalFishEntity.BUCKET_VARIANT_TAG_KEY, variant);
			previewFish.copyDataFromNbt(nbt);
			previewVariant = variant;
		}
		// Drive the swim animation from world time (the entity isn't ticked in the GUI).
		previewFish.age = (int) this.client.world.getTime();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= toggleX && mouseX < toggleX + TOGGLE_W && mouseY >= toggleY && mouseY < toggleY + TOGGLE_H) {
			missingOnly = !missingOnly;
			return true;
		}
		if (mouseX >= overviewBtnX && mouseX < overviewBtnX + OV_BTN_W && mouseY >= overviewBtnY && mouseY < overviewBtnY + OV_BTN_H) {
			selectedType = -1;
			return true;
		}
		if (selectedType < 0) {
			for (int i = 0; i < TropicalFishVariant.TYPES; i++) {
				if (mouseX >= ovCellX[i] && mouseX < ovCellX[i] + OV_CELL_W && mouseY >= ovCellY[i] && mouseY < ovCellY[i] + OV_CELL_H) {
					selectedType = i;
					return true;
				}
			}
		}
		for (int i = 0; i < TropicalFishVariant.TYPES; i++) {
			if (mouseX >= tabX[i] && mouseX < tabX[i] + TAB_W && mouseY >= tabY[i] && mouseY < tabY[i] + TAB_H) {
				selectedType = i;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
