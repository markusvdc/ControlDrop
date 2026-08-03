package br.com.dropcontrol.access;

import net.minecraft.world.entity.player.Player;

public interface PhantomBiteAccess {
	boolean dropcontrol$hasBittenPlayer();

	void dropcontrol$markPlayerBitten(Player player);
}
