package br.com.dropcontrol.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class SovereignVoid {
	private static IntegratedServer historyServer;
	private static DeletedStack history;

	private SovereignVoid() {
	}

	static void deleteInventoryStack(Minecraft minecraft, int inventorySlot, ItemStack expectedStack) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null || minecraft.player == null) {
			return;
		}
		UUID playerId = minecraft.player.getUUID();
		ItemStack expected = expectedStack.copy();
		server.execute(() -> {
			resetFor(server);
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null || inventorySlot < 0 || inventorySlot >= Inventory.INVENTORY_SIZE) {
				return;
			}
			Inventory inventory = player.getInventory();
			ItemStack current = inventory.getItem(inventorySlot);
			if (!ItemStack.matches(current, expected)) {
				return;
			}

			history = new DeletedStack(playerId, inventorySlot, -1, -1, current.copy());
			inventory.setItem(inventorySlot, ItemStack.EMPTY);
			synchronize(player);
		});
	}

	static void deleteContainerStack(Minecraft minecraft, int containerId, int menuSlot, ItemStack expectedStack) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null || minecraft.player == null) {
			return;
		}
		UUID playerId = minecraft.player.getUUID();
		ItemStack expected = expectedStack.copy();
		server.execute(() -> {
			resetFor(server);
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			AbstractContainerMenu menu = player == null ? null : player.containerMenu;
			if (!isSupportedStorageMenu(menu)
				|| menu.containerId != containerId
				|| menuSlot < 0
				|| menuSlot >= menu.slots.size()) {
				return;
			}
			Slot slot = menu.getSlot(menuSlot);
			if (slot.container == player.getInventory() || !ItemStack.matches(slot.getItem(), expected)) {
				return;
			}

			history = new DeletedStack(playerId, -1, containerId, menuSlot, slot.getItem().copy());
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			synchronize(player);
		});
	}

	static void undo(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null || minecraft.player == null) {
			return;
		}
		UUID playerId = minecraft.player.getUUID();
		server.execute(() -> {
			resetFor(server);
			DeletedStack deleted = history;
			if (deleted == null || !deleted.playerId().equals(playerId)) {
				return;
			}
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null) {
				return;
			}

			history = null;
			Inventory inventory = player.getInventory();
			ItemStack remaining = deleted.stack().copy();
			restoreToOriginalSlot(player, deleted, remaining);
			if (!remaining.isEmpty()) {
				inventory.add(remaining);
			}
			if (!remaining.isEmpty()) {
				player.drop(remaining, false);
			}
			synchronize(player);
		});
	}

	private static void restoreToOriginalSlot(ServerPlayer player, DeletedStack deleted, ItemStack remaining) {
		if (deleted.inventorySlot() >= 0) {
			restoreToSlot(player.getInventory().getItem(deleted.inventorySlot()), remaining,
				stack -> player.getInventory().setItem(deleted.inventorySlot(), stack));
			return;
		}
		AbstractContainerMenu menu = player.containerMenu;
		if (!isSupportedStorageMenu(menu)
			|| menu.containerId != deleted.containerId()
			|| deleted.menuSlot() < 0
			|| deleted.menuSlot() >= menu.slots.size()) {
			return;
		}
		Slot slot = menu.getSlot(deleted.menuSlot());
		if (slot.container == player.getInventory()) {
			return;
		}
		restoreToSlot(slot.getItem(), remaining, stack -> {
			slot.set(stack);
			slot.setChanged();
		});
	}

	private static void restoreToSlot(ItemStack current, ItemStack remaining, java.util.function.Consumer<ItemStack> setStack) {
		if (current.isEmpty()) {
			setStack.accept(remaining.copy());
			remaining.setCount(0);
			return;
		}
		if (!ItemStack.isSameItemSameComponents(current, remaining)) {
			return;
		}
		int transferable = Math.min(remaining.getCount(), current.getMaxStackSize() - current.getCount());
		if (transferable > 0) {
			current.grow(transferable);
			remaining.shrink(transferable);
		}
	}

	private static boolean isSupportedStorageMenu(AbstractContainerMenu menu) {
		return menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu;
	}

	private static void synchronize(ServerPlayer player) {
		player.getInventory().setChanged();
		player.inventoryMenu.broadcastChanges();
		if (player.containerMenu != player.inventoryMenu) {
			player.containerMenu.broadcastChanges();
		}
	}

	private static void resetFor(IntegratedServer server) {
		if (historyServer != server) {
			historyServer = server;
			history = null;
		}
	}

	private record DeletedStack(UUID playerId, int inventorySlot, int containerId, int menuSlot, ItemStack stack) {
	}
}
