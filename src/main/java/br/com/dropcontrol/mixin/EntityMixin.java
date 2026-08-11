package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.DropRemovalPolicy;
import br.com.dropcontrol.access.PhantomBiteAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	private static final ThreadLocal<Boolean> SPAWNING_BONUS_BOTTLE = new ThreadLocal<>();

	@Inject(
		method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void dropcontrol$removeConfiguredDeathDrop(
		ServerLevel level,
		ItemStack stack,
		Vec3 offset,
		CallbackInfoReturnable<ItemEntity> callback
	) {
		if (DropControlConfig.phantomPressureTwo()
			&& (Object)this instanceof Phantom phantom
			&& ((PhantomBiteAccess)phantom).dropcontrol$hasBittenPlayer()
			&& stack.is(Items.PHANTOM_MEMBRANE)) {
			callback.setReturnValue(null);
			return;
		}

		if (!Boolean.TRUE.equals(SPAWNING_BONUS_BOTTLE.get())
			&& DropControlConfig.constantThreat()
			&& (Object)this instanceof Raider raider
			&& raider.getCurrentRaid() == null
			&& raider.isCaptain()
			&& stack.is(Items.OMINOUS_BOTTLE)) {
			stack.setCount(1);
			randomizeOminousLevel(raider, stack);
			spawnBonusBottle(level, raider, offset);
		}

		if ((Object)this instanceof LivingEntity livingEntity
			&& DropRemovalPolicy.shouldRemove(livingEntity, stack)) {
			callback.setReturnValue(null);
		}
	}

	private static void spawnBonusBottle(ServerLevel level, Raider raider, Vec3 offset) {
		ItemStack bonusBottle = new ItemStack(Items.OMINOUS_BOTTLE);
		randomizeOminousLevel(raider, bonusBottle);

		SPAWNING_BONUS_BOTTLE.set(true);
		try {
			raider.spawnAtLocation(level, bonusBottle, offset);
		} finally {
			SPAWNING_BONUS_BOTTLE.remove();
		}
	}

	private static void randomizeOminousLevel(Raider raider, ItemStack bottle) {
		int amplifier = raider.getRandom().nextInt(5);
		bottle.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(amplifier));
	}
}
