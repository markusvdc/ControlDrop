package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrushItem.class)
public abstract class BrushItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableProtectedBrush(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (dropcontrol$isProtectedBroken(context.getItemInHand())) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$stopProtectedBrush(
		Level level,
		LivingEntity entity,
		ItemStack stack,
		int remainingUseTicks,
		CallbackInfo ci
	) {
		if (dropcontrol$isProtectedBroken(stack)) {
			entity.releaseUsingItem();
			ci.cancel();
		}
	}

	private static boolean dropcontrol$isProtectedBroken(ItemStack stack) {
		return DropControlConfig.eternalRelics()
			&& stack.isDamageableItem()
			&& stack.getDamageValue() >= stack.getMaxDamage() - 1;
	}
}
