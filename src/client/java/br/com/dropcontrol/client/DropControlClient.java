package br.com.dropcontrol.client;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.RedstoneConcealment;
import br.com.dropcontrol.mixin.client.AbstractContainerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class DropControlClient implements ClientModInitializer {
	private static final int CHEST_ARMOR_MENU_SLOT = 6;
	private static final long MOUSE_IDLE_PAUSE_DELAY_MS = 15_000L;
	private static boolean graveAccentWasDown;
	private static boolean gWasDown;
	private static double lastMouseX;
	private static double lastMouseY;
	private static long lastMouseMovementTime;
	private static boolean mousePositionInitialized;
	private static boolean redstoneVisibilityInitialized;
	private static boolean lastArcaneRedstoneEnabled;
	private static boolean lastNightVisionActive;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(DropControlClient::onEndClientTick);
	}

	private static void onEndClientTick(Minecraft minecraft) {
		boolean graveAccentDown = InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_GRAVE);
		if (graveAccentDown && !graveAccentWasDown) {
			boolean swapEnabled = DropControlConfig.chestplateElytraSwap();
			boolean sortingEnabled = DropControlConfig.inventorySorting();
			if (swapEnabled) {
				swapChestEquipment(minecraft);
			}
			if (sortingEnabled) {
				InventoryProfilesIntegration.requestSort(minecraft);
			}
		}
		graveAccentWasDown = graveAccentDown;
		tickSovereignVoid(minecraft);
		InventoryProfilesIntegration.tick(minecraft);
		tickMouseIdlePause(minecraft);
		tickArcaneRedstoneVisibility(minecraft);
	}

	private static void tickArcaneRedstoneVisibility(Minecraft minecraft) {
		boolean enabled = DropControlConfig.arcaneRedstone();
		boolean nightVision = minecraft.player != null && minecraft.player.hasEffect(MobEffects.NIGHT_VISION);
		if (redstoneVisibilityInitialized
			&& enabled == lastArcaneRedstoneEnabled
			&& nightVision == lastNightVisionActive) {
			return;
		}

		lastArcaneRedstoneEnabled = enabled;
		lastNightVisionActive = nightVision;
		redstoneVisibilityInitialized = true;
		if (minecraft.level != null) {
			markConcealedRedstoneSectionsDirty(minecraft);
		}
	}

	private static void markConcealedRedstoneSectionsDirty(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}

		int centerChunkX = player.blockPosition().getX() >> 4;
		int centerChunkZ = player.blockPosition().getZ() >> 4;
		int radius = minecraft.options.getEffectiveRenderDistance();
		for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
			for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
				if (!minecraft.level.getChunkSource().hasChunk(chunkX, chunkZ)) {
					continue;
				}

				LevelChunk chunk = minecraft.level.getChunk(chunkX, chunkZ);
				LevelChunkSection[] sections = chunk.getSections();
				for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
					if (sections[sectionIndex].maybeHas(RedstoneConcealment::isConcealed)) {
						minecraft.level.setSectionDirtyWithNeighbors(
							chunkX,
							minecraft.level.getMinSectionY() + sectionIndex,
							chunkZ
						);
					}
				}
			}
		}
	}

	private static void tickSovereignVoid(Minecraft minecraft) {
		boolean gDown = InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_G);
		if (gDown && !gWasDown && DropControlConfig.sovereignVoid()) {
			if (minecraft.hasControlDown() && !minecraft.hasShiftDown()) {
				deleteHoveredStack(minecraft);
			} else if (minecraft.hasShiftDown()
				&& !minecraft.hasControlDown()
				&& minecraft.gui.screen() instanceof AbstractContainerScreen<?>) {
				SovereignVoid.undo(minecraft);
			}
		}
		gWasDown = gDown;
	}

	private static void deleteHoveredStack(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null
			|| !(minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen)
			|| !screen.getMenu().getCarried().isEmpty()) {
			return;
		}

		Slot slot = ((AbstractContainerScreenAccessor)screen).dropcontrol$getHoveredSlot();
		if (slot == null || !slot.hasItem()) {
			return;
		}
		if (slot.container == player.getInventory()) {
			int inventorySlot = slot.getContainerSlot();
			if (inventorySlot < 0 || inventorySlot >= Inventory.INVENTORY_SIZE) {
				return;
			}
			SovereignVoid.deleteInventoryStack(minecraft, inventorySlot, slot.getItem());
			return;
		}
		if (screen.getMenu() instanceof ChestMenu || screen.getMenu() instanceof ShulkerBoxMenu) {
			SovereignVoid.deleteContainerStack(minecraft, screen.getMenu().containerId, slot.index, slot.getItem());
		}
	}

	private static void tickMouseIdlePause(Minecraft minecraft) {
		double mouseX = minecraft.mouseHandler.xpos();
		double mouseY = minecraft.mouseHandler.ypos();
		long now = System.nanoTime() / 1_000_000L;
		boolean mouseMoved = !mousePositionInitialized || mouseX != lastMouseX || mouseY != lastMouseY;

		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mousePositionInitialized = true;
		if (mouseMoved) {
			lastMouseMovementTime = now;
		}

		boolean canPauseForIdle = DropControlConfig.pauseWhenMouseIdle()
			&& minecraft.level != null
			&& minecraft.player != null
			&& minecraft.gui.screen() == null
			&& minecraft.isWindowActive()
			&& minecraft.hasSingleplayerServer()
			&& !minecraft.getSingleplayerServer().isPublished();
		if (!canPauseForIdle) {
			lastMouseMovementTime = now;
			return;
		}

		if (now - lastMouseMovementTime >= MOUSE_IDLE_PAUSE_DELAY_MS) {
			minecraft.pauseGame(false);
			lastMouseMovementTime = now;
		}
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
