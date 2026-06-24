package online.blizzen.bucketlist.criteria;

import online.blizzen.bucketlist.detect.ScanResult;
import online.blizzen.bucketlist.engine.AdvancementNode;

/**
 * Evaluates whether an advancement node's criterion is satisfied by current client state.
 * Pluggable per vanilla trigger id; v0.1 ships only {@code minecraft:inventory_changed}.
 * Unsupported triggers simply never fire (their nodes stay un-collected) so foreign packs
 * still load without crashing.
 */
public interface CriterionEvaluator {

	/** The vanilla criterion trigger id this evaluator handles. */
	String trigger();

	boolean isSatisfied(AdvancementNode node, ScanResult scan);
}
