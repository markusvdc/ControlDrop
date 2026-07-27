package br.com.dropcontrol.client.screen.component;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class ActionButtons {
	private static final int GAP = 8;

	private final HoverOnlyButton backButton;
	private final HoverOnlyButton optionsButton;
	private final HoverOnlyButton applyButton;
	private final HoverOnlyButton selectAllButton;

	public ActionButtons(
		int x,
		int y,
		int width,
		Runnable onBack,
		Runnable onOptions,
		Runnable onToggleAll,
		Runnable onApply,
		boolean silentOptions
	) {
		int buttonWidth = (width - GAP * 3) / 4;
		int applyWidth = width - buttonWidth * 3 - GAP * 3;

		this.backButton = new HoverOnlyButton(
			x, y, buttonWidth, 20, Component.translatable("dropcontrol.action.back"), button -> onBack.run()
		);
		this.optionsButton = new HoverOnlyButton(
			x + buttonWidth + GAP,
			y,
			buttonWidth,
			20,
			Component.translatable("dropcontrol.action.options"),
			button -> onOptions.run(),
			silentOptions
		);
		this.selectAllButton = new HoverOnlyButton(
			x + buttonWidth * 2 + GAP * 2,
			y,
			buttonWidth,
			20,
			accentedLabel("dropcontrol.action.toggle_all", ChatFormatting.GOLD),
			button -> onToggleAll.run()
		);
		this.selectAllButton.setTooltip(Tooltip.create(Component.translatable("dropcontrol.action.toggle_all.tooltip")));
		this.applyButton = new HoverOnlyButton(
			x + buttonWidth * 3 + GAP * 3,
			y,
			applyWidth,
			20,
			accentedLabel("dropcontrol.action.apply", ChatFormatting.GREEN),
			button -> onApply.run()
		);
	}

	public void addTo(Consumer<HoverOnlyButton> addWidget) {
		addWidget.accept(this.backButton);
		addWidget.accept(this.optionsButton);
		addWidget.accept(this.selectAllButton);
		addWidget.accept(this.applyButton);
	}

	private static Component accentedLabel(String translationKey, ChatFormatting color) {
		return Component.empty()
			.append(Component.literal("■ ").withStyle(color))
			.append(Component.translatable(translationKey));
	}
}
