package br.com.dropcontrol.client.screen.component;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class GlobalOptionList extends AbstractWidget {
	private static final int ROW_HEIGHT = 40;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLLBAR_GAP = 6;
	private static final int VANILLA_TOOLTIP_MAX_WIDTH = 170;
	private static final int TOOLTIP_MAX_WIDTH = VANILLA_TOOLTIP_MAX_WIDTH * 5 / 2;
	private static final int TOOLTIP_LINE_HEIGHT = 12;

	private final Minecraft minecraft;
	private final List<Entry> entries;
	private boolean draggingScrollbar;
	private double scrollAmount;
	private Component hoveredDescription;

	public GlobalOptionList(Minecraft minecraft, int width, int height, int y, List<Option> options) {
		super(0, y, width, height, Component.translatable("dropcontrol.options.title"));
		this.minecraft = minecraft;
		this.entries = options.stream()
			.map(option -> new Entry(option, DropControlConfig.isOptionEnabled(option.id())))
			.toList();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		int contentWidth = this.width - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
		boolean needsScrollbar = getMaxScroll() > 0;
		this.hoveredDescription = null;

		graphics.enableScissor(x, y, x + this.width, y + this.height);
		graphics.fill(x, y, x + this.width, y + this.height, 0xB8101010);
		for (int index = 0; index < this.entries.size(); index++) {
			int rowY = getRowY(index);
			if (!isRowVisible(rowY)) {
				continue;
			}
			Entry entry = this.entries.get(index);
			boolean hovered = mouseX >= x && mouseX < x + contentWidth
				&& mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
			if (hovered) {
				this.hoveredDescription = entry.option.description();
			}
			renderEntry(graphics, entry, x, rowY, contentWidth, hovered);
		}
		if (needsScrollbar) {
			drawScrollbar(graphics);
		}
		graphics.disableScissor();
	}

	public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (this.hoveredDescription == null) {
			return;
		}

		List<FormattedCharSequence> lines = this.minecraft.font.split(
			this.hoveredDescription,
			TOOLTIP_MAX_WIDTH
		);
		graphics.tooltip(
			this.minecraft.font,
			List.of(new GlobalOptionTooltip(lines)),
			mouseX,
			mouseY,
			DefaultTooltipPositioner.INSTANCE,
			null
		);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || !isMouseOver(event.x(), event.y())) {
			return false;
		}
		int contentWidth = this.width - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
		if (event.x() >= getX() + contentWidth) {
			this.draggingScrollbar = getMaxScroll() > 0;
			setScrollFromMouse(event.y());
			return true;
		}
		int index = (int)((event.y() - getY() + this.scrollAmount) / ROW_HEIGHT);
		if (index >= 0 && index < this.entries.size()) {
			this.entries.get(index).selected = !this.entries.get(index).selected;
			playDownSound(this.minecraft.getSoundManager());
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
		if (!this.draggingScrollbar || event.button() != 0) {
			return false;
		}
		setScrollFromMouse(event.y());
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && this.draggingScrollbar) {
			this.draggingScrollbar = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		this.scrollAmount = Mth.clamp(this.scrollAmount - verticalAmount * ROW_HEIGHT, 0.0, getMaxScroll());
		return true;
	}

	public Set<String> enabledIds() {
		LinkedHashSet<String> ids = new LinkedHashSet<>();
		for (Entry entry : this.entries) {
			if (entry.selected) {
				ids.add(entry.option.id());
			}
		}
		return Set.copyOf(ids);
	}

	public void setAllSelected(boolean selected) {
		this.entries.forEach(entry -> entry.selected = selected);
	}

	public boolean areAllSelected() {
		return this.entries.stream().allMatch(entry -> entry.selected);
	}

	private void renderEntry(
		GuiGraphicsExtractor graphics,
		Entry entry,
		int x,
		int y,
		int width,
		boolean hovered
	) {
		graphics.fill(x, y + 1, x + width, y + ROW_HEIGHT - 2, hovered ? 0xCC333333 : 0x99202020);
		graphics.fill(x, y + 1, x + 1, y + ROW_HEIGHT - 2, entry.selected ? 0xFF79C64A : 0xFF555555);
		if (hovered) {
			graphics.outline(x, y + 1, width, ROW_HEIGHT - 3, 0xFFFFFFFF);
		}
		drawCheckbox(graphics, x + 8, y + 14, entry.selected);
		graphics.text(this.minecraft.font, entry.option.label(), x + 29, y + 16, 0xFFFFFFFF, true);
	}

	private void drawCheckbox(GuiGraphicsExtractor graphics, int x, int y, boolean selected) {
		graphics.fill(x, y, x + 11, y + 11, 0xFF111111);
		graphics.fill(x + 1, y + 1, x + 10, y + 10, 0xFF8B8B8B);
		graphics.fill(x + 2, y + 2, x + 9, y + 9, 0xFF252525);
		if (selected) {
			graphics.fill(x + 3, y + 5, x + 5, y + 8, 0xFF8EE36B);
			graphics.fill(x + 5, y + 7, x + 7, y + 9, 0xFF8EE36B);
			graphics.fill(x + 7, y + 3, x + 9, y + 8, 0xFF8EE36B);
		}
	}

	private int getRowY(int index) {
		return getY() + index * ROW_HEIGHT - (int)this.scrollAmount;
	}

	private boolean isRowVisible(int rowY) {
		return rowY + ROW_HEIGHT > getY() && rowY < getY() + this.height;
	}

	private int getMaxScroll() {
		return Math.max(0, this.entries.size() * ROW_HEIGHT - this.height);
	}

	private int getThumbHeight() {
		return Math.max(24, this.height * this.height / (this.entries.size() * ROW_HEIGHT));
	}

	private void setScrollFromMouse(double mouseY) {
		int travel = this.height - getThumbHeight();
		double relative = Mth.clamp((mouseY - getY() - getThumbHeight() / 2.0) / travel, 0.0, 1.0);
		this.scrollAmount = relative * getMaxScroll();
	}

	private void drawScrollbar(GuiGraphicsExtractor graphics) {
		int x = getX() + this.width - SCROLLBAR_WIDTH;
		int thumbHeight = getThumbHeight();
		int travel = this.height - thumbHeight;
		int thumbY = getY() + (getMaxScroll() == 0 ? 0 : (int)(travel * this.scrollAmount / getMaxScroll()));
		graphics.fill(x, getY(), x + SCROLLBAR_WIDTH, getY() + this.height, 0xFF080808);
		graphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFC0C0C0);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	public record Option(String id, Component label, Component description) {
	}

	private static final class Entry {
		private final Option option;
		private boolean selected;

		private Entry(Option option, boolean selected) {
			this.option = option;
			this.selected = selected;
		}
	}

	private record GlobalOptionTooltip(List<FormattedCharSequence> lines) implements ClientTooltipComponent {
		@Override
		public int getHeight(Font font) {
			return this.lines.size() * TOOLTIP_LINE_HEIGHT;
		}

		@Override
		public int getWidth(Font font) {
			return this.lines.stream().mapToInt(font::width).max().orElse(0);
		}

		@Override
		public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
			for (int index = 0; index < this.lines.size(); index++) {
				graphics.text(font, this.lines.get(index), x, y + index * TOOLTIP_LINE_HEIGHT, -1, true);
			}
		}
	}
}
