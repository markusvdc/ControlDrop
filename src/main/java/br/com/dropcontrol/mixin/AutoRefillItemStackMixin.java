package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.AutoRefill;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class AutoRefillItemStackMixin {
	@WrapMethod(method = "finishUsingItem")
	private ItemStack dropcontrol$refillAfterEating(Level level, LivingEntity entity, Operation<ItemStack> original) {
		ServerPlayer player = entity instanceof ServerPlayer serverPlayer ? serverPlayer : null;
		var attempt = player != null && player.getMainHandItem() == (Object) this
			? AutoRefill.capture(player, InteractionHand.MAIN_HAND) : null;
		ItemStack result = original.call(level, entity);
		if (result.isEmpty() && player != null) {
			AutoRefill.consumed(player, attempt);
		}
		return result;
	}
}
