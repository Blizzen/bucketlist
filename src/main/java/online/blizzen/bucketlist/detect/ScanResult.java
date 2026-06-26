package online.blizzen.bucketlist.detect;

import java.util.Set;

/**
 * Result of one deep scan.
 *
 * @param variants     distinct packed tropical-fish variant ints currently accessible
 * @param sourceStacks how many tropical-fish-bucket stacks were seen pre-dedup (a diagnostic
 *                     signal: if one catch yields {@code sourceStacks == 1} the count can
 *                     only rise by one)
 */
public record ScanResult(Set<Integer> variants, int sourceStacks) {
	public static final ScanResult EMPTY = new ScanResult(Set.of(), 0);
}
