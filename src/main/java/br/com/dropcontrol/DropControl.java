package br.com.dropcontrol;

import br.com.dropcontrol.config.DropControlConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DropControl implements ModInitializer {
	public static final String MOD_ID = "dropcontrol";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		DropControlConfig.load();
	}
}
