package br.com.dropcontrol.client.screen.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;

public final class ItemSelectionList extends AbstractWidget {
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLLBAR_GAP = 6;

	private final Minecraft minecraft;
	private final List<ItemEntry> entries;
	private final int rowHeight;
	private boolean draggingScrollbar;
	private double scrollAmount;
	private Component hoveredLore;
	private Component hoveredDescription;

	public ItemSelectionList(Minecraft minecraft, int width, int height, int y, int rowHeight) {
		super(0, y, width, height, Component.translatable("dropcontrol.list.title"));
		this.minecraft = minecraft;
		this.rowHeight = rowHeight;
		this.entries = sortWithinCategories(minecraft, List.of(
			ItemEntry.category(minecraft, "dropcontrol.category.spider"),
			new ItemEntry(
				minecraft,
				"dropcontrol:spider_cobweb",
				Identifier.withDefaultNamespace("textures/block/cobweb.png"),
				"dropcontrol.marker.spider_cobweb"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.witch"),
			new ItemEntry(
				minecraft,
				"dropcontrol:witch_potions",
				Identifier.withDefaultNamespace("textures/item/potion.png"),
				Identifier.withDefaultNamespace("textures/item/potion_overlay.png"),
				0xFFC2FF66,
				"dropcontrol.marker.witch_potions"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.creeper"),
			ItemEntry.preRenderedPngIcon(
				minecraft,
				"dropcontrol:creeper_tnt",
				Identifier.fromNamespaceAndPath("dropcontrol", "textures/gui/tnt.png"),
				300,
				"dropcontrol.marker.creeper_tnt"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.skeleton"),
			new ItemEntry(
				minecraft,
				"dropcontrol:skeleton_spectral_arrow",
				Items.SPECTRAL_ARROW,
				"dropcontrol.marker.skeleton_spectral_arrow"
			),
			new ItemEntry(
				minecraft,
				"dropcontrol:skeleton_enchantment",
				Items.ENCHANTED_BOOK,
				"dropcontrol.marker.skeleton_enchantment",
				"dropcontrol.marker.skeleton_enchantment.lore"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.pillager"),
			new ItemEntry(
				minecraft,
				"dropcontrol:pillager_wealth",
				Items.EMERALD,
				"dropcontrol.marker.pillager_wealth"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.zombie"),
			ItemEntry.preRenderedPngIcon(
				minecraft,
				"dropcontrol:zombie_sulfur",
				Identifier.fromNamespaceAndPath("dropcontrol", "textures/gui/sulfur.png"),
				300,
				"dropcontrol.marker.zombie_sulfur"
			),
			ItemEntry.category(minecraft, "dropcontrol.category.remove"),
			ItemEntry.removal(
				minecraft,
				"dropcontrol:witch_all",
				Items.REDSTONE,
				"dropcontrol.marker.witch_all"
			),
			ItemEntry.removal(
				minecraft,
				"dropcontrol:skeleton_armor",
				Items.BOW,
				"dropcontrol.marker.skeleton_armor"
			),
			ItemEntry.removal(
				minecraft,
				"dropcontrol:pillager_crossbow",
				Identifier.withDefaultNamespace("textures/item/crossbow_standby.png"),
				"dropcontrol.marker.pillager_crossbow"
			),
			ItemEntry.removal(
				minecraft,
				"dropcontrol:zombie_armor",
				Items.POTATO,
				"dropcontrol.marker.zombie_armor"
			)
		));
	}

	private static List<ItemEntry> sortWithinCategories(Minecraft minecraft, List<ItemEntry> entries) {
		List<ItemEntry> sorted = new ArrayList<>(entries);
		Comparator<ItemEntry> comparator = Comparator.comparing(ItemEntry::removesDrop)
			.reversed()
			.thenComparing(ItemEntry::name, AlphabeticalOrder.components(minecraft));
		int categoryStart = 0;
		while (categoryStart < sorted.size()) {
			int nextCategory = categoryStart + 1;
			while (nextCategory < sorted.size() && !sorted.get(nextCategory).isCategory()) {
				nextCategory++;
			}
			sorted.subList(categoryStart + 1, nextCategory).sort(comparator);
			categoryStart = nextCategory;
		}
		return List.copyOf(sorted);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int x = getX();
		int y = getY();
		int contentWidth = this.width - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
		boolean needsScrollbar = getMaxScroll() > 0;
		this.hoveredDescription = null;
		this.hoveredLore = null;

		graphics.enableScissor(x, y, x + this.width, y + this.height);
		graphics.fill(x, y, x + this.width, y + this.height, 0xB8101010);

		for (int index = 0; index < this.entries.size(); index++) {
			int rowY = getRowY(index);
			if (!isRowVisible(rowY)) {
				continue;
			}
			ItemEntry entry = this.entries.get(index);
			boolean hovered = !entry.isCategory()
				&& mouseX >= x
				&& mouseX < x + contentWidth
				&& mouseY >= rowY
				&& mouseY < rowY + this.rowHeight;
			if (hovered) {
				this.hoveredLore = entry.lore();
				this.hoveredDescription = entry.description();
			}
			entry.renderBackground(graphics, x, rowY, contentWidth, this.rowHeight, hovered);
		}

		if (needsScrollbar) {
			drawScrollbarTrack(graphics);
		}
		graphics.disableScissor();

		graphics.nextStratum();
		graphics.enableScissor(x, y, x + this.width, y + this.height);
		for (int index = 0; index < this.entries.size(); index++) {
			int rowY = getRowY(index);
			if (isRowVisible(rowY)) {
				this.entries.get(index).renderContent(graphics, x, rowY, this.rowHeight);
			}
		}
		if (needsScrollbar) {
			drawScrollbarThumb(graphics);
		}
		graphics.disableScissor();
	}

	public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (this.hoveredDescription != null) {
			DescriptionTooltip.render(
				this.minecraft,
				graphics,
				this.hoveredLore,
				this.hoveredDescription,
				mouseX,
				mouseY
			);
		}
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

		int index = (int) ((event.y() - getY() + this.scrollAmount) / this.rowHeight);
		if (index >= 0 && index < this.entries.size()) {
			ItemEntry entry = this.entries.get(index);
			if (!entry.isCategory()) {
				entry.toggle();
				playDownSound(Minecraft.getInstance().getSoundManager());
			}
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
		this.scrollAmount = Mth.clamp(
			this.scrollAmount - verticalAmount * this.rowHeight,
			0.0,
			getMaxScroll()
		);
		return true;
	}

	public void setAllSelected(boolean selected) {
		this.entries.forEach(entry -> entry.setSelected(selected));
	}

	public boolean areAllSelected() {
		return this.entries.stream()
			.filter(entry -> !entry.isCategory())
			.allMatch(ItemEntry::isSelected);
	}

	public Set<Identifier> selectedIds() {
		return this.entries.stream()
			.filter(entry -> !entry.isCategory() && entry.isSelected())
			.map(ItemEntry::itemId)
			.collect(Collectors.toUnmodifiableSet());
	}

	private int getRowY(int index) {
		return getY() + index * this.rowHeight - (int) this.scrollAmount;
	}

	private boolean isRowVisible(int rowY) {
		return rowY + this.rowHeight > getY() && rowY < getY() + this.height;
	}

	private int getMaxScroll() {
		return Math.max(0, this.entries.size() * this.rowHeight - this.height);
	}

	private void setScrollFromMouse(double mouseY) {
		int travel = this.height - getThumbHeight();
		double relative = Mth.clamp((mouseY - getY() - getThumbHeight() / 2.0) / travel, 0.0, 1.0);
		this.scrollAmount = relative * getMaxScroll();
	}

	private int getThumbHeight() {
		return Math.max(24, this.height * this.height / (this.entries.size() * this.rowHeight));
	}

	private int getThumbY() {
		int travel = this.height - getThumbHeight();
		return getY() + (getMaxScroll() == 0 ? 0 : (int) (travel * this.scrollAmount / getMaxScroll()));
	}

	private void drawScrollbarTrack(GuiGraphicsExtractor graphics) {
		int scrollbarX = getX() + this.width - SCROLLBAR_WIDTH;
		graphics.fill(scrollbarX, getY(), scrollbarX + SCROLLBAR_WIDTH, getY() + this.height, 0xFF080808);
	}

	private void drawScrollbarThumb(GuiGraphicsExtractor graphics) {
		int scrollbarX = getX() + this.width - SCROLLBAR_WIDTH;
		int thumbY = getThumbY();
		graphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + getThumbHeight(), 0xFFC0C0C0);
		graphics.fill(scrollbarX, thumbY, scrollbarX + 1, thumbY + getThumbHeight(), 0xFFFFFFFF);
		graphics.fill(scrollbarX + SCROLLBAR_WIDTH - 1, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + getThumbHeight(), 0xFF707070);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
