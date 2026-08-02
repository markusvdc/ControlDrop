package br.com.dropcontrol.compat.jade;

import br.com.dropcontrol.config.DropControlConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Horse;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

final class ExactHorseHealthProvider implements IEntityComponentProvider {
	static final ExactHorseHealthProvider INSTANCE = new ExactHorseHealthProvider();
	private static final Identifier UID = Identifier.fromNamespaceAndPath("dropcontrol", "exact_horse_health");

	private ExactHorseHealthProvider() {
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!DropControlConfig.exactHorseHealth()) {
			return;
		}

		Horse horse = (Horse)accessor.getEntity();
		tooltip.add(Component.translatable(
			"dropcontrol.jade.horse_health",
			IThemeHelper.get().info(formatHealth(horse.getHealth())),
			formatHealth(horse.getMaxHealth())
		));
	}

	private static String formatHealth(float value) {
		return new BigDecimal(Float.toString(value)).setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	@Override
	public Identifier getUid() {
		return UID;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1;
	}
}
