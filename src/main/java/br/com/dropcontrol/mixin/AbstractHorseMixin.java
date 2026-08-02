package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
	@Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$keepSaddledHorseStill(CallbackInfoReturnable<Boolean> callback) {
		AbstractHorse horse = (AbstractHorse)(Object)this;
		if (DropControlConfig.saddledHorseStaysPut() && horse.isSaddled() && !horse.isVehicle() && !horse.isInLove()) {
			callback.setReturnValue(true);
		}
	}
}
