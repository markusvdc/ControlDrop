package br.com.dropcontrol.gameplay;

import java.util.List;

public final class MatchingTransferPlanTest {
	private record Stack(String type, String data, int count) {
	}

	private static final Stack EMPTY = new Stack("", "", 0);
	private static final StackSortPlan.Stacks<Stack> STACKS = new StackSortPlan.Stacks<>() {
		public int count(Stack stack) { return stack.count(); }
		public int limit(Stack stack) { return 64; }
		public boolean same(Stack left, Stack right) {
			return left.type().equals(right.type()) && left.data().equals(right.data());
		}
		public Stack copy(Stack stack, int count) { return new Stack(stack.type(), stack.data(), count); }
		public Stack empty() { return EMPTY; }
	};

	static void verify() {
		Stack carrots = new Stack("carrot", "", 40);
		Stack named = new Stack("carrot", "named", 4);
		Stack apples = new Stack("apple", "", 5);
		List<Stack> inventory = List.of(carrots, carrots, named, apples);
		List<Stack> storage = List.of(EMPTY, new Stack("carrot", "", 60), EMPTY);
		var result = MatchingTransferPlan.create(inventory, storage, STACKS, (index, stack) -> 64);
		check(result.inventory().equals(List.of(EMPTY, EMPTY, named, apples)), "Only matching items leave inventory");
		check(result.storage().equals(List.of(new Stack("carrot", "", 64),
			new Stack("carrot", "", 64), new Stack("carrot", "", 12))), "Merge before empty slots");
		check(inventory.get(0).count() == 40 && storage.get(1).count() == 60, "Inputs unchanged");
		var partial = MatchingTransferPlan.create(List.of(carrots), List.of(new Stack("carrot", "", 60)), STACKS,
			(index, stack) -> 64);
		check(partial.inventory().getFirst().count() == 36 && partial.storage().getFirst().count() == 64, "Remainder kept");
		var full = MatchingTransferPlan.create(List.of(carrots), List.of(new Stack("carrot", "", 64)), STACKS,
			(index, stack) -> 64);
		check(full.inventory().equals(List.of(carrots)), "Full chest changes nothing");
		var empty = MatchingTransferPlan.create(inventory, List.of(EMPTY, EMPTY), STACKS, (index, stack) -> 64);
		check(empty.inventory().equals(inventory) && empty.storage().equals(List.of(EMPTY, EMPTY)), "Empty chest changes nothing");
		var restricted = MatchingTransferPlan.create(List.of(carrots), storage, STACKS,
			(index, stack) -> index == 1 ? 62 : 0);
		check(restricted.inventory().getFirst().count() == 38 && restricted.storage().get(1).count() == 62,
			"Slot limits and rejected slots respected");
		check(MatchingTransferPlan.create(result.inventory(), result.storage(), STACKS, (index, stack) -> 64)
			.equals(result), "Repeated transfer stable");
		System.out.println("Matching transfer: eligibility, variants, capacity, remainders and repeat checks passed.");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
