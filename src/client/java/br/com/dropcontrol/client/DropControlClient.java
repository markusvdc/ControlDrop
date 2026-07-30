package br.com.dropcontrol.client;

import br.com.dropcontrol.config.DropControlConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

public final class DropControlClient implements ClientModInitializer {
	private static final int CHEST_ARMOR_MENU_SLOT = 6;
	private static boolean graveAccentWasDown;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(DropControlClient::onEndClientTick);
	}

	private static void onEndClientTick(Minecraft minecraft) {
		boolean graveAccentDown = InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_GRAVE);
		if (graveAccentDown && !graveAccentWasDown && DropControlConfig.chestplateElytraSwap()) {
			swapChestEquipment(minecraft);
		}
		graveAccentWasDown = graveAccentDown;
	}

	private static void swapChestEquipment(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null
			|| minecraft.gameMode == null
			|| minecraft.gui.screen() != null
			|| player.containerMenu != player.inventoryMenu
			|| !player.inventoryMenu.getCarried().isEmpty()) {
			return;
		}

		boolean needsChestplate = player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
		int inventorySlot = findLeastDurableCandidate(player.getInventory(), needsChestplate);
		if (inventorySlot < 0) {
			return;
		}

		int menuSlot = inventorySlot < Inventory.getSelectionSize() ? inventorySlot + 36 : inventorySlot;
		click(minecraft, player, menuSlot);
		click(minecraft, player, CHEST_ARMOR_MENU_SLOT);
		click(minecraft, player, menuSlot);
	}

	private static int findLeastDurableCandidate(Inventory inventory, boolean chestplate) {
		int selectedSlot = -1;
		int lowestRemainingDurability = Integer.MAX_VALUE;
		for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!isCandidate(stack, chestplate)) {
				continue;
			}

			int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
			if (remainingDurability < lowestRemainingDurability) {
				selectedSlot = slot;
				lowestRemainingDurability = remainingDurability;
			}
		}
		return selectedSlot;
	}

	private static boolean isCandidate(ItemStack stack, boolean chestplate) {
		if (chestplate) {
			Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
			return !stack.is(Items.ELYTRA) && equippable != null && equippable.slot() == EquipmentSlot.CHEST;
		}
		return stack.is(Items.ELYTRA);
	}

	private static void click(Minecraft minecraft, LocalPlayer player, int slot) {
		minecraft.gameMode.handleContainerInput(
			InventoryMenu.CONTAINER_ID,
			slot,
			0,
			ContainerInput.PICKUP,
			player
		);
	}
}
