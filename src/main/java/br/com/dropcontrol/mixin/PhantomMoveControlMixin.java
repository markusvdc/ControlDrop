package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomMoveControl")
public abstract class PhantomMoveControlMixin {
	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.1F))
	private float dropcontrol$increaseCollisionRecoverySpeed(float originalSpeed) {
		return increaseByTwentyPercent(originalSpeed);
	}

	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 1.8F))
	private float dropcontrol$increaseForwardSpeed(float originalSpeed) {
		return increaseByTwentyPercent(originalSpeed);
	}

	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.2F, ordinal = 0))
	private float dropcontrol$increaseTurningSpeed(float originalSpeed) {
		return increaseByTwentyPercent(originalSpeed);
	}

	private static float increaseByTwentyPercent(float speed) {
		return DropControlConfig.phantomPressureTwo() ? speed * 1.2F : speed;
	}
}
