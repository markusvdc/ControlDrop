package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.MainInventoryPriority;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Shadow public NonNullList<Slot> slots;

	@Redirect(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	private ItemStack dropcontrol$trackQuickMoveSource(
		AbstractContainerMenu menu,
		Player player,
		int slotIndex
	) {
		if (!DropControlConfig.ascendingTreasure()
			|| slotIndex < 0
			|| slotIndex >= this.slots.size()
			|| this.slots.get(slotIndex).container instanceof Inventory) {
			return menu.quickMoveStack(player, slotIndex);
		}
		return MainInventoryPriority.whileActive(() -> menu.quickMoveStack(player, slotIndex));
	}

	@Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$preferMainInventoryOnQuickMove(
		ItemStack stack,
		int startIndex,
		int endIndex,
		boolean reverseDirection,
		CallbackInfoReturnable<Boolean> callback
	) {
		if (!DropControlConfig.ascendingTreasure() || !MainInventoryPriority.isActive()) {
			return;
		}

		List<Integer> mainInventorySlots = new ArrayList<>();
		List<Integer> hotbarSlots = new ArrayList<>();
		for (int index = startIndex; index < endIndex; index++) {
			Slot slot = this.slots.get(index);
			if (!(slot.container instanceof Inventory)) {
				return;
			}
			int inventorySlot = slot.getContainerSlot();
			if (inventorySlot >= 9 && inventorySlot < 36) {
				mainInventorySlots.add(index);
			} else if (Inventory.isHotbarSlot(inventorySlot)) {
				hotbarSlots.add(index);
			}
		}
		if (mainInventorySlots.isEmpty() || hotbarSlots.isEmpty()) {
			return;
		}

		if (reverseDirection) {
			mainInventorySlots = mainInventorySlots.reversed();
			hotbarSlots = hotbarSlots.reversed();
		}
		List<Integer> orderedSlots = new ArrayList<>(mainInventorySlots.size() + hotbarSlots.size());
		orderedSlots.addAll(mainInventorySlots);
		orderedSlots.addAll(hotbarSlots);
		callback.setReturnValue(dropcontrol$moveItemStackTo(stack, orderedSlots));
	}

	private boolean dropcontrol$moveItemStackTo(ItemStack stack, List<Integer> orderedSlots) {
		boolean moved = false;
		if (stack.isStackable()) {
			for (int index : orderedSlots) {
				if (stack.isEmpty()) {
					break;
				}
				Slot slot = this.slots.get(index);
				ItemStack existing = slot.getItem();
				if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(stack, existing)) {
					continue;
				}
				int combinedCount = existing.getCount() + stack.getCount();
				int maximum = slot.getMaxStackSize(existing);
				if (combinedCount <= maximum) {
					stack.setCount(0);
					existing.setCount(combinedCount);
					slot.setChanged();
					moved = true;
				} else if (existing.getCount() < maximum) {
					stack.shrink(maximum - existing.getCount());
					existing.setCount(maximum);
					slot.setChanged();
					moved = true;
				}
			}
		}

		if (!stack.isEmpty()) {
			for (int index : orderedSlots) {
				Slot slot = this.slots.get(index);
				if (!slot.getItem().isEmpty() || !slot.mayPlace(stack)) {
					continue;
				}
				int maximum = slot.getMaxStackSize(stack);
				slot.setByPlayer(stack.split(Math.min(stack.getCount(), maximum)));
				slot.setChanged();
				moved = true;
				break;
			}
		}
		return moved;
	}
}
