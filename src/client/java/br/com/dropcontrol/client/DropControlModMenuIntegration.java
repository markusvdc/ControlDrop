package br.com.dropcontrol.client;

import br.com.dropcontrol.client.screen.DropControlScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class DropControlModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return DropControlScreen::new;
	}
}
