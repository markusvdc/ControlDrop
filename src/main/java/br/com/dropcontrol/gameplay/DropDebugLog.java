package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.DropControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class DropDebugLog {
	private DropDebugLog() {
	}

	public static void chance(LivingEntity entity, String rule, float roll, float requiredChance) {
		DropControl.LOGGER.info(
			"[DROP DEBUG] entity={} rule={} roll={} required={} result={}",
			entityId(entity),
			rule,
			percent(roll),
			percent(requiredChance),
			roll < requiredChance ? "success" : "no_drop"
		);
	}

	public static void added(LivingEntity entity, String rule, ItemStack stack) {
		DropControl.LOGGER.info(
			"[DROP DEBUG] entity={} rule={} added={} count={} components={}",
			entityId(entity),
			rule,
			BuiltInRegistries.ITEM.getKey(stack.getItem()),
			stack.getCount(),
			stack.getComponents()
		);
	}

	public static void removed(LivingEntity entity, String rule, ItemStack stack) {
		DropControl.LOGGER.info(
			"[DROP DEBUG] entity={} rule={} removed={} count={}",
			entityId(entity),
			rule,
			BuiltInRegistries.ITEM.getKey(stack.getItem()),
			stack.getCount()
		);
	}

	private static String entityId(LivingEntity entity) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
	}

	private static String percent(float value) {
		return String.format(java.util.Locale.ROOT, "%.2f%%", value * 100.0F);
	}
}
