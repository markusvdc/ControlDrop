package br.com.dropcontrol.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

final class SovereignVoid {
	private static IntegratedServer historyServer;
	private static DeletedStack history;

	private SovereignVoid() {
	}

	static void delete(Minecraft minecraft, int inventorySlot, ItemStack expectedStack) {
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

			history = new DeletedStack(playerId, inventorySlot, current.copy());
			inventory.setItem(inventorySlot, ItemStack.EMPTY);
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
			restoreToOriginalSlot(inventory, deleted.inventorySlot(), remaining);
			if (!remaining.isEmpty()) {
				inventory.add(remaining);
			}
			if (!remaining.isEmpty()) {
				player.drop(remaining, false);
			}
			synchronize(player);
		});
	}

	private static void restoreToOriginalSlot(Inventory inventory, int slot, ItemStack remaining) {
		ItemStack current = inventory.getItem(slot);
		if (current.isEmpty()) {
			inventory.setItem(slot, remaining.copy());
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

	private record DeletedStack(UUID playerId, int inventorySlot, ItemStack stack) {
	}
}
