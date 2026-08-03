package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.DropControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;

public final class PhantomDebugLog {
	private PhantomDebugLog() {
	}

	public static void spawnGroup(
		int originalGroupSize,
		int modifiedGroupSize,
		boolean predatorPressure
	) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=spawn_group original_group={} modified_group={} rest_threshold_ticks=48000 movement_multiplier=1.20 damage_multiplier={} sound_multiplier={} daylight_immune={}",
			originalGroupSize,
			modifiedGroupSize,
			predatorPressure ? "4.00" : "1.00",
			predatorPressure ? "1.50" : "1.00",
			predatorPressure
		);
	}

	public static void playerBitten(Phantom phantom, Player player) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=player_bitten phantom_uuid={} player_uuid={} membrane_drop_chance=0.00%",
			phantom.getStringUUID(),
			player.getStringUUID()
		);
	}

	public static void membraneRemoved(Phantom phantom, int count) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=membrane_removed phantom_uuid={} removed_count={}",
			phantom.getStringUUID(),
			count
		);
	}

	public static void purged(int count) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=pressure_two_disabled living_phantoms_killed={}",
			count
		);
	}

	public static void bite(Phantom phantom, LivingEntity target, float originalDamage, float modifiedDamage) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=bite phantom_uuid={} target={} target_uuid={} original_damage={} modified_damage={}",
			phantom.getStringUUID(),
			BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()),
			target.getStringUUID(),
			originalDamage,
			modifiedDamage
		);
	}
}
