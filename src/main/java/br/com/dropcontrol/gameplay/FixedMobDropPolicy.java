package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import br.com.dropcontrol.item.DropControlItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FixedMobDropPolicy {
	private static final float ZOMBIE_SULFUR_CHANCE = 0.30F;
	private static final float ZOMBIE_POISONOUS_POTATO_CHANCE = 0.35F;
	private static final float DROWNED_KELP_CHANCE = 0.50F;
	private static final float ENDERMAN_AMETHYST_SHARD_CHANCE = 0.50F;
	private static final float PHANTOM_GLOW_INK_SAC_CHANCE = 0.50F;
	private static final Identifier PILLAGER_WEALTH =
		Identifier.fromNamespaceAndPath("dropcontrol", "pillager_wealth");
	private static final Identifier ZOMBIE_SULFUR =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_sulfur");
	private static final Identifier ZOMBIE_POISONOUS_POTATO =
		Identifier.fromNamespaceAndPath("dropcontrol", "zombie_poisonous_potato");
	private static final Identifier DROWNED_TROPICAL_FISH =
		Identifier.fromNamespaceAndPath("dropcontrol", "drowned_tropical_fish");
	private static final Identifier DROWNED_KELP =
		Identifier.fromNamespaceAndPath("dropcontrol", "drowned_kelp");
	private static final Identifier ENDERMAN_AMETHYST_SHARD =
		Identifier.fromNamespaceAndPath("dropcontrol", "enderman_amethyst_shard");
	private static final Identifier ENDERMAN_PARTICLES =
		Identifier.fromNamespaceAndPath("dropcontrol", "enderman_particles");
	private static final Identifier WITCH_WART =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_wart");
	private static final Identifier WARDEN_HORN =
		Identifier.fromNamespaceAndPath("dropcontrol", "warden_horn");
	private static final Identifier PILLAGER_APPLE =
		Identifier.fromNamespaceAndPath("dropcontrol", "pillager_apple");
	private static final Identifier PHANTOM_GLOW_INK_SAC =
		Identifier.fromNamespaceAndPath("dropcontrol", "phantom_glow_ink_sac");

	private FixedMobDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() == EntityTypes.WITCH) {
			dropWitchItems(level, entity);
		} else if (entity.getType() == EntityTypes.WARDEN
			&& DropControlConfig.isSelected(WARDEN_HORN)) {
			spawn(level, entity, new ItemStack(DropControlItems.WARDEN_HORN));
		} else if (entity.getType() == EntityTypes.PILLAGER) {
			dropPillagerItems(level, entity);
		} else if (entity.getType() == EntityTypes.ZOMBIE) {
			dropZombieItems(level, entity);
		} else if (entity.getType() == EntityTypes.DROWNED) {
			dropDrownedItems(level, entity);
		} else if (entity.getType() == EntityTypes.ENDERMAN) {
			dropEndermanItems(level, entity);
		} else if (entity.getType() == EntityTypes.PHANTOM
			&& DropControlConfig.isSelected(PHANTOM_GLOW_INK_SAC)
			&& entity.getRandom().nextFloat() < PHANTOM_GLOW_INK_SAC_CHANCE) {
			spawn(level, entity, new ItemStack(Items.GLOW_INK_SAC));
		}
	}

	private static void dropEndermanItems(ServerLevel level, LivingEntity entity) {
		if (DropControlConfig.isSelected(ENDERMAN_PARTICLES)) {
			spawn(level, entity, new ItemStack(DropControlItems.ENDERMAN_PARTICLES));
		}

		if (DropControlConfig.isSelected(ENDERMAN_AMETHYST_SHARD)
			&& entity.getRandom().nextFloat() < ENDERMAN_AMETHYST_SHARD_CHANCE) {
			spawn(level, entity, new ItemStack(Items.AMETHYST_SHARD));
		}
	}

	private static void dropWitchItems(ServerLevel level, LivingEntity entity) {
		if (DropControlConfig.isSelected(WITCH_WART)) {
			spawn(level, entity, new ItemStack(DropControlItems.WITCH_WART));
		}
	}

	private static void dropPillagerItems(ServerLevel level, LivingEntity entity) {
		if (DropControlConfig.isSelected(PILLAGER_WEALTH)) {
			for (int rollIndex = 0; rollIndex < 2; rollIndex++) {
				float roll = entity.getRandom().nextFloat();
				ItemStack stack = roll < 0.50F
					? new ItemStack(Items.EMERALD)
					: roll < 0.90F
						? new ItemStack(Items.GOLD_INGOT)
						: new ItemStack(Items.DIAMOND);
				spawn(level, entity, stack);
			}
		}

		if (DropControlConfig.isSelected(PILLAGER_APPLE)) {
			spawn(level, entity, new ItemStack(Items.APPLE));
		}
	}

	private static void dropDrownedItems(ServerLevel level, LivingEntity entity) {
		if (DropControlConfig.isSelected(DROWNED_TROPICAL_FISH)) {
			spawn(level, entity, new ItemStack(Items.TROPICAL_FISH, 1 + entity.getRandom().nextInt(2)));
		}

		if (DropControlConfig.isSelected(DROWNED_KELP)
			&& entity.getRandom().nextFloat() < DROWNED_KELP_CHANCE) {
			spawn(level, entity, new ItemStack(Items.KELP));
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
