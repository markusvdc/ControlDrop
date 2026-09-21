package br.com.dropcontrol.gameplay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Standalone deterministic checks; no Minecraft client or external test framework. */
public final class StackSortPlanTest {
	private record Stack(String item, String components, int count, int limit) {
	}

	private static final Stack EMPTY = new Stack("", "", 0, 64);
	private static final StackSortPlan.Stacks<Stack> STACKS = new StackSortPlan.Stacks<>() {
		public int count(Stack stack) { return stack.count(); }
		public int limit(Stack stack) { return stack.limit(); }
		public boolean same(Stack left, Stack right) {
			return left.item().equals(right.item()) && left.components().equals(right.components());
		}
		public Stack copy(Stack stack, int count) {
			return new Stack(stack.item(), stack.components(), count, stack.limit());
		}
		public Stack empty() { return EMPTY; }
	};
	private static final Comparator<Stack> ORDER = Comparator.comparing(Stack::item);

	public static void main(String[] args) {
		MatchingTransferPlanTest.verify();
		List<Stack> mixed = List.of(new Stack("stone", "", 40, 64),
			new Stack("apple", "", 2, 64), new Stack("stone", "", 30, 64),
			new Stack("stone", "named", 1, 64), EMPTY);
		List<Stack> expected = List.of(new Stack("apple", "", 2, 64),
			new Stack("stone", "", 64, 64), new Stack("stone", "", 6, 64),
			new Stack("stone", "named", 1, 64), EMPTY);
		check(StackSortPlan.create(mixed, STACKS, ORDER).equals(expected), "Merge and alphabetical order");
		check(StackSortPlan.create(List.of(EMPTY, EMPTY), STACKS, ORDER).equals(List.of(EMPTY, EMPTY)), "Empty inventory");
		check(StackSortPlan.create(List.<Stack>of(), STACKS, ORDER).isEmpty(), "Empty region");
		try {
			StackSortPlan.create(List.of(new Stack("stone", "", 65, 64)), STACKS, ORDER);
			throw new AssertionError("Overfull input must be rejected");
		} catch (IllegalArgumentException expectedException) {
			// Refuse invalid input instead of silently discarding any items.
		}
		Random random = new Random(739_021);
		for (int trial = 0; trial < 10_000; trial++) {
			int size = new int[] {27, 54, 0, 1}[trial % 4];
			List<Stack> input = new ArrayList<>();
			for (int slot = 0; slot < size; slot++) {
				int kind = random.nextInt(8);
				int limit = new int[] {1, 16, 64}[kind % 3];
				input.add(random.nextInt(5) == 0 ? EMPTY : new Stack("item" + kind,
					"components" + random.nextInt(4), random.nextInt(limit) + 1, limit));
			}
			List<Stack> snapshot = List.copyOf(input);
			List<Stack> output = StackSortPlan.create(input, STACKS, ORDER);
			check(input.equals(snapshot), "Input mutated");
			check(output.size() == input.size(), "Slot count changed");
			check(totals(input).equals(totals(output)), "Item or component counts changed");
			check(output.equals(StackSortPlan.create(output, STACKS, ORDER)), "Not idempotent");
			boolean emptySeen = false;
			Stack previous = null;
			for (Stack stack : output) {
				check(stack.count() <= stack.limit(), "Stack overflow");
				if (stack.count() == 0) {
					emptySeen = true;
				} else {
					check(!emptySeen, "Gap between stacks");
					check(previous == null || ORDER.compare(previous, stack) <= 0, "Not alphabetical");
					previous = stack;
				}
			}
		}
		System.out.println("Stack sorting: explicit cases and 10,000 randomized inventories passed.");
	}

	private static Map<String, Integer> totals(List<Stack> stacks) {
		Map<String, Integer> totals = new HashMap<>();
		for (Stack stack : stacks) {
			if (stack.count() > 0) {
				totals.merge(stack.item() + ":" + stack.components(), stack.count(), Integer::sum);
			}
		}
		return totals;
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
