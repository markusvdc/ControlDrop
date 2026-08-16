package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.MainInventoryPriority;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
	private static final int MAIN_INVENTORY_START = 9;
	private static final int MAIN_INVENTORY_END = 36;

	@Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$preferMainInventoryFreeSlot(CallbackInfoReturnable<Integer> callback) {
		if (!DropControlConfig.ascendingTreasure() || !MainInventoryPriority.isActive()) {
			return;
		}

		Inventory inventory = (Inventory)(Object)this;
		for (int slot = MAIN_INVENTORY_START; slot < MAIN_INVENTORY_END; slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				callback.setReturnValue(slot);
				return;
			}
		}
	}

	@Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$preferMainInventoryStack(
		ItemStack incoming,
		CallbackInfoReturnable<Integer> callback
	) {
		if (!DropControlConfig.ascendingTreasure() || !MainInventoryPriority.isActive()) {
			return;
		}

		Inventory inventory = (Inventory)(Object)this;
		for (int slot = MAIN_INVENTORY_START; slot < MAIN_INVENTORY_END; slot++) {
			ItemStack existing = inventory.getItem(slot);
			if (ItemStack.isSameItemSameComponents(existing, incoming)
				&& existing.isStackable()
				&& existing.getCount() < inventory.getMaxStackSize(existing)) {
				callback.setReturnValue(slot);
				return;
			}
		}
	}
}
