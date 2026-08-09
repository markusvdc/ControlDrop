package br.com.dropcontrol.client.screen.component;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class GlobalOptionList extends AbstractWidget {
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLLBAR_GAP = 6;

	private final Minecraft minecraft;
	private final List<Entry> entries;
	private final int rowHeight;
	private boolean draggingScrollbar;
	private double scrollAmount;
	private Component hoveredLore;
	private Component hoveredDescription;

	public GlobalOptionList(Minecraft minecraft, int width, int height, int y, int rowHeight, List<Option> options) {
		super(0, y, width, height, Component.translatable("dropcontrol.options.title"));
		this.minecraft = minecraft;
		this.rowHeight = rowHeight;
		Comparator<Component> alphabeticalOrder = AlphabeticalOrder.components(minecraft);
		List<Option> sortedOptions = new ArrayList<>();
		List<Option> section = new ArrayList<>();
		for (Option option : options) {
			if (option.category()) {
				section.sort((first, second) -> alphabeticalOrder.compare(first.label(), second.label()));
				sortedOptions.addAll(section);
				section.clear();
				sortedOptions.add(option);
			} else {
				section.add(option);
			}
		}
		section.sort((first, second) -> alphabeticalOrder.compare(first.label(), second.label()));
		sortedOptions.addAll(section);
		this.entries = sortedOptions.stream()
			.map(option -> new Entry(option, !option.category() && DropControlConfig.isOptionEnabled(option.id())))
			.toList();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		boolean needsScrollbar = getMaxScroll() > 0;
		int contentWidth = this.width - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
		this.hoveredLore = null;
		this.hoveredDescription = null;

		graphics.enableScissor(x, y, x + this.width, y + this.height);
		for (int index = 0; index < this.entries.size(); index++) {
			int rowY = getRowY(index);
			if (!isRowVisible(rowY)) {
				continue;
			}
			Entry entry = this.entries.get(index);
			boolean hovered = !entry.option.category() && mouseX >= x && mouseX < x + contentWidth
				&& mouseY >= rowY && mouseY < rowY + this.rowHeight;
			if (hovered) {
				this.hoveredLore = entry.option.lore();
				this.hoveredDescription = entry.option.description();
			}
			renderEntry(graphics, entry, x, rowY, contentWidth, hovered);
		}
		drawScrollbar(graphics, needsScrollbar);
		graphics.disableScissor();
	}

	public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (this.hoveredDescription == null) {
			return;
		}

		DescriptionTooltip.render(this.minecraft, graphics, this.hoveredLore, this.hoveredDescription, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || !isMouseOver(event.x(), event.y())) {
			return false;
		}
		boolean needsScrollbar = getMaxScroll() > 0;
		int contentWidth = this.width - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
		if (needsScrollbar && event.x() >= getX() + contentWidth) {
			this.draggingScrollbar = true;
			setScrollFromMouse(event.y());
			return true;
		}
		int index = (int)((event.y() - getY() + this.scrollAmount) / this.rowHeight);
		if (index >= 0 && index < this.entries.size()) {
			Entry entry = this.entries.get(index);
			if (entry.option.category()) {
				return false;
			}
			entry.selected = !entry.selected;
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
		this.scrollAmount = Mth.clamp(this.scrollAmount - verticalAmount * this.rowHeight, 0.0, getMaxScroll());
		return true;
	}

	public Set<String> enabledIds() {
		LinkedHashSet<String> ids = new LinkedHashSet<>();
		for (Entry entry : this.entries) {
			if (!entry.option.category() && entry.selected) {
				ids.add(entry.option.id());
			}
		}
		return Set.copyOf(ids);
	}

	public void setAllSelected(boolean selected) {
		this.entries.stream().filter(entry -> !entry.option.category()).forEach(entry -> entry.selected = selected);
	}

	public boolean areAllSelected() {
		return this.entries.stream().filter(entry -> !entry.option.category()).allMatch(entry -> entry.selected);
	}

	private void renderEntry(
		GuiGraphicsExtractor graphics,
		Entry entry,
		int x,
		int y,
		int width,
		boolean hovered
	) {
		if (entry.option.category()) {
			int lineY = y + this.rowHeight / 2;
			int labelY = y + (this.rowHeight - 9) / 2;
			graphics.fill(x, lineY, x + width, lineY + 1, 0xFF4A4A4A);
			graphics.fill(x + 7, labelY - 6, x + 15 + this.minecraft.font.width(entry.option.label()), labelY + 11,
				0xFF101010);
			graphics.text(this.minecraft.font, entry.option.label(), x + 11, labelY, 0xFFFFD36A, true);
			return;
		}
		graphics.fill(x, y + 1, x + width, y + this.rowHeight - 2, hovered ? 0xCC333333 : 0x99202020);
		graphics.fill(x, y + 1, x + 1, y + this.rowHeight - 2, entry.selected ? 0xFF79C64A : 0xFF555555);
		if (hovered) {
			graphics.outline(x, y + 1, width, this.rowHeight - 3, 0xFFFFFFFF);
		}
		drawCheckbox(graphics, x + 8, y + (this.rowHeight - 11) / 2, entry.selected);
		graphics.text(this.minecraft.font, entry.option.label(), x + 29, y + (this.rowHeight - 9) / 2, 0xFFFFFFFF, true);
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
		return getY() + index * this.rowHeight - (int)this.scrollAmount;
	}

	private boolean isRowVisible(int rowY) {
		return rowY + this.rowHeight > getY() && rowY < getY() + this.height;
	}

	private int getMaxScroll() {
		return Math.max(0, this.entries.size() * this.rowHeight - this.height);
	}

	private int getThumbHeight() {
		return Math.max(24, this.height * this.height / (this.entries.size() * this.rowHeight));
	}

	private void setScrollFromMouse(double mouseY) {
		int travel = this.height - getThumbHeight();
		double relative = Mth.clamp((mouseY - getY() - getThumbHeight() / 2.0) / travel, 0.0, 1.0);
		this.scrollAmount = relative * getMaxScroll();
	}

	private void drawScrollbar(GuiGraphicsExtractor graphics, boolean active) {
		int x = getX() + this.width - SCROLLBAR_WIDTH;
		graphics.fill(x, getY(), x + SCROLLBAR_WIDTH, getY() + this.height, 0xFF080808);
		if (!active) {
			graphics.fill(x, getY(), x + SCROLLBAR_WIDTH, getY() + this.height, 0xFF555555);
			graphics.fill(x, getY(), x + 1, getY() + this.height, 0xFF707070);
			graphics.fill(x + SCROLLBAR_WIDTH - 1, getY(), x + SCROLLBAR_WIDTH, getY() + this.height, 0xFF303030);
			return;
		}
		int thumbHeight = getThumbHeight();
		int travel = this.height - thumbHeight;
		int thumbY = getY() + (getMaxScroll() == 0 ? 0 : (int)(travel * this.scrollAmount / getMaxScroll()));
		graphics.fill(x, thumbY, x + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFC0C0C0);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	public record Option(String id, Component label, Component lore, Component description, boolean category) {
		public Option(String id, Component label, Component lore, Component description) {
			this(id, label, lore, description, false);
		}

		public Option(String id, Component label, Component description) {
			this(id, label, Component.empty(), description, false);
		}

		public static Option category(Component label) {
			return new Option(null, label, Component.empty(), Component.empty(), true);
		}
	}

	private static final class Entry {
		private final Option option;
		private boolean selected;

		private Entry(Option option, boolean selected) {
			this.option = option;
			this.selected = selected;
		}
	}

}
