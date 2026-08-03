package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.DropControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;

public final class PhantomDebugLog {
	private PhantomDebugLog() {
	}

	public static void spawnGroup(int originalGroupSize, int modifiedGroupSize) {
		DropControl.LOGGER.info(
			"[PHANTOM DEBUG] event=spawn_group original_group={} modified_group={} rest_threshold_ticks=48000 movement_multiplier=1.20 damage_multiplier=3.00",
			originalGroupSize,
			modifiedGroupSize
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
