package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.RedstoneConcealment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$hideConcealedRedstone(CallbackInfoReturnable<RenderShape> callbackInfo) {
		BlockState state = (BlockState)(Object)this;
		if (!DropControlConfig.arcaneRedstone() || !RedstoneConcealment.isConcealed(state)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
			callbackInfo.setReturnValue(RenderShape.INVISIBLE);
		}
	}
}
