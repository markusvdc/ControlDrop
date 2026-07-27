package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DropRemovalPolicy {
	private static final ThreadLocal<Boolean> ALLOW_CONFIGURED_DROP = new ThreadLocal<>();
	private static final Identifier SKELETON_BOW =
		Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_bow");
	private static final Identifier SKELETON_ARMOR =
		Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_armor");
	private static final Identifier ZOMBIE_ARMOR =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_armor");
	private static final Identifier ZOMBIE_ORES =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_ores");
	private static final Identifier ZOMBIE_WEAPONS =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_weapons");
	private static final Identifier WITCH_ALL =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_all");

	private DropRemovalPolicy() {
	}

	public static boolean shouldRemove(LivingEntity entity, ItemStack stack) {
		if (Boolean.TRUE.equals(ALLOW_CONFIGURED_DROP.get())) {
			return false;
		}

		if (!entity.isDeadOrDying()) {
			return false;
		}

		if (entity.getType() == EntityTypes.WITCH) {
			boolean remove = DropControlConfig.isSelected(WITCH_ALL);
			if (remove) {
				DropDebugLog.removed(entity, "remove_all", stack);
			}
			return remove;
		}

		if (entity.getType() == EntityTypes.SKELETON) {
			if (DropControlConfig.isSelected(SKELETON_BOW) && stack.is(Items.BOW)) {
				DropDebugLog.removed(entity, "remove_bow", stack);
				return true;
			}

			boolean removeArmor =
				DropControlConfig.isSelected(SKELETON_ARMOR) && stack.has(DataComponents.EQUIPPABLE);
			if (removeArmor) {
				DropDebugLog.removed(entity, "remove_armor", stack);
			}
			return removeArmor;
		}

		if (entity.getType() != EntityTypes.ZOMBIE) {
			return false;
		}

		if (DropControlConfig.isSelected(ZOMBIE_ARMOR) && stack.has(DataComponents.EQUIPPABLE)) {
			DropDebugLog.removed(entity, "remove_armor", stack);
			return true;
		}

		if (DropControlConfig.isSelected(ZOMBIE_ORES) && stack.is(Items.IRON_INGOT)) {
			DropDebugLog.removed(entity, "remove_vanilla_iron_ingot", stack);
			return true;
		}

		boolean removeWeapon = DropControlConfig.isSelected(ZOMBIE_WEAPONS)
			&& (stack.is(Items.IRON_SWORD)
				|| stack.is(Items.IRON_SPEAR)
				|| stack.is(Items.IRON_SHOVEL));
		if (removeWeapon) {
			DropDebugLog.removed(entity, "remove_weapons", stack);
		}
		return removeWeapon;
	}

	public static void spawnConfiguredDrop(ServerLevel level, LivingEntity entity, ItemStack stack) {
		Boolean previousValue = ALLOW_CONFIGURED_DROP.get();
		ALLOW_CONFIGURED_DROP.set(true);
		try {
			entity.spawnAtLocation(level, stack);
		} finally {
			if (previousValue == null) {
				ALLOW_CONFIGURED_DROP.remove();
			} else {
				ALLOW_CONFIGURED_DROP.set(previousValue);
			}
		}
	}
}
