package br.com.dropcontrol.gameplay;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;

public final class WitchSuspiciousStewDropPolicy {
	private static final Identifier WITCH_SUSPICIOUS_STEW =
		Identifier.fromNamespaceAndPath("dropcontrol", "witch_suspicious_stew");
	private static final List<StewEffect> EFFECTS = List.of(
		new StewEffect(MobEffects.SATURATION, 7),
		new StewEffect(MobEffects.NIGHT_VISION, 100),
		new StewEffect(MobEffects.FIRE_RESISTANCE, 60),
		new StewEffect(MobEffects.REGENERATION, 140),
		new StewEffect(MobEffects.JUMP_BOOST, 100),
		new StewEffect(MobEffects.WEAKNESS, 140),
		new StewEffect(MobEffects.POISON, 220),
		new StewEffect(MobEffects.BLINDNESS, 220),
		new StewEffect(MobEffects.WITHER, 140),
		new StewEffect(MobEffects.NAUSEA, 140)
	);

	private WitchSuspiciousStewDropPolicy() {
	}

	public static void drop(ServerLevel level, LivingEntity entity) {
		if (entity.getType() != EntityTypes.WITCH || !DropControlConfig.isSelected(WITCH_SUSPICIOUS_STEW)) {
			return;
		}

		int quantity = 1 + entity.getRandom().nextInt(2);
		for (int draw = 0; draw < quantity; draw++) {
			StewEffect selectedEffect = EFFECTS.get(entity.getRandom().nextInt(EFFECTS.size()));
			ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
			stew.set(
				DataComponents.SUSPICIOUS_STEW_EFFECTS,
				new SuspiciousStewEffects(List.of(selectedEffect.toEntry()))
			);
			DropRemovalPolicy.spawnConfiguredDrop(level, entity, stew);
		}
	}

	private record StewEffect(Holder<MobEffect> effect, int duration) {
		private SuspiciousStewEffects.Entry toEntry() {
			return new SuspiciousStewEffects.Entry(effect, duration);
		}
	}
}
