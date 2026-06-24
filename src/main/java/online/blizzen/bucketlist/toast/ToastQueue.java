package online.blizzen.bucketlist.toast;

/**
 * Tiered + coalesced toast feedback.
 *
 * <ul>
 *   <li>A single new variant during normal play -> one "new variant" toast.</li>
 *   <li>A bulk stash scan landing many at once -> one coalesced "+N new variants" toast,
 *       plus tier-completion toasts for any row (16/16), type (256/256), named variety,
 *       or the global challenge that completed.</li>
 * </ul>
 *
 * This prevents the 200-toast firehose the deep-scan would otherwise cause.
 */
public final class ToastQueue {

	// TODO(v0.1): accept newly-collected variants for a frame; decide single vs coalesced
	// vs tier toasts; submit to MinecraftClient.getInstance().getToastManager().
}
