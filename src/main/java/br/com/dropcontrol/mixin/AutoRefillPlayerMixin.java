package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.AutoRefill;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class AutoRefillPlayerMixin {
	@WrapMethod(method = "interactOn")
	private InteractionResult dropcontrol$refillAfterEntityUse(Entity target, InteractionHand hand,
		Vec3 hit, Operation<InteractionResult> original) {
		ServerPlayer player = (Object) this instanceof ServerPlayer serverPlayer ? serverPlayer : null;
		var attempt = player != null ? AutoRefill.capture(player, hand) : null;
		InteractionResult result = original.call(target, hand, hit);
		if (result.consumesAction() && player != null) {
			AutoRefill.consumed(player, attempt);
		}
		return result;
	}
}
