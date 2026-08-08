package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 72000))
	private int dropcontrol$shortenPhantomRestThreshold(int originalThreshold) {
		return DropControlConfig.phantomPressureOne() ? 48000 : originalThreshold;
	}

	@ModifyVariable(method = "tick", at = @At("STORE"), name = "groupSize")
	private int dropcontrol$doublePhantomGroupSize(
		int originalGroupSize,
		ServerLevel level,
		boolean spawnEnemies
	) {
		if (!DropControlConfig.phantomPressureOne()) {
			return originalGroupSize;
		}
		int modifiedGroupSize = originalGroupSize * 2;
		return modifiedGroupSize;
	}
}
