package br.com.dropcontrol.client;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.MatchingTransferPlan;
import br.com.dropcontrol.gameplay.StackSortPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MatchingTransfer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final StackSortPlan.Stacks<ItemStack> STACKS = new StackSortPlan.Stacks<>() {
		public int count(ItemStack stack) { return stack.getCount(); }
		public int limit(ItemStack stack) { return stack.getMaxStackSize(); }
		public boolean same(ItemStack left, ItemStack right) {
			return ItemStack.isSameItemSameComponents(left, right);
		}
		public ItemStack copy(ItemStack stack, int count) { return stack.copyWithCount(count); }
		public ItemStack empty() { return ItemStack.EMPTY; }
	};

	private MatchingTransfer() {
	}

	public static void request(Minecraft minecraft) {
		InventorySorting.cancel();
		LOGGER.info("[ControlDrop] Matching transfer shortcut received");
		var server = minecraft.getSingleplayerServer();
		if (server == null || minecraft.player == null || !minecraft.isWindowActive() || minecraft.isPaused()
			|| !DropControlConfig.matchingTransfer()
			|| !(minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
			LOGGER.info("[ControlDrop] Matching transfer unavailable: check option, singleplayer and open container");
			return;
		}
		AbstractContainerMenu menu = screen.getMenu();
		if (menu != minecraft.player.containerMenu || !supported(menu) || !menu.getCarried().isEmpty()) {
			LOGGER.info("[ControlDrop] Matching transfer rejected: menu={}, cursorEmpty={}",
				menu.getClass().getSimpleName(), menu.getCarried().isEmpty());
			return;
		}
		UUID playerId = minecraft.player.getUUID();
		int menuId = menu.containerId;
		Class<?> menuType = menu.getClass();
		int slotCount = menu.slots.size();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null || !DropControlConfig.matchingTransfer()) {
				LOGGER.info("[ControlDrop] Matching transfer cancelled: player unavailable or option disabled");
				return;
			}
			AbstractContainerMenu active = player.containerMenu;
			if (active.containerId != menuId || active.getClass() != menuType
				|| !active.getCarried().isEmpty() || !active.stillValid(player)
				|| active.slots.size() != slotCount) {
				LOGGER.info("[ControlDrop] Matching transfer cancelled: container changed or no longer usable");
				return;
			}
			List<Slot> inventory = new ArrayList<>();
			List<Slot> storage = new ArrayList<>();
			// This is a high-level deposit request, not a client-authored slot replacement.
			// Read and plan entirely on the server thread so unrelated client data cannot veto it.
			for (int index = 0; index < slotCount; index++) {
				Slot slot = active.getSlot(index);
				if (slot.container == player.getInventory()) {
					if (slot.getContainerSlot() >= 0 && slot.getContainerSlot() < Inventory.INVENTORY_SIZE
						&& slot.mayPickup(player)) {
						inventory.add(slot);
					}
				} else {
					storage.add(slot);
				}
			}
			var result = MatchingTransferPlan.create(
				inventory.stream().map(slot -> slot.getItem().copy()).toList(),
				storage.stream().map(slot -> slot.getItem().copy()).toList(), STACKS,
				(index, stack) -> storage.get(index).mayPlace(stack) ? storage.get(index).getMaxStackSize(stack) : 0);
			int moved = 0;
			for (int index = 0; index < inventory.size(); index++) {
				moved += inventory.get(index).getItem().getCount() - result.inventory().get(index).getCount();
			}
			apply(inventory, result.inventory());
			apply(storage, result.storage());
			player.getInventory().setChanged();
			player.inventoryMenu.broadcastChanges();
			active.broadcastChanges();
			LOGGER.info("[ControlDrop] Matching transfer completed: {} items, {} inventory slots, {} storage slots",
				moved, inventory.size(), storage.size());
		});
	}

	private static void apply(List<Slot> slots, List<ItemStack> result) {
		for (int index = 0; index < slots.size(); index++) {
			if (!ItemStack.matches(slots.get(index).getItem(), result.get(index))) {
				slots.get(index).set(result.get(index).copy());
			}
		}
	}

	private static boolean supported(AbstractContainerMenu menu) {
		return menu.getClass() == ChestMenu.class || menu.getClass() == ShulkerBoxMenu.class;
	}
}
