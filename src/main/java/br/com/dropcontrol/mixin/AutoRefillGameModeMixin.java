package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.AutoRefill;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerGameMode.class)
public abstract class AutoRefillGameModeMixin {
	@WrapMethod(method = "useItemOn")
	private InteractionResult dropcontrol$refillAfterBlockUse(ServerPlayer player, Level level,
		ItemStack stack, InteractionHand hand, BlockHitResult hit, Operation<InteractionResult> original) {
		var attempt = AutoRefill.capture(player, hand);
		InteractionResult result = original.call(player, level, stack, hand, hit);
		if (result.consumesAction()) {
			AutoRefill.consumed(player, attempt);
		}
		return result;
	}

	@WrapMethod(method = "useItem")
	private InteractionResult dropcontrol$refillAfterItemUse(ServerPlayer player, Level level,
		ItemStack stack, InteractionHand hand, Operation<InteractionResult> original) {
		var attempt = AutoRefill.capture(player, hand);
		InteractionResult result = original.call(player, level, stack, hand);
		if (result.consumesAction()) {
			AutoRefill.consumed(player, attempt);
		}
		return result;
	}
}
