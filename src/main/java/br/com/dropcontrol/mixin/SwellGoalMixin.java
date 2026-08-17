package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SwellGoal.class)
public abstract class SwellGoalMixin {
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/Creeper;setSwellDir(I)V"
		)
	)
	private void dropcontrol$keepExplosionCharging(Creeper creeper, int direction) {
		if (DropControlConfig.inevitableExplosion() && direction < 0 && creeper.getSwellDir() > 0) {
			creeper.setSwellDir(1);
			return;
		}
		creeper.setSwellDir(direction);
	}
}
