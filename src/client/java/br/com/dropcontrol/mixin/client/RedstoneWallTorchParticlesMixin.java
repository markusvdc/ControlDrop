package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.RedstoneConcealment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneWallTorchBlock.class)
public abstract class RedstoneWallTorchParticlesMixin {
	@Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$hideConcealedParticles(
		BlockState state,
		Level level,
		BlockPos pos,
		RandomSource random,
		CallbackInfo callbackInfo
	) {
		Minecraft minecraft = Minecraft.getInstance();
		if (DropControlConfig.arcaneRedstone()
			&& RedstoneConcealment.isConcealed(state)
			&& (minecraft.player == null || !minecraft.player.hasEffect(MobEffects.NIGHT_VISION))) {
			callbackInfo.cancel();
		}
	}
}
