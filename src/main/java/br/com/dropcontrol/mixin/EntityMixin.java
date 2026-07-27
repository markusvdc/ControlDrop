package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.DropRemovalPolicy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
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
		if ((Object)this instanceof LivingEntity livingEntity
			&& DropRemovalPolicy.shouldRemove(livingEntity, stack)) {
			callback.setReturnValue(null);
		}
	}
}
