package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.FixedMobDropPolicy;
import br.com.dropcontrol.access.PhantomBiteAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(
		method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 0
	)
	private float dropcontrol$modifyConfiguredDamage(
		float amount,
		ServerLevel level,
		DamageSource source
	) {
		if (DropControlConfig.constantThreat() && source.getEntity() instanceof Raider) {
			return amount * 1.5F;
		}
		if (DropControlConfig.phantomPressureTwo() && source.getEntity() instanceof Phantom phantom) {
			float modifiedAmount = amount * 2.0F;
			return modifiedAmount;
		}
		return amount;
	}

	@Inject(
		method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
		at = @At("RETURN")
	)
	private void dropcontrol$rememberAcceptedPhantomBite(
		ServerLevel level,
		DamageSource source,
		float amount,
		CallbackInfoReturnable<Boolean> callback
	) {
		if (DropControlConfig.phantomPressureTwo()
			&& callback.getReturnValueZ()
			&& (Object)this instanceof Player player
			&& source.getEntity() instanceof Phantom phantom) {
			((PhantomBiteAccess)phantom).dropcontrol$markPlayerBitten(player);
		}
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
		FixedMobDropPolicy.drop(level, entity);
	}
}
