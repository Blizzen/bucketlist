package online.blizzen.bucketlist.detect;

import online.blizzen.bucketlist.Bucketlist;
import online.blizzen.bucketlist.variant.NamedVarieties;
import online.blizzen.bucketlist.variant.TropicalFishVariant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Structured diagnostics for the over-count investigation. Appends one JSONL line per
 * new-variant scan event to {@code config/bucketlist/diagnostic/scan.jsonl} so the exact
 * variants, their decoded (size/pattern/base/patternColor), and how many bucket stacks were
 * seen can be inspected after the fact — rather than guessing.
 *
 * <p>{@code bucketStacks} vs {@code distinctFound} is the key signal: bucketing one fish can
 * only produce one stack, so {@code distinctFound > bucketStacks} (or a single catch adding
 * several) points at a source feeding extra buckets.
 */
public final class Diagnostics {

	private Diagnostics() {}

	public static void writeScanEvent(Path configDir, long now, String serverKey,
			ScanResult scan, List<Integer> added, int totalAfter) {
		try {
			Path dir = configDir.resolve(Bucketlist.MOD_ID).resolve("diagnostic");
			Files.createDirectories(dir);

			StringBuilder sb = new StringBuilder();
			sb.append("{\"ts\":").append(now);
			sb.append(",\"server\":\"").append(esc(serverKey)).append('"');
			sb.append(",\"distinctFound\":").append(scan.variants().size());
			sb.append(",\"bucketStacks\":").append(scan.sourceStacks());
			sb.append(",\"newlyAdded\":").append(added.size());
			sb.append(",\"totalAfter\":").append(totalAfter);
			sb.append(",\"added\":[");
			for (int i = 0; i < added.size(); i++) {
				int v = added.get(i);
				TropicalFishVariant tv = TropicalFishVariant.unpack(v);
				if (i > 0) {
					sb.append(',');
				}
				sb.append("{\"variant\":").append(v)
						.append(",\"size\":").append(tv.size())
						.append(",\"pattern\":").append(tv.pattern())
						.append(",\"base\":").append(tv.baseColor())
						.append(",\"patternColor\":").append(tv.patternColor())
						.append(",\"named\":").append(NamedVarieties.isNamed(v))
						.append('}');
			}
			sb.append("]}\n");

			Files.writeString(dir.resolve("scan.jsonl"), sb.toString(),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception e) {
			Bucketlist.LOGGER.warn("Bucketlist: diagnostic write failed", e);
		}
	}

	private static String esc(String s) {
		return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
