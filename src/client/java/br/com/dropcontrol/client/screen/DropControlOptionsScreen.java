package br.com.dropcontrol.client.screen;

import br.com.dropcontrol.client.screen.component.ActionButtons;
import br.com.dropcontrol.client.screen.component.DropBasePanel;
import br.com.dropcontrol.client.screen.component.GlobalOptionEntry;
import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DropControlOptionsScreen extends Screen {
	private static final int MAX_CONTENT_WIDTH = 540;
	private static final int SIDE_MARGIN = 16;
	private static final int OPTIONS_TOP = 137;
	private static final int OPTION_HEIGHT = 30;

	private final Screen parent;
	private final DropBasePanel basePanel = new DropBasePanel();
	private GlobalOptionEntry optionOneEntry;
	private GlobalOptionEntry optionTwoEntry;
	private GlobalOptionEntry optionThreeEntry;
	private GlobalOptionEntry optionFourEntry;
	private boolean optionOne;
	private boolean optionTwo;
	private boolean optionThree;
	private boolean optionFour;
	private Component status = Component.empty();
	private int statusColor = 0xFF9CD67A;

	public DropControlOptionsScreen(Screen parent) {
		super(Component.translatable("dropcontrol.options.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(MAX_CONTENT_WIDTH, this.width - SIDE_MARGIN * 2);
		int left = (this.width - contentWidth) / 2;
		this.optionOne = DropControlConfig.optionOne();
		this.optionTwo = DropControlConfig.optionTwo();
		this.optionThree = DropControlConfig.optionThree();
		this.optionFour = DropControlConfig.optionFour();

		this.optionOneEntry = new GlobalOptionEntry(
			left,
			OPTIONS_TOP,
			contentWidth,
			OPTION_HEIGHT,
			Component.translatable("dropcontrol.options.one"),
			Component.translatable("dropcontrol.options.one.description"),
			this.optionOne,
			selected -> this.optionOne = selected
		);
		this.addRenderableWidget(this.optionOneEntry);
		this.optionTwoEntry = new GlobalOptionEntry(
			left,
			OPTIONS_TOP + OPTION_HEIGHT,
			contentWidth,
			OPTION_HEIGHT,
			Component.translatable("dropcontrol.options.two"),
			Component.translatable("dropcontrol.options.two.description"),
			this.optionTwo,
			selected -> this.optionTwo = selected
		);
		this.addRenderableWidget(this.optionTwoEntry);
		this.optionThreeEntry = new GlobalOptionEntry(
			left,
			OPTIONS_TOP + OPTION_HEIGHT * 2,
			contentWidth,
			OPTION_HEIGHT,
			Component.translatable("dropcontrol.options.three"),
			Component.translatable("dropcontrol.options.three.description"),
			this.optionThree,
			selected -> this.optionThree = selected
		);
		this.addRenderableWidget(this.optionThreeEntry);
		this.optionFourEntry = new GlobalOptionEntry(
			left,
			OPTIONS_TOP + OPTION_HEIGHT * 3,
			contentWidth,
			OPTION_HEIGHT,
			Component.translatable("dropcontrol.options.four"),
			Component.translatable("dropcontrol.options.four.description"),
			this.optionFour,
			selected -> this.optionFour = selected
		);
		this.addRenderableWidget(this.optionFourEntry);

		int buttonY = this.height - 36;
		ActionButtons actionButtons = new ActionButtons(
			left,
			buttonY,
			contentWidth,
			this::onClose,
			() -> {
			},
			this::toggleAllOptions,
			this::applyOptions,
			true
		);
		actionButtons.addTo(this::addRenderableWidget);
	}

	private void toggleAllOptions() {
		boolean selectAll = !(
			this.optionOne
				&& this.optionTwo
				&& this.optionThree
				&& this.optionFour
		);
		this.optionOneEntry.setSelected(selectAll);
		this.optionTwoEntry.setSelected(selectAll);
		this.optionThreeEntry.setSelected(selectAll);
		this.optionFourEntry.setSelected(selectAll);
	}

	private void applyOptions() {
		boolean saved = DropControlConfig.saveOptions(
			this.optionOne,
			this.optionTwo,
			this.optionThree,
			this.optionFour
		);
		this.status = Component.translatable(saved ? "dropcontrol.options.status.applied" : "dropcontrol.status.save_failed");
		this.statusColor = saved ? 0xFF9CD67A : 0xFFFF6B6B;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xD0101010);

		int contentWidth = Math.min(MAX_CONTENT_WIDTH, this.width - SIDE_MARGIN * 2);
		int left = (this.width - contentWidth) / 2;
		graphics.centeredText(this.font, Component.translatable("dropcontrol.title"), this.width / 2, 14, 0xFFFFFFFF);
		graphics.centeredText(
			this.font,
			Component.translatable("dropcontrol.options.subtitle"),
			this.width / 2,
			29,
			0xFFBDBDBD
		);

		this.basePanel.render(graphics, this.font, left, 47, contentWidth);
		graphics.text(this.font, this.title, left + 4, 123, 0xFFE0E0E0, true);
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		if (!this.status.getString().isEmpty()) {
			graphics.centeredText(this.font, this.status, this.width / 2, this.height - 49, this.statusColor);
		}
	}

	@Override
	public void onClose() {
		this.status = Component.empty();
		this.minecraft.gui.setScreen(this.parent);
	}
}
