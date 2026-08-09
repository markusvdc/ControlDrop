package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DropRemovalPolicy {
	private static final ThreadLocal<Boolean> ALLOW_CONFIGURED_DROP = new ThreadLocal<>();
	private static final Identifier SKELETON_ARMOR =
		Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_armor");
	private static final Identifier PILLAGER_CROSSBOW =
		Identifier.fromNamespaceAndPath("dropcontrol", "pillager_crossbow");
	private static final Identifier ZOMBIE_ARMOR =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_armor");
	private static final Identifier DROWNED_COPPER =
		Identifier.fromNamespaceAndPath("dropcontrol", "drowned_copper");
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
			return remove;
		}

		if (entity.getType() == EntityTypes.SKELETON) {
			if (isPreservedEquipment(entity, stack)) {
				return false;
			}

			if (DropControlConfig.isSelected(SKELETON_ARMOR) && stack.is(Items.BOW)) {
				return true;
			}

			boolean removeArmor =
				DropControlConfig.isSelected(SKELETON_ARMOR) && stack.has(DataComponents.EQUIPPABLE);
			return removeArmor;
		}

		if (entity.getType() == EntityTypes.PILLAGER) {
			if (isPreservedEquipment(entity, stack)) {
				return false;
			}

			boolean remove =
				DropControlConfig.isSelected(PILLAGER_CROSSBOW) && stack.is(Items.CROSSBOW);
			return remove;
		}

		if (entity.getType() == EntityTypes.DROWNED) {
			return DropControlConfig.isSelected(DROWNED_COPPER) && stack.is(Items.COPPER_INGOT);
		}

		if (entity.getType() != EntityTypes.ZOMBIE) {
			return false;
		}

		if (isPreservedEquipment(entity, stack)) {
			return false;
		}

		if (DropControlConfig.isSelected(ZOMBIE_ARMOR)) {
			if (stack.has(DataComponents.EQUIPPABLE)) {
				return true;
			}

			if (stack.is(Items.POTATO) || stack.is(Items.BAKED_POTATO) || stack.is(Items.CARROT)) {
				return true;
			}

			if (stack.is(Items.IRON_INGOT)) {
				return true;
			}
		}

		boolean removeWeapon = DropControlConfig.isSelected(ZOMBIE_ARMOR)
			&& (stack.is(Items.IRON_SWORD)
				|| stack.is(Items.IRON_SPEAR)
				|| stack.is(Items.IRON_SHOVEL));
		return removeWeapon;
	}

	private static boolean isPreservedEquipment(LivingEntity entity, ItemStack stack) {
		if (!(entity instanceof Mob mob)) {
			return false;
		}

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (mob.getItemBySlot(slot) == stack && mob.getDropChances().isPreserved(slot)) {
				return true;
			}
		}

		return false;
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
