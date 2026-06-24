package online.blizzen.bucketlist.engine;

import java.util.List;

/**
 * One node of a loaded advancement tree, decoded from datapack JSON.
 *
 * <p>{@code bucketVariant} is the additive {@code gu:bucket_variant} hint (the packed
 * tropical-fish variant int) and is {@code null} for non-fish nodes.
 */
public record AdvancementNode(
		String id,             // e.g. "bucketlist:flopper/red/pattern_white"
		String parentId,       // null for tab roots
		String title,
		String description,
		String frame,          // "task" | "goal" | "challenge"
		boolean showToast,
		Integer bucketVariant, // gu:bucket_variant hint; null if not a fish leaf
		List<String> children
) {
	public boolean isRoot() {
		return parentId == null;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}
}
