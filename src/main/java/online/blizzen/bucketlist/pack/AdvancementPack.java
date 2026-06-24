package online.blizzen.bucketlist.pack;

import online.blizzen.bucketlist.engine.AdvancementNode;

import java.util.List;
import java.util.Map;

/**
 * A loaded advancement pack (one datapack namespace). Each tab root (a node with no
 * parent) becomes a tab in the UI; the bundled fish pack ships a {@code global} tab plus
 * one tab per fish type.
 */
public record AdvancementPack(String namespace, Map<String, AdvancementNode> nodes) {

	/** Tab roots, in load order. */
	public List<AdvancementNode> roots() {
		return nodes.values().stream().filter(AdvancementNode::isRoot).toList();
	}
}
