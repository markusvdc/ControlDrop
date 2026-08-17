package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.RedstoneConcealment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin extends Block {
	protected RedStoneWireBlockMixin(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void dropcontrol$setVisibleDefault(BlockBehaviour.Properties properties, CallbackInfo callbackInfo) {
		this.registerDefaultState(this.defaultBlockState().setValue(RedstoneConcealment.CONCEALED, false));
	}

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void dropcontrol$addConcealedProperty(
		StateDefinition.Builder<Block, BlockState> builder,
		CallbackInfo callbackInfo
	) {
		builder.add(RedstoneConcealment.CONCEALED);
	}

	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void dropcontrol$preserveConcealmentWhenConnectionsChange(
		BlockState state,
		LevelReader level,
		ScheduledTickAccess ticks,
		BlockPos pos,
		Direction directionToNeighbour,
		BlockPos neighbourPos,
		BlockState neighbourState,
		RandomSource random,
		CallbackInfoReturnable<BlockState> callbackInfo
	) {
		BlockState updatedState = callbackInfo.getReturnValue();
		if (RedstoneConcealment.isConcealed(state)
			&& updatedState.is(state.getBlock())
			&& !RedstoneConcealment.isConcealed(updatedState)) {
			callbackInfo.setReturnValue(updatedState.setValue(RedstoneConcealment.CONCEALED, true));
		}
	}

	@Inject(method = "useWithoutItem", at = @At("RETURN"), cancellable = true)
	private void dropcontrol$preserveConcealmentWhenTogglingWire(
		BlockState state,
		Level level,
		BlockPos pos,
		Player player,
		BlockHitResult hitResult,
		CallbackInfoReturnable<InteractionResult> callbackInfo
	) {
		if (RedstoneConcealment.isConcealed(state) && callbackInfo.getReturnValue().consumesAction()) {
			BlockState updatedState = level.getBlockState(pos);
			if (updatedState.is(state.getBlock()) && !RedstoneConcealment.isConcealed(updatedState)) {
				level.setBlock(pos, updatedState.setValue(RedstoneConcealment.CONCEALED, true), 3);
			}
		}
	}
}
