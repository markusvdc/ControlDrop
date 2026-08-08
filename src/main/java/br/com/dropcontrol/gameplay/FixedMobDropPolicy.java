package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;

public final class FixedMobDropPolicy {
	private static final float COBWEB_CHANCE = 0.15F;
	private static final float SPECTRAL_ARROW_CHANCE = 0.20F;
	private static final float ZOMBIE_SULFUR_CHANCE = 0.30F;
	private static final float ZOMBIE_POISONOUS_POTATO_CHANCE = 0.35F;
	private static final float ENDERMAN_AMETHYST_SHARD_CHANCE = 0.50F;
	private static final Identifier SPIDER_COBWEB =
		Identifier.fromNamespaceAndPath("dropcontrol", "spider_cobweb");
	private static final Identifier PILLAGER_WEALTH =
		Identifier.fromNamespaceAndPath("dropcontrol", "pillager_wealth");
	private static final Identifier SKELETON_SPECTRAL_ARROW =
		Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_spectral_arrow");
	private static final Identifier ZOMBIE_SULFUR =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_sulfur");
	private static final Identifier ZOMBIE_POISONOUS_POTATO =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_poisonous_potato");
	private static final Identifier ENDERMAN_AMETHYST_SHARD =
		Identifier.fromNamespaceAndPath("dropcontrol", "enderman_amethyst_shard");
	private static final Identifier WITCH_FIREWORK_ROCKET =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_firework_rocket");

	private FixedMobDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() == EntityTypes.WITCH && DropControlConfig.isSelected(WITCH_FIREWORK_ROCKET)) {
			ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
			rocket.set(DataComponents.FIREWORKS, new Fireworks(1, List.of()));
			spawn(level, entity, rocket);
		} else if (entity.getType() == EntityTypes.SPIDER && DropControlConfig.isSelected(SPIDER_COBWEB)) {
			float roll = entity.getRandom().nextFloat();
			if (roll < COBWEB_CHANCE) {
				spawn(level, entity, new ItemStack(Items.COBWEB));
			}
		} else if (entity.getType() == EntityTypes.PILLAGER && DropControlConfig.isSelected(PILLAGER_WEALTH)) {
			for (int rollIndex = 0; rollIndex < 2; rollIndex++) {
				float roll = entity.getRandom().nextFloat();
				ItemStack stack = roll < 0.50F
					? new ItemStack(Items.EMERALD)
					: roll < 0.90F
						? new ItemStack(Items.GOLD_INGOT)
						: new ItemStack(Items.DIAMOND);
				spawn(level, entity, stack);
			}
		} else if (entity.getType() == EntityTypes.SKELETON
			&& DropControlConfig.isSelected(SKELETON_SPECTRAL_ARROW)) {
			float roll = entity.getRandom().nextFloat();
			if (roll < SPECTRAL_ARROW_CHANCE) {
				spawn(level, entity, new ItemStack(Items.SPECTRAL_ARROW));
			}
		} else if (entity.getType() == EntityTypes.ZOMBIE) {
			dropZombieItems(level, entity);
		} else if (entity.getType() == EntityTypes.ENDERMAN
			&& DropControlConfig.isSelected(ENDERMAN_AMETHYST_SHARD)) {
			float roll = entity.getRandom().nextFloat();
			if (roll < ENDERMAN_AMETHYST_SHARD_CHANCE) {
				spawn(level, entity, new ItemStack(Items.AMETHYST_SHARD));
			}
		}
	}

	private static void dropZombieItems(ServerLevel level, LivingEntity entity) {
		if (DropControlConfig.isSelected(ZOMBIE_SULFUR)) {
			float roll = entity.getRandom().nextFloat();
			if (roll < ZOMBIE_SULFUR_CHANCE) {
				spawn(level, entity, new ItemStack(Items.SULFUR));
			}
		}

		if (DropControlConfig.isSelected(ZOMBIE_POISONOUS_POTATO)) {
			float roll = entity.getRandom().nextFloat();
			if (roll < ZOMBIE_POISONOUS_POTATO_CHANCE) {
				spawn(level, entity, new ItemStack(Items.POISONOUS_POTATO, 1 + entity.getRandom().nextInt(2)));
			}
		}
	}

	private static void spawn(ServerLevel level, LivingEntity entity, ItemStack stack) {
		DropRemovalPolicy.spawnConfiguredDrop(level, entity, stack);
	}
}
