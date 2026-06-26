package online.blizzen.bucketlist.detect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.HashSet;
import java.util.Set;

/**
 * Deep-scan detector. Gathers the tropical-fish variants the player currently has access to:
 *
 * <ul>
 *   <li>player inventory (main + offhand) — their real possessions</li>
 *   <li>the contents of a genuinely open container screen (ender chest, stash chests, ...) —
 *       but never the <b>creative inventory palette</b>, which shows items the player does
 *       not actually own</li>
 *   <li>the {@code container} component of any shulker-box item found in the above
 *       (readable without opening the box)</li>
 * </ul>
 */
public final class CollectionScanner {

	public ScanResult scan(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null) {
			return ScanResult.EMPTY;
		}

		Set<Integer> variants = new HashSet<>();
		int[] bucketStacks = {0};

		// Real possessions.
		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.main) {
			collect(stack, variants, bucketStacks, true);
		}
		for (ItemStack stack : inv.offHand) {
			collect(stack, variants, bucketStacks, true);
		}

		// A genuinely open container (chest / ender chest / barrel / ...). Excludes the
		// creative inventory, whose slots are a palette of items the player does not own.
		Screen screen = client.currentScreen;
		if (screen instanceof HandledScreen<?> handledScreen && !(screen instanceof CreativeInventoryScreen)) {
			for (Slot slot : handledScreen.getScreenHandler().slots) {
				collect(slot.getStack(), variants, bucketStacks, true);
			}
		}

		return new ScanResult(variants, bucketStacks[0]);
	}

	/** Records a variant if {@code stack} is a tropical-fish bucket; recurses one level into
	 *  shulker-box container contents when {@code recurse} is true. */
	private void collect(ItemStack stack, Set<Integer> out, int[] bucketStacks, boolean recurse) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		if (stack.isOf(Items.TROPICAL_FISH_BUCKET)) {
			NbtComponent data = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
			if (data != null) {
				NbtCompound nbt = data.copyNbt();
				if (nbt.contains(TropicalFishEntity.BUCKET_VARIANT_TAG_KEY)) {
					out.add(nbt.getInt(TropicalFishEntity.BUCKET_VARIANT_TAG_KEY));
					bucketStacks[0]++;
				}
			}
		}

		if (recurse) {
			ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
			if (container != null) {
				container.streamNonEmpty().forEach(inner -> collect(inner, out, bucketStacks, false));
			}
		}
	}
}
