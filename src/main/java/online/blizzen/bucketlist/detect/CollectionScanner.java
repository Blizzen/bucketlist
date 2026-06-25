package online.blizzen.bucketlist.detect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.HashSet;
import java.util.Set;

/**
 * Deep-scan detector. Gathers the tropical-fish variants the player currently has access to:
 *
 * <ul>
 *   <li>player inventory (main + offhand)</li>
 *   <li>the contents of any open container screen (ender chest, stash chests, ...) via the
 *       current {@link ScreenHandler}'s slots</li>
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

		PlayerInventory inv = player.getInventory();
		for (ItemStack stack : inv.main) {
			collect(stack, variants, true);
		}
		for (ItemStack stack : inv.offHand) {
			collect(stack, variants, true);
		}

		// The current handler covers the player inventory and any open container.
		ScreenHandler handler = player.currentScreenHandler;
		if (handler != null) {
			for (Slot slot : handler.slots) {
				collect(slot.getStack(), variants, true);
			}
		}

		return new ScanResult(variants);
	}

	/** Records a variant if {@code stack} is a tropical-fish bucket; recurses one level into
	 *  shulker-box container contents when {@code recurse} is true. */
	private void collect(ItemStack stack, Set<Integer> out, boolean recurse) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		if (stack.isOf(Items.TROPICAL_FISH_BUCKET)) {
			NbtComponent data = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
			if (data != null) {
				NbtCompound nbt = data.copyNbt();
				if (nbt.contains(TropicalFishEntity.BUCKET_VARIANT_TAG_KEY)) {
					out.add(nbt.getInt(TropicalFishEntity.BUCKET_VARIANT_TAG_KEY));
				}
			}
		}

		if (recurse) {
			ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
			if (container != null) {
				container.streamNonEmpty().forEach(inner -> collect(inner, out, false));
			}
		}
	}
}
