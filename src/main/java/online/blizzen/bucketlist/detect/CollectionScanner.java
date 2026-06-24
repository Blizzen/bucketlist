package online.blizzen.bucketlist.detect;

import net.minecraft.client.MinecraftClient;

/**
 * Deep-scan detector. Each tick / inventory change it gathers the tropical-fish variants
 * the player currently has access to:
 *
 * <ul>
 *   <li>player inventory (hotbar + main + offhand)</li>
 *   <li>the {@code container} component of any shulker-box item being held
 *       (readable without opening the box)</li>
 *   <li>the contents of any open container screen (ender chest, stash chests, ...)</li>
 * </ul>
 *
 * <p>"Collected" = the variant has appeared in any of these sources, matching the
 * honest framing chosen in the design (not strictly "personally caught").
 */
public final class CollectionScanner {

	// TODO(v0.1): read bucket_entity_data "BucketVariantTag" from every
	// "Bucket of Tropical Fish" stack found in the sources above; collect packed ints.
	public ScanResult scan(MinecraftClient client) {
		return ScanResult.EMPTY;
	}
}
