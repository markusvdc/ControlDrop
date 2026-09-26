package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class HorseReinsRendererMixin {
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void dropcontrol$alignHorseWithRider(LivingEntity entity, LivingEntityRenderState state,
			float partialTick, CallbackInfo callback) {
		if (!DropControlConfig.sovereignReins()
				|| !(entity instanceof Horse || entity instanceof SkeletonHorse || entity instanceof ZombieHorse)) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.getCameraEntity() != player
				|| player.getVehicle() != entity || entity.getControllingPassenger() != player) {
			return;
		}
		// Match the camera's yaw sample without mutating simulation or network state.
		// Vanilla tickRidden already aligns both horse body and head to its rider.
		state.bodyRot = player.getViewYRot(partialTick);
		state.yRot = 0.0F;
	}
}
