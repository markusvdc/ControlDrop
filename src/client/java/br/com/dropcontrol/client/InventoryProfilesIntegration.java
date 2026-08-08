package br.com.dropcontrol.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

final class InventoryProfilesIntegration {
	private static final String IPN_MOD_ID = "inventoryprofilesnext";
	private static final int FALLBACK_SEPARATION_TICKS = 100;
	private static ReflectionAccess access;
	private static boolean accessAttempted;
	private static PendingPlayerSort pendingPlayerSort;

	private InventoryProfilesIntegration() {
	}

	static void requestSort(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gameMode == null) {
			return;
		}
		if (!FabricLoader.getInstance().isModLoaded(IPN_MOD_ID)) {
			return;
		}
		ReflectionAccess ipn = access();
		if (ipn == null) {
			return;
		}
		if (pendingPlayerSort != null || Boolean.TRUE.equals(ipn.queueBusy())) {
			return;
		}

		Screen screen = minecraft.gui.screen();
		AbstractContainerMenu menu;
		if (screen == null) {
			menu = player.inventoryMenu;
		} else if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			menu = containerScreen.getMenu();
		} else {
			return;
		}

		if (!menu.getCarried().isEmpty()) {
			return;
		}

		if (menu != player.inventoryMenu) {
			if (ipn.deliverSort(menu, false, true)) {
				pendingPlayerSort = new PendingPlayerSort(menu, screen, ipn.queueSignalAvailable());
			}
		} else {
			ipn.deliverSort(menu, true, false);
		}
	}

	static void tick(Minecraft minecraft) {
		PendingPlayerSort pending = pendingPlayerSort;
		if (pending == null) {
			return;
		}

		ReflectionAccess ipn = access();
		if (ipn == null) {
			pendingPlayerSort = null;
			return;
		}
		pending.ticks++;
		Boolean busy = ipn.queueBusy();
		if (Boolean.TRUE.equals(busy)) {
			pending.sawQueueWork = true;
			pending.idleTicks = 0;
			return;
		}
		pending.idleTicks++;

		boolean finished = pending.queueSignalAvailable
			? (pending.sawQueueWork ? pending.idleTicks >= 2 : pending.ticks >= 2)
			: pending.ticks >= FALLBACK_SEPARATION_TICKS;
		if (!finished) {
			return;
		}

		pendingPlayerSort = null;
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.gameMode == null) {
			return;
		}

		AbstractContainerMenu safeMenu = null;
		Screen currentScreen = minecraft.gui.screen();
		if (currentScreen == pending.expectedScreen && player.containerMenu == pending.expectedMenu) {
			safeMenu = pending.expectedMenu;
		} else if (player.containerMenu == player.inventoryMenu && currentScreen == null) {
			safeMenu = player.inventoryMenu;
		}
		if (safeMenu == null) {
			return;
		}
		if (!safeMenu.getCarried().isEmpty()) {
			return;
		}
		ipn.deliverSort(safeMenu, true, false);
	}

	private static ReflectionAccess access() {
		if (!accessAttempted) {
			accessAttempted = true;
			try {
				access = new ReflectionAccess();
			} catch (ReflectiveOperationException | LinkageError exception) {
			}
		}
		return access;
	}

	private static final class PendingPlayerSort {
		private final AbstractContainerMenu expectedMenu;
		private final Screen expectedScreen;
		private final boolean queueSignalAvailable;
		private int ticks;
		private int idleTicks;
		private boolean sawQueueWork;

		private PendingPlayerSort(AbstractContainerMenu expectedMenu, Screen expectedScreen,
			boolean queueSignalAvailable) {
			this.expectedMenu = expectedMenu;
			this.expectedScreen = expectedScreen;
			this.queueSignalAvailable = queueSignalAvailable;
		}
	}

	private static final class ReflectionAccess {
		private final Object actions;
		private final Method doSort;
		private final Object sortAtCursor;
		private final Method getBooleanValue;
		private final Method setValue;
		private final Method queueContents;

		private ReflectionAccess() throws ReflectiveOperationException {
			Class<?> actionsClass = Class.forName("org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions");
			Field actionsInstance = actionsClass.getField("INSTANCE");
			this.actions = actionsInstance.get(null);
			this.doSort = actionsClass.getMethod("doSort", AbstractContainerMenu.class, boolean.class, boolean.class);

			Class<?> sortSettingsClass = Class.forName("org.anti_ad.mc.ipnext.config.SortSettings");
			Object sortSettings = sortSettingsClass.getField("INSTANCE").get(null);
			this.sortAtCursor = sortSettingsClass.getMethod("getSORT_AT_CURSOR").invoke(sortSettings);
			this.getBooleanValue = this.sortAtCursor.getClass().getMethod("getBooleanValue");
			this.setValue = this.sortAtCursor.getClass().getMethod("setValue", boolean.class);

			Method signal = null;
			try {
				Class<?> clickerClass = Class.forName("org.anti_ad.mc.ipnext.inventory.ContainerClicker");
				signal = clickerClass.getMethod("access$getHighlights$p");
			} catch (ReflectiveOperationException exception) {
			}
			this.queueContents = signal;
		}

		private boolean queueSignalAvailable() {
			return this.queueContents != null;
		}

		private Boolean queueBusy() {
			if (this.queueContents == null) {
				return null;
			}
			try {
				return !((Set<?>) this.queueContents.invoke(null)).isEmpty();
			} catch (ReflectiveOperationException | RuntimeException exception) {
				return null;
			}
		}

		private boolean deliverSort(AbstractContainerMenu menu, boolean playerSide, boolean forceContainerSide) {
			Boolean original = null;
			try {
				if (forceContainerSide) {
					original = (Boolean) this.getBooleanValue.invoke(this.sortAtCursor);
					this.setValue.invoke(this.sortAtCursor, false);
				}
				this.doSort.invoke(this.actions, menu, true, playerSide);
				return true;
			} catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
				return false;
			} finally {
				if (original != null) {
					try {
						this.setValue.invoke(this.sortAtCursor, original.booleanValue());
					} catch (ReflectiveOperationException | RuntimeException exception) {
					}
				}
			}
		}
	}
}
