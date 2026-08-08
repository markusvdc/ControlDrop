package br.com.dropcontrol;

import br.com.dropcontrol.config.DropControlConfig;
import net.fabricmc.api.ModInitializer;

public final class DropControl implements ModInitializer {
	public static final String MOD_ID = "dropcontrol";

	@Override
	public void onInitialize() {
		DropControlConfig.load();
	}
}
