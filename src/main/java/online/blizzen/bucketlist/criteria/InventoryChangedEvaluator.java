package online.blizzen.bucketlist.criteria;

import online.blizzen.bucketlist.detect.ScanResult;
import online.blizzen.bucketlist.engine.AdvancementNode;

/**
 * Handles {@code minecraft:inventory_changed}.
 *
 * <p>Per the design (pragmatic evaluator + additive hint): a fish leaf carries the exact
 * variant via the {@code gu:bucket_variant} hint, because the precise variant match is
 * awkward to express as a pure vanilla item predicate. Generic item-id / count predicates
 * for non-fish packs are a later addition.
 */
public final class InventoryChangedEvaluator implements CriterionEvaluator {

	@Override
	public String trigger() {
		return "minecraft:inventory_changed";
	}

	@Override
	public boolean isSatisfied(AdvancementNode node, ScanResult scan) {
		Integer variant = node.bucketVariant();
		return variant != null && scan.variants().contains(variant);
		// TODO: evaluate generic item-id + count item predicates for foreign packs.
	}
}
