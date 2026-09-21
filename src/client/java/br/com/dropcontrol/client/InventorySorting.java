package br.com.dropcontrol.client;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.StackSortPlan;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class InventorySorting {
	private static Pending pending;
	private static final StackSortPlan.Stacks<ItemStack> STACKS = new StackSortPlan.Stacks<>() {
		public int count(ItemStack stack) { return stack.getCount(); }
		public int limit(ItemStack stack) { return stack.getMaxStackSize(); }
		public boolean same(ItemStack left, ItemStack right) {
			return ItemStack.isSameItemSameComponents(left, right);
		}
		public ItemStack copy(ItemStack stack, int count) { return stack.copyWithCount(count); }
		public ItemStack empty() { return ItemStack.EMPTY; }
	};

	private InventorySorting() {
	}

	static void cancel() {
		pending = null;
	}

	static void request(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		Screen screen = minecraft.gui.screen();
		if (player == null || minecraft.getSingleplayerServer() == null
			|| !minecraft.isWindowActive() || minecraft.isPaused()
			|| (screen != null && !(screen instanceof AbstractContainerScreen<?>))) {
			return;
		}
		AbstractContainerMenu menu = player.containerMenu;
		if (!supported(menu) || !menu.getCarried().isEmpty()
			|| (screen instanceof AbstractContainerScreen<?> container && container.getMenu() != menu)) {
			return;
		}
		// Let the armor swap's vanilla packets settle before taking the sort snapshot.
		pending = new Pending(player, screen, menu, 2);
	}

	static void tick(Minecraft minecraft) {
		Pending current = pending;
		if (current == null) {
			return;
		}
		if (!DropControlConfig.inventorySorting() || minecraft.player != current.player()
			|| minecraft.gui.screen() != current.screen()
			|| current.player().containerMenu != current.menu()
			|| !current.menu().getCarried().isEmpty() || !minecraft.isWindowActive() || minecraft.isPaused()) {
			pending = null;
			return;
		}
		if (current.delay() > 0) {
			pending = new Pending(current.player(), current.screen(), current.menu(), current.delay() - 1);
			return;
		}
		pending = null;
		var server = minecraft.getSingleplayerServer();
		if (server == null) {
			return;
		}
		AbstractContainerMenu menu = current.menu();
		List<Slot> inventory = new ArrayList<>();
		List<Slot> storage = new ArrayList<>();
		for (Slot slot : menu.slots) {
			if (slot.container == current.player().getInventory()) {
				if (slot.getContainerSlot() >= Inventory.getSelectionSize()
					&& slot.getContainerSlot() < Inventory.INVENTORY_SIZE) {
					inventory.add(slot);
				}
			} else if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) {
				storage.add(slot);
			}
		}
		inventory.sort(Comparator.comparingInt(Slot::getContainerSlot));
		if (inventory.size() != 27) {
			return;
		}
		String language = minecraft.getLanguageManager().getSelected();
		Collator collator = Collator.getInstance(Locale.forLanguageTag(language.replace('_', '-')));
		collator.setStrength(Collator.PRIMARY);
		Comparator<ItemStack> order = Comparator.comparing(
			(ItemStack stack) -> stack.getHoverName().getString(), collator
		).thenComparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		List<Change> changes = new ArrayList<>();
		try {
			plan(inventory, order, changes);
			plan(storage, order, changes);
		} catch (IllegalArgumentException exception) {
			return;
		}
		UUID playerId = current.player().getUUID();
		int menuId = menu.containerId;
		Class<?> menuType = menu.getClass();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null || !DropControlConfig.inventorySorting()) {
				return;
			}
			AbstractContainerMenu active = player.containerMenu;
			if (active.containerId != menuId || active.getClass() != menuType
				|| !active.getCarried().isEmpty() || !active.stillValid(player)) {
				return;
			}
			// Validate the whole snapshot before changing either region. Never apply a stale plan.
			for (Change change : changes) {
				if (change.slot() >= active.slots.size()) {
					return;
				}
				Slot slot = active.getSlot(change.slot());
				if (!ItemStack.matches(slot.getItem(), change.before()) || !slot.mayPickup(player)
					|| (!change.after().isEmpty() && (!slot.mayPlace(change.after())
						|| change.after().getCount() > slot.getMaxStackSize(change.after())))) {
					return;
				}
			}
			for (Change change : changes) {
				if (!ItemStack.matches(change.before(), change.after())) {
					active.getSlot(change.slot()).set(change.after().copy());
				}
			}
			player.getInventory().setChanged();
			player.inventoryMenu.broadcastChanges();
			if (active != player.inventoryMenu) {
				active.broadcastChanges();
			}
		});
	}

	private static void plan(List<Slot> slots, Comparator<ItemStack> order, List<Change> changes) {
		List<ItemStack> before = slots.stream().map(slot -> slot.getItem().copy()).toList();
		List<ItemStack> after = StackSortPlan.create(before, STACKS, order);
		for (int index = 0; index < slots.size(); index++) {
			changes.add(new Change(slots.get(index).index, before.get(index), after.get(index)));
		}
	}

	private static boolean supported(AbstractContainerMenu menu) {
		return menu.getClass() == InventoryMenu.class || menu.getClass() == ChestMenu.class
			|| menu.getClass() == ShulkerBoxMenu.class;
	}

	private record Pending(LocalPlayer player, Screen screen, AbstractContainerMenu menu, int delay) {
	}

	private record Change(int slot, ItemStack before, ItemStack after) {
	}
}
