package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CreeperTntDropPolicy {
	private static final float DROP_CHANCE = 0.10F;
	private static final Identifier CREEPER_TNT =
		Identifier.fromNamespaceAndPath("dropcontrol", "creeper_tnt");

	private CreeperTntDropPolicy() {
	}

	public static void tryDrop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() != EntityTypes.CREEPER || !DropControlConfig.isSelected(CREEPER_TNT)) {
			return;
		}

		float roll = entity.getRandom().nextFloat();
		DropDebugLog.chance(entity, "add_tnt", roll, DROP_CHANCE);
		if (roll >= DROP_CHANCE) {
			return;
		}

		ItemStack stack = new ItemStack(Items.TNT);
		DropDebugLog.added(entity, "add_tnt", stack);
		DropRemovalPolicy.spawnConfiguredDrop(level, entity, stack);
	}
}
