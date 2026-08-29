package br.com.dropcontrol.block;

import br.com.dropcontrol.DropControl;
import br.com.dropcontrol.mixin.BlockEntityTypeAccessor;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class DropControlBlocks {
	private static final Identifier SPIDER_HEAD_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "spider_head");
	private static final ResourceKey<Block> SPIDER_HEAD_KEY =
		ResourceKey.create(Registries.BLOCK, SPIDER_HEAD_ID);
	private static final Identifier SPIDER_WALL_HEAD_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "spider_wall_head");
	private static final ResourceKey<Block> SPIDER_WALL_HEAD_KEY =
		ResourceKey.create(Registries.BLOCK, SPIDER_WALL_HEAD_ID);

	public static final Block SPIDER_HEAD = Registry.register(
		BuiltInRegistries.BLOCK,
		SPIDER_HEAD_KEY,
		new SkullBlock(
			SpiderSkullType.INSTANCE,
			BlockBehaviour.Properties.of()
				.setId(SPIDER_HEAD_KEY)
				.strength(1.0F)
				.pushReaction(PushReaction.DESTROY)
				.noOcclusion()
		)
	);
	public static final Block SPIDER_WALL_HEAD = Registry.register(
		BuiltInRegistries.BLOCK,
		SPIDER_WALL_HEAD_KEY,
		new WallSkullBlock(
			SpiderSkullType.INSTANCE,
			BlockBehaviour.Properties.of()
				.setId(SPIDER_WALL_HEAD_KEY)
				.strength(1.0F)
				.pushReaction(PushReaction.DESTROY)
				.noOcclusion()
		)
	);

	private DropControlBlocks() {
	}

	public static void initialize() {
		SpiderSkullType.initialize();
		BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor)(Object)BlockEntityTypes.SKULL;
		HashSet<Block> supportedBlocks = new HashSet<>(accessor.dropcontrol$getValidBlocks());
		supportedBlocks.add(SPIDER_HEAD);
		supportedBlocks.add(SPIDER_WALL_HEAD);
		accessor.dropcontrol$setValidBlocks(Set.copyOf(supportedBlocks));
	}
}
