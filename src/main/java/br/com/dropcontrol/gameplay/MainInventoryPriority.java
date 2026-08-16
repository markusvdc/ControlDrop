package br.com.dropcontrol.gameplay;

import java.util.function.Supplier;

public final class MainInventoryPriority {
	private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

	private MainInventoryPriority() {
	}

	public static boolean isActive() {
		return ACTIVE.get();
	}

	public static <T> T whileActive(Supplier<T> action) {
		boolean previous = ACTIVE.get();
		ACTIVE.set(true);
		try {
			return action.get();
		} finally {
			ACTIVE.set(previous);
		}
	}
}
