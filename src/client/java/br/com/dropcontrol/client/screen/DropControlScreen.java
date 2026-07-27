package br.com.dropcontrol.client.screen;

import br.com.dropcontrol.client.screen.component.ActionButtons;
import br.com.dropcontrol.client.screen.component.DropBasePanel;
import br.com.dropcontrol.client.screen.component.ItemSelectionList;
import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DropControlScreen extends Screen {
	private static final int MAX_CONTENT_WIDTH = 540;
	private static final int SIDE_MARGIN = 16;

	private final Screen parent;
	private final DropBasePanel basePanel = new DropBasePanel();
	private ItemSelectionList itemList;
	private Component status = Component.empty();
	private int statusColor = 0xFF9CD67A;

	public DropControlScreen(Screen parent) {
		super(Component.translatable("dropcontrol.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(MAX_CONTENT_WIDTH, this.width - SIDE_MARGIN * 2);
		int left = (this.width - contentWidth) / 2;
		int listTop = 137;
		int buttonY = this.height - 36;
		int listBottom = Math.max(listTop + 56, buttonY - 12);

		this.itemList = new ItemSelectionList(this.minecraft, contentWidth, listBottom - listTop, listTop, 30);
		this.itemList.setX(left);
		this.addRenderableWidget(this.itemList);

		ActionButtons actionButtons = new ActionButtons(
			left,
			buttonY,
			contentWidth,
			this::onClose,
			this::openOptions,
			this::toggleAllItems,
			this::applySelection,
			false
		);
		actionButtons.addTo(this::addRenderableWidget);
	}

	private void openOptions() {
		this.clearStatus();
		this.minecraft.gui.setScreen(new DropControlOptionsScreen(this));
	}

	private void clearStatus() {
		this.status = Component.empty();
	}

	private void applySelection() {
		boolean saved = DropControlConfig.saveSelection(this.itemList.selectedIds());
		this.status = Component.translatable(saved ? "dropcontrol.status.applied" : "dropcontrol.status.save_failed");
		this.statusColor = saved ? 0xFF9CD67A : 0xFFFF6B6B;
	}

	private void toggleAllItems() {
		this.itemList.setAllSelected(!this.itemList.areAllSelected());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xD0101010);

		int contentWidth = Math.min(MAX_CONTENT_WIDTH, this.width - SIDE_MARGIN * 2);
		int left = (this.width - contentWidth) / 2;

		graphics.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
		graphics.centeredText(
			this.font,
			Component.translatable("dropcontrol.subtitle"),
			this.width / 2,
			29,
			0xFFBDBDBD
		);

		this.basePanel.render(graphics, this.font, left, 47, contentWidth);
		graphics.text(this.font, Component.translatable("dropcontrol.list.title"), left + 4, 123, 0xFFE0E0E0, true);

		super.extractRenderState(graphics, mouseX, mouseY, delta);

		if (!this.status.getString().isEmpty()) {
			graphics.centeredText(this.font, this.status, this.width / 2, this.height - 49, this.statusColor);
		}
	}

	@Override
	public void onClose() {
		this.clearStatus();
		this.minecraft.gui.setScreen(this.parent);
	}
}
