package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class OreDropPolicy {
	private static final float DROP_CHANCE = 0.30F;
	private static final Map<EntityType<?>, Identifier> MARKERS = Map.of(
		EntityTypes.SKELETON, Identifier.fromNamespaceAndPath("dropcontrol", "skeleton_ores"),
		EntityTypes.ZOMBIE, Identifier.fromNamespaceAndPath("dropcontrol", "zombie_ores"),
		EntityTypes.SPIDER, Identifier.fromNamespaceAndPath("dropcontrol", "spider_ores"),
		EntityTypes.CREEPER, Identifier.fromNamespaceAndPath("dropcontrol", "creeper_ores")
	);
	private static final Item[] ORES = {
		Items.RAW_IRON,
		Items.RAW_COPPER,
		Items.RAW_GOLD,
		Items.DIAMOND
	};

	private OreDropPolicy() {
	}

	public static void tryDrop(ServerLevel level, LivingEntity entity) {
		Identifier marker = MARKERS.get(entity.getType());
		if (marker == null || !DropControlConfig.isSelected(marker)) {
			return;
		}

		float roll = entity.getRandom().nextFloat();
		DropDebugLog.chance(entity, "add_ores", roll, DROP_CHANCE);
		if (roll >= DROP_CHANCE) {
			return;
		}

		Item selectedOre = ORES[entity.getRandom().nextInt(ORES.length)];
		ItemStack stack = new ItemStack(selectedOre);
		DropDebugLog.added(entity, "add_ores", stack);
		DropRemovalPolicy.spawnConfiguredDrop(level, entity, stack);
	}
}
