package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
	private static final int TARGET_SPAWN_WEIGHT_MULTIPLIER = 6;

	@Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
	private static void dropcontrol$increaseTraditionalMonsterSpawnWeights(
		CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> callback
	) {
		if (!DropControlConfig.traditionalMonsterSpawns()) {
			return;
		}

		WeightedList.Builder<MobSpawnSettings.SpawnerData> modifiedSpawns = WeightedList.builder();
		for (Weighted<MobSpawnSettings.SpawnerData> entry : callback.getReturnValue().unwrap()) {
			EntityType<?> type = entry.value().type();
			int multiplier = type == EntityTypes.WITCH || type == EntityTypes.ENDERMAN
				? TARGET_SPAWN_WEIGHT_MULTIPLIER
				: 1;
			modifiedSpawns.add(entry.value(), entry.weight() * multiplier);
		}
		callback.setReturnValue(modifiedSpawns.build());
	}
}
