package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Armadillo.class)
public abstract class ArmadilloMixin {
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableProtectedBrush(
		Player player,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		ItemStack stack = player.getItemInHand(hand);
		if (DropControlConfig.eternalRelics()
			&& stack.is(Items.BRUSH)
			&& stack.getDamageValue() >= stack.getMaxDamage() - 1) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
