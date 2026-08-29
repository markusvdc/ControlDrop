package br.com.dropcontrol.mixin;

import br.com.dropcontrol.item.DropControlItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
	@Shadow
	private boolean droppedSkulls;

	@Shadow
	public abstract boolean isPowered();

	@Inject(method = "killedEntity", at = @At("HEAD"))
	private void dropcontrol$dropSpiderHead(
		ServerLevel level,
		LivingEntity entity,
		DamageSource source,
		CallbackInfoReturnable<Boolean> callback
	) {
		if (level.getGameRules().get(GameRules.MOB_DROPS)
			&& this.isPowered()
			&& !this.droppedSkulls
			&& entity.getType() == EntityTypes.SPIDER) {
			entity.spawnAtLocation(level, new ItemStack(DropControlItems.SPIDER_HEAD));
			this.droppedSkulls = true;
		}
	}
}
