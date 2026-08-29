package br.com.dropcontrol;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.block.DropControlBlocks;
import br.com.dropcontrol.item.DropControlItems;
import net.fabricmc.api.ModInitializer;

public final class DropControl implements ModInitializer {
	public static final String MOD_ID = "dropcontrol";

	@Override
	public void onInitialize() {
		DropControlBlocks.initialize();
		DropControlItems.initialize();
		DropControlConfig.load();
	}
}
