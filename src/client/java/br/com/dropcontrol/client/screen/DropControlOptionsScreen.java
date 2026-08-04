package br.com.dropcontrol.client.screen;

import br.com.dropcontrol.client.screen.component.ActionButtons;
import br.com.dropcontrol.client.PhantomPurge;
import br.com.dropcontrol.client.screen.component.DropBasePanel;
import br.com.dropcontrol.client.screen.component.GlobalOptionList;
import br.com.dropcontrol.config.DropControlConfig;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DropControlOptionsScreen extends Screen {
	private static final int MAX_CONTENT_WIDTH = 540;
	private static final int SIDE_MARGIN = 16;
	private static final int OPTIONS_TOP = 137;

	private final Screen parent;
	private final DropBasePanel basePanel = new DropBasePanel();
	private GlobalOptionList optionList;
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
		int buttonY = this.height - 36;
		int listBottom = Math.max(OPTIONS_TOP + 56, buttonY - 12);
		this.optionList = new GlobalOptionList(
			this.minecraft,
			contentWidth,
			listBottom - OPTIONS_TOP,
			OPTIONS_TOP,
			List.of(
				new GlobalOptionList.Option(
					DropControlConfig.CONTINUOUS_DAMAGE,
					Component.translatable("dropcontrol.options.continuous_damage"),
					Component.translatable("dropcontrol.options.continuous_damage.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.CONSTANT_THREAT,
					Component.translatable("dropcontrol.options.constant_threat"),
					Component.translatable("dropcontrol.options.constant_threat.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.PARKED_SADDLED_HORSES,
					Component.translatable("dropcontrol.options.parked_saddled_horses"),
					Component.translatable("dropcontrol.options.parked_saddled_horses.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.RABBITS_AVOID_FENCES,
					Component.translatable("dropcontrol.options.rabbits_avoid_fences"),
					Component.translatable("dropcontrol.options.rabbits_avoid_fences.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.ENDERMEN_DONT_PICK_UP_BLOCKS,
					Component.translatable("dropcontrol.options.endermen_dont_pick_up_blocks"),
					Component.translatable("dropcontrol.options.endermen_dont_pick_up_blocks.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.CHESTPLATE_ELYTRA_SWAP,
					Component.translatable("dropcontrol.options.chestplate_elytra_swap"),
					Component.translatable("dropcontrol.options.chestplate_elytra_swap.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.PAUSE_WHEN_MOUSE_IDLE,
					Component.translatable("dropcontrol.options.pause_when_mouse_idle"),
					Component.translatable("dropcontrol.options.pause_when_mouse_idle.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.EXACT_HORSE_HEALTH,
					Component.translatable("dropcontrol.options.exact_horse_health"),
					Component.translatable("dropcontrol.options.exact_horse_health.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.PHANTOM_PRESSURE_ONE,
					Component.translatable("dropcontrol.options.phantom_pressure_one"),
					Component.translatable("dropcontrol.options.phantom_pressure_one.lore"),
					Component.translatable("dropcontrol.options.phantom_pressure_one.description")
				),
				new GlobalOptionList.Option(
					DropControlConfig.PHANTOM_PRESSURE_TWO,
					Component.translatable("dropcontrol.options.phantom_pressure_two"),
					Component.translatable("dropcontrol.options.phantom_pressure_two.lore"),
					Component.translatable("dropcontrol.options.phantom_pressure_two.description")
				)
			)
		);
		this.optionList.setX(left);
		this.addRenderableWidget(this.optionList);

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
		this.optionList.setAllSelected(!this.optionList.areAllSelected());
	}

	private void applyOptions() {
		boolean phantomPressureTwoWasEnabled = DropControlConfig.phantomPressureTwo();
		java.util.Set<String> enabledIds = this.optionList.enabledIds();
		boolean removesPhantomPressureTwo = phantomPressureTwoWasEnabled
			&& !enabledIds.contains(DropControlConfig.PHANTOM_PRESSURE_TWO);
		boolean saved = DropControlConfig.saveOptions(enabledIds);
		if (saved && removesPhantomPressureTwo) {
			PhantomPurge.killAllLoaded(this.minecraft);
		}
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
		this.optionList.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public void onClose() {
		this.status = Component.empty();
		this.minecraft.gui.setScreen(this.parent);
	}
}
