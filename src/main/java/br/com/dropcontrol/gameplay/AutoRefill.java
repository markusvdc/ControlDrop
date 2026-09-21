package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AutoRefill {
	private static final Map<ServerPlayer, Attempt> PENDING = new IdentityHashMap<>();

	private AutoRefill() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var entry : PENDING.entrySet()) {
				refill(entry.getKey(), entry.getValue());
			}
			PENDING.clear();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> PENDING.clear());
	}

	public static Attempt capture(ServerPlayer player, InteractionHand hand) {
		if (!DropControlConfig.autoRefill() || hand != InteractionHand.MAIN_HAND
			|| player.level().getServer().isDedicatedServer() || player.getAbilities().instabuild
			|| player.containerMenu != player.inventoryMenu || !player.inventoryMenu.getCarried().isEmpty()) {
			return null;
		}
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty() || held.isDamageableItem()) {
			return null;
		}
		return new Attempt(player.getInventory().getSelectedSlot(), held, held.copyWithCount(1));
	}

	public static void consumed(ServerPlayer player, Attempt attempt) {
		if (attempt != null && attempt.original().isEmpty()) {
			PENDING.put(player, attempt);
		}
	}

	private static void refill(ServerPlayer player, Attempt attempt) {
		Inventory inventory = player.getInventory();
		if (!DropControlConfig.autoRefill() || !player.isAlive() || player.isSpectator()
			|| player.hasDisconnected() || inventory.getSelectedSlot() != attempt.slot()
			|| player.containerMenu != player.inventoryMenu || !player.inventoryMenu.getCarried().isEmpty()
			|| !inventory.getItem(attempt.slot()).isEmpty()) {
			return;
		}
		for (int source = Inventory.getSelectionSize(); source < Inventory.INVENTORY_SIZE; source++) {
			ItemStack candidate = inventory.getItem(source);
			if (!candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, attempt.template())) {
				inventory.setItem(source, ItemStack.EMPTY);
				inventory.setItem(attempt.slot(), candidate);
				inventory.setChanged();
				player.inventoryMenu.broadcastChanges();
				return;
			}
		}
	}

	public record Attempt(int slot, ItemStack original, ItemStack template) {
	}
}
