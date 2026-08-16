package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.MainInventoryPriority;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Redirect(
		method = "playerTouch",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
		)
	)
	private boolean dropcontrol$prioritizeMainInventoryOnPickup(Inventory inventory, ItemStack stack) {
		return MainInventoryPriority.whileActive(() -> inventory.add(stack));
	}
}
