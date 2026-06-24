package online.blizzen.bucketlist.detect;

import java.util.Set;

/** Packed tropical-fish variant ints the player currently has access to. */
public record ScanResult(Set<Integer> variants) {
	public static final ScanResult EMPTY = new ScanResult(Set.of());
}
