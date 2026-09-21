package br.com.dropcontrol.gameplay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Builds a compact, stable ordering without changing the input stacks. */
public final class StackSortPlan {
	private StackSortPlan() {
	}

	public interface Stacks<T> {
		int count(T stack);
		int limit(T stack);
		boolean same(T left, T right);
		T copy(T stack, int count);
		T empty();
	}

	public static <T> List<T> create(List<T> input, Stacks<T> stacks, Comparator<T> order) {
		List<T> packed = new ArrayList<>();
		for (T original : input) {
			int remaining = stacks.count(original);
			if (remaining == 0) {
				continue;
			}
			int limit = stacks.limit(original);
			if (remaining < 0 || limit <= 0 || remaining > limit) {
				throw new IllegalArgumentException("Invalid stack size");
			}
			for (int index = 0; index < packed.size() && remaining > 0; index++) {
				T existing = packed.get(index);
				if (stacks.same(existing, original)) {
					int moved = Math.min(remaining, stacks.limit(existing) - stacks.count(existing));
					packed.set(index, stacks.copy(existing, stacks.count(existing) + moved));
					remaining -= moved;
				}
			}
			if (remaining > 0) {
				packed.add(stacks.copy(original, remaining));
			}
		}
		// Stable sorting preserves the relative order of distinct variants with equal names.
		packed.sort(order);
		while (packed.size() < input.size()) {
			packed.add(stacks.empty());
		}
		return List.copyOf(packed);
	}
}
