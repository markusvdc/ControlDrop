package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class SkeletonEnchantmentDropPolicy {
	private static final float DROP_CHANCE = 0.25F;
	private static final Identifier SKELETON_ENCHANTMENT =
		Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_enchantment");

	private SkeletonEnchantmentDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() != EntityTypes.SKELETON || !DropControlConfig.isSelected(SKELETON_ENCHANTMENT)) {
			return;
		}

		ItemStack bow = entity.getMainHandItem();
		if (!bow.is(Items.BOW)) {
			return;
		}

		ItemEnchantments enchantments = bow.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Holder<Enchantment> enchantment : enchantments.keySet()) {
			if (!isNaturalSkeletonBowEnchantment(enchantment)) {
				continue;
			}

			float roll = entity.getRandom().nextFloat();
			if (roll < DROP_CHANCE) {
				ItemStack book = EnchantmentHelper.createBook(
					new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel())
				);
				DropRemovalPolicy.spawnConfiguredDrop(level, entity, book);
			}
		}
	}

	private static boolean isNaturalSkeletonBowEnchantment(Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.UNBREAKING)
			|| enchantment.is(Enchantments.POWER)
			|| enchantment.is(Enchantments.PUNCH)
			|| enchantment.is(Enchantments.INFINITY)
			|| enchantment.is(Enchantments.FLAME);
	}
}
