package br.com.dropcontrol.mixin;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
	@Accessor("validBlocks")
	Set<Block> dropcontrol$getValidBlocks();

	@Accessor("validBlocks")
	@Mutable
	@Final
	void dropcontrol$setValidBlocks(Set<Block> blocks);
}
