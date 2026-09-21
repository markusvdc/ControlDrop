package br.com.dropcontrol.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntBiFunction;

public final class MatchingTransferPlan {
	private MatchingTransferPlan() {
	}

	public record Result<T>(List<T> inventory, List<T> storage) {
	}

	public static <T> Result<T> create(List<T> inventory, List<T> storage,
		StackSortPlan.Stacks<T> stacks, ToIntBiFunction<Integer, T> capacity) {
		List<T> remaining = new ArrayList<>(inventory);
		List<T> deposited = new ArrayList<>(storage);
		for (int source = 0; source < inventory.size(); source++) {
			T original = inventory.get(source);
			int count = stacks.count(original);
			if (count <= 0 || storage.stream().noneMatch(existing ->
				stacks.count(existing) > 0 && stacks.same(existing, original))) {
				continue;
			}
			// Existing stacks first, then empty slots. Eligibility uses the original storage.
			for (int pass = 0; pass < 2 && count > 0; pass++) {
				for (int target = 0; target < deposited.size() && count > 0; target++) {
					T current = deposited.get(target);
					int present = stacks.count(current);
					if (pass == 0 ? present == 0 || !stacks.same(current, original) : present != 0) {
						continue;
					}
					int limit = Math.min(stacks.limit(original), capacity.applyAsInt(target, original));
					int moved = Math.min(count, Math.max(0, limit - present));
					if (moved > 0) {
						deposited.set(target, stacks.copy(present == 0 ? original : current, present + moved));
						count -= moved;
					}
				}
			}
			remaining.set(source, count == 0 ? stacks.empty() : stacks.copy(original, count));
		}
		return new Result<>(List.copyOf(remaining), List.copyOf(deposited));
	}
}
