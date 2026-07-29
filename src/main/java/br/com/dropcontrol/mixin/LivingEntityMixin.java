package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.CreeperTntDropPolicy;
import br.com.dropcontrol.gameplay.WitchPotionDropPolicy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(
		method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 0
	)
	private float dropcontrol$doubleRaiderDamage(
		float amount,
		ServerLevel level,
		DamageSource source
	) {
		return DropControlConfig.constantThreat() && source.getEntity() instanceof Raider
			? amount * 2.0F
			: amount;
	}

	@Inject(
		method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V",
		at = @At("TAIL")
	)
	private void dropcontrol$addConfiguredDrop(
		ServerLevel level,
		DamageSource source,
		boolean playerKilled,
		CallbackInfo callback
	) {
		LivingEntity entity = (LivingEntity)(Object)this;
		CreeperTntDropPolicy.tryDrop(level, entity);
		WitchPotionDropPolicy.drop(level, entity);
	}
}
