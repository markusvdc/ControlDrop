package br.com.dropcontrol.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;

public final class PhantomPurge {
	private PhantomPurge() {
	}

	public static void killAllLoaded(Minecraft minecraft) {
		MinecraftServer server = minecraft.getSingleplayerServer();
		if (server == null) {
			return;
		}

		server.execute(() -> {
			for (ServerLevel level : server.getAllLevels()) {
				List<Phantom> phantoms = new ArrayList<>();
				for (Entity entity : level.getAllEntities()) {
					if (entity instanceof Phantom phantom && phantom.isAlive()) {
						phantoms.add(phantom);
					}
				}
				for (Phantom phantom : phantoms) {
					phantom.kill(level);
				}
			}
		});
	}
}
