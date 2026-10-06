package br.com.dropcontrol.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

/** Bridges the physical grave-key event to the inventory sorting request. */
public final class InventorySortKey {
	private InventorySortKey() {
	}

	public static void request(Minecraft minecraft) {
		InventorySorting.request(minecraft);
	}

	public static void handleKeyPress(Minecraft minecraft, long window, int action, KeyEvent event) {
		if (window != minecraft.getWindow().handle() || action != InputConstants.PRESS
			|| !(minecraft.gui.screen() instanceof AbstractContainerScreen<?>)) {
			return;
		}
		// Some keyboard layouts report the grave-accent key as apostrophe (keycode 39).
		boolean gravePressed = event.key() == InputConstants.KEY_GRAVE
			|| event.keycode() == '`'
			|| event.keycode() == InputConstants.KEY_APOSTROPHE;
		if (gravePressed && !event.hasControlDown() && !event.hasAltDown() && !event.hasShiftDown()) {
			request(minecraft);
		}
	}
}
