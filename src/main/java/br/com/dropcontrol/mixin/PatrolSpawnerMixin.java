package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PatrolSpawner.class)
public abstract class PatrolSpawnerMixin {
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 12000))
	private int dropcontrol$shortenPatrolInterval(int originalInterval) {
		return DropControlConfig.constantThreat() ? 800 : originalInterval;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 1200))
	private int dropcontrol$shortenPatrolIntervalVariation(int originalVariation) {
		return DropControlConfig.constantThreat() ? 80 : originalVariation;
	}
}
