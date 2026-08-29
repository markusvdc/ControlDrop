package br.com.dropcontrol.block;

import net.minecraft.world.level.block.SkullBlock;

public enum SpiderSkullType implements SkullBlock.Type {
	INSTANCE;

	private static final String NAME = "dropcontrol:spider";

	@Override
	public String getSerializedName() {
		return NAME;
	}

	public static void initialize() {
		SkullBlock.Type.TYPES.put(NAME, INSTANCE);
	}
}
