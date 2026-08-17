package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.gameplay.RedstoneConcealment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.GlowInkSacItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class GlowInkSacItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$concealRedstone(
		UseOnContext context,
		CallbackInfoReturnable<InteractionResult> callbackInfo
	) {
		if (!((Object)this instanceof GlowInkSacItem) || !DropControlConfig.arcaneRedstone()) {
			return;
		}

		Level level = context.getLevel();
		BlockState state = level.getBlockState(context.getClickedPos());
		if (!RedstoneConcealment.supports(state) || RedstoneConcealment.isConcealed(state)) {
			return;
		}

		if (!level.isClientSide()) {
			Player player = context.getPlayer();
			ItemStack stack = context.getItemInHand();
			level.setBlock(context.getClickedPos(), state.setValue(RedstoneConcealment.CONCEALED, true), 3);
			level.playSound(null, context.getClickedPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (player != null) {
				player.awardStat(Stats.ITEM_USED.get((Item)(Object)this));
				if (!player.getAbilities().instabuild) {
					stack.shrink(1);
				}
			}
		}
		callbackInfo.setReturnValue(InteractionResult.SUCCESS);
	}
}
