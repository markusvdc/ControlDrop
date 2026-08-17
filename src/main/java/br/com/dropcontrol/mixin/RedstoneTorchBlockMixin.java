package br.com.dropcontrol.mixin;

import br.com.dropcontrol.gameplay.RedstoneConcealment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneTorchBlock.class)
public abstract class RedstoneTorchBlockMixin extends Block {
	protected RedstoneTorchBlockMixin(BlockBehaviour.Properties properties) {
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
}
