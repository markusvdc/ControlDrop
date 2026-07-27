package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.OminousBottleAmplifier;

public final class WitchPotionDropPolicy {
	private static final Identifier WITCH_POTIONS =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_potions");

	private WitchPotionDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() != EntityTypes.WITCH || !DropControlConfig.isSelected(WITCH_POTIONS)) {
			return;
		}

		ItemStack potion = switch (entity.getRandom().nextInt(4)) {
			case 0 -> ominousBottle(entity);
			case 1 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_NIGHT_VISION);
			case 2 -> PotionContents.createItemStack(Items.POTION, Potions.LONG_FIRE_RESISTANCE);
			default -> PotionContents.createItemStack(Items.POTION, Potions.LONG_WATER_BREATHING);
		};
		DropDebugLog.added(entity, "add_potions", potion);
		DropRemovalPolicy.spawnConfiguredDrop(level, entity, potion);
	}

	private static ItemStack ominousBottle(LivingEntity entity) {
		ItemStack bottle = new ItemStack(Items.OMINOUS_BOTTLE);
		int amplifier = entity.getRandom().nextInt(5);
		bottle.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(amplifier));
		return bottle;
	}
}
