package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class WitchPotionDropPolicy {
	private static final Identifier WITCH_POTIONS =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_potions");

	private WitchPotionDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() != EntityTypes.WITCH || !DropControlConfig.isSelected(WITCH_POTIONS)) {
			return;
		}

		for (int draw = 0; draw < 2; draw++) {
			ItemStack potion = randomPotion(entity);
			DropDebugLog.added(entity, "add_potions", potion);
			DropRemovalPolicy.spawnConfiguredDrop(level, entity, potion);
		}
	}

	private static ItemStack randomPotion(LivingEntity entity) {
		return switch (entity.getRandom().nextInt(9)) {
			case 0 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_FIRE_RESISTANCE);
			case 1 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_SWIFTNESS);
			case 2 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_LEAPING);
			case 3 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_STRENGTH);
			case 4 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_NIGHT_VISION);
			case 5 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_WATER_BREATHING);
			case 6 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_SLOW_FALLING);
			case 7 -> PotionContents.createItemStack(Items.POTION, Potions.STRONG_POISON);
			default -> PotionContents.createItemStack(Items.POTION, Potions.STRONG_REGENERATION);
		};
	}
}
