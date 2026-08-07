package br.com.dropcontrol.client;

import br.com.dropcontrol.DropControl;
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
			DropControl.LOGGER.info("Inventory sorting skipped: no active player or game mode.");
			return;
		}
		if (!FabricLoader.getInstance().isModLoaded(IPN_MOD_ID)) {
			DropControl.LOGGER.warn("Inventory sorting unavailable: Inventory Profiles Next is not installed.");
			return;
		}
		ReflectionAccess ipn = access();
		if (ipn == null) {
			return;
		}
		if (pendingPlayerSort != null || Boolean.TRUE.equals(ipn.queueBusy())) {
			DropControl.LOGGER.warn("Inventory sorting skipped: an IPN sorting sequence is already running.");
			return;
		}

		Screen screen = minecraft.gui.screen();
		AbstractContainerMenu menu;
		if (screen == null) {
			menu = player.inventoryMenu;
		} else if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			menu = containerScreen.getMenu();
		} else {
			DropControl.LOGGER.info("Inventory sorting skipped: current screen is not an inventory interface ({}).",
				screen.getClass().getSimpleName());
			return;
		}

		DropControl.LOGGER.info("Inventory sorting menu selected: {} (containerId={}).",
			menu.getClass().getSimpleName(), menu.containerId);
		if (!menu.getCarried().isEmpty()) {
			DropControl.LOGGER.warn("Inventory sorting skipped: cursor is carrying an item.");
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
			DropControl.LOGGER.info("Pending player inventory sort cancelled: player is no longer available.");
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
			DropControl.LOGGER.warn("Pending player inventory sort cancelled: the expected menu is no longer safely open.");
			return;
		}
		if (!safeMenu.getCarried().isEmpty()) {
			DropControl.LOGGER.warn("Pending player inventory sort cancelled: cursor is carrying an item.");
			return;
		}
		ipn.deliverSort(safeMenu, true, false);
	}

	private static ReflectionAccess access() {
		if (!accessAttempted) {
			accessAttempted = true;
			try {
				access = new ReflectionAccess();
				DropControl.LOGGER.info("Inventory Profiles Next integration initialized.");
			} catch (ReflectiveOperationException | LinkageError exception) {
				DropControl.LOGGER.error("Inventory Profiles Next integration failed to initialize.", exception);
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
				DropControl.LOGGER.warn(
					"IPN exposes no usable completion signal; using a documented 100-tick safe separation."
				);
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
				DropControl.LOGGER.error("Could not inspect the IPN sorting queue.", exception);
				return null;
			}
		}

		private boolean deliverSort(AbstractContainerMenu menu, boolean playerSide, boolean forceContainerSide) {
			DropControl.LOGGER.info("IPN sort requested: side={}, menu={}, containerId={}.",
				playerSide ? "player" : "container", menu.getClass().getSimpleName(), menu.containerId);
			Boolean original = null;
			try {
				if (forceContainerSide) {
					original = (Boolean) this.getBooleanValue.invoke(this.sortAtCursor);
					this.setValue.invoke(this.sortAtCursor, false);
				}
				this.doSort.invoke(this.actions, menu, true, playerSide);
				DropControl.LOGGER.info("IPN sort call delivered: side={}, containerId={}.",
					playerSide ? "player" : "container", menu.containerId);
				return true;
			} catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
				DropControl.LOGGER.error("Inventory Profiles Next sort call failed.", exception);
				return false;
			} finally {
				if (original != null) {
					try {
						this.setValue.invoke(this.sortAtCursor, original.booleanValue());
						DropControl.LOGGER.info("IPN SORT_AT_CURSOR restored to {}.", original);
					} catch (ReflectiveOperationException | RuntimeException exception) {
						DropControl.LOGGER.error("Could not restore IPN SORT_AT_CURSOR to {}.", original, exception);
					}
				}
			}
		}
	}
}
