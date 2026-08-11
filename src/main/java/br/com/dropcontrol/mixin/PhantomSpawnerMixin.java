package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
	@ModifyConstant(method = "tick", constant = @Constant(intValue = 72000))
	private int dropcontrol$shortenPhantomRestThreshold(int originalThreshold) {
		return DropControlConfig.phantomPressureTwo() ? 48000 : originalThreshold;
	}
}
