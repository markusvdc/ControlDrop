package br.com.dropcontrol.gameplay;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class RedstoneConcealment {
	public static final BooleanProperty CONCEALED = BooleanProperty.create("dropcontrol_concealed_v2");

	private RedstoneConcealment() {
	}

	public static boolean supports(BlockState state) {
		return state.is(Blocks.REDSTONE_WIRE)
			|| state.is(Blocks.REDSTONE_TORCH)
			|| state.is(Blocks.REDSTONE_WALL_TORCH);
	}

	public static boolean isConcealed(BlockState state) {
		return state.hasProperty(CONCEALED) && state.getValue(CONCEALED);
	}
}
