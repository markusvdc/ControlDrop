package br.com.dropcontrol.client.screen.component;

import br.com.dropcontrol.config.DropControlConfig;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ItemEntry {
	private final Minecraft minecraft;
	private final Identifier itemId;
	private final Identifier texture;
	private final Identifier tintedTexture;
	private final int sourceImageSize;
	private final int tintColor;
	private final Component name;
	private final Component lore;
	private final Component description;
	private final boolean category;
	private final boolean removesDrop;
	private boolean selected;

	public ItemEntry(Minecraft minecraft, Item item) {
		this(minecraft, item, uppercaseNativeName(minecraft, item), false);
	}

	public ItemEntry(Minecraft minecraft, String markerId, Item icon, String translationKey) {
		this(
			minecraft,
			Identifier.parse(markerId),
			itemTexture(icon),
			null,
			0xFFFFFFFF,
			Component.translatable(translationKey),
			Component.translatable(translationKey + ".lore"),
			Component.translatable(translationKey + ".description"),
			false,
			false
		);
	}

	public ItemEntry(
		Minecraft minecraft,
		String markerId,
		Identifier texture,
		Identifier tintedTexture,
		int tintColor,
		String translationKey
	) {
		this(
			minecraft,
			Identifier.parse(markerId),
			texture,
			tintedTexture,
			tintColor,
			Component.translatable(translationKey),
			Component.translatable(translationKey + ".lore"),
			Component.translatable(translationKey + ".description"),
			false,
			false
		);
	}

	public ItemEntry(Minecraft minecraft, String markerId, Identifier texture, String translationKey) {
		this(
			minecraft,
			Identifier.parse(markerId),
			texture,
			null,
			0xFFFFFFFF,
			Component.translatable(translationKey),
			Component.translatable(translationKey + ".lore"),
			Component.translatable(translationKey + ".description"),
			false,
			false
		);
	}

	public ItemEntry(Minecraft minecraft, String markerId, Item icon, String translationKey, String loreKey) {
		this(
			minecraft,
			Identifier.parse(markerId),
			itemTexture(icon),
			null,
			0xFFFFFFFF,
			Component.translatable(translationKey),
			Component.translatable(loreKey),
			Component.translatable(translationKey + ".description"),
			false,
			false
		);
	}

	private ItemEntry(Minecraft minecraft, Item item, Component name, boolean category) {
		this(
			minecraft,
			BuiltInRegistries.ITEM.getKey(item),
			itemTexture(item),
			null,
			0xFFFFFFFF,
			name,
			Component.empty(),
			Component.empty(),
			category,
			false
		);
	}

	private ItemEntry(
		Minecraft minecraft,
		Identifier itemId,
		Identifier texture,
		Identifier tintedTexture,
		int tintColor,
		Component name,
		Component lore,
		Component description,
		boolean category,
		boolean removesDrop
	) {
		this(minecraft, itemId, texture, tintedTexture, tintColor, name, lore, description, category, removesDrop, 16);
	}

	private ItemEntry(
		Minecraft minecraft,
		Identifier itemId,
		Identifier texture,
		Identifier tintedTexture,
		int tintColor,
		Component name,
		Component lore,
		Component description,
		boolean category,
		boolean removesDrop,
		int sourceImageSize
	) {
		this.minecraft = minecraft;
		this.itemId = itemId;
		this.texture = texture;
		this.tintedTexture = tintedTexture;
		this.sourceImageSize = sourceImageSize;
		this.tintColor = tintColor;
		this.name = name;
		this.lore = lore;
		this.description = description;
		this.category = category;
		this.removesDrop = removesDrop;
		this.selected = !category && DropControlConfig.isSelected(itemId);
	}

	private static Identifier itemTexture(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).withPrefix("textures/item/").withSuffix(".png");
	}

	public static ItemEntry category(Minecraft minecraft, String translationKey) {
		return new ItemEntry(minecraft, Items.AIR, Component.translatable(translationKey), true);
	}

	public static ItemEntry preRenderedPngIcon(
		Minecraft minecraft,
		String markerId,
		Identifier pngTexture,
		int sourceImageSize,
		String translationKey
	) {
		return new ItemEntry(
			minecraft,
			Identifier.parse(markerId),
			pngTexture,
			null,
			0xFFFFFFFF,
			Component.translatable(translationKey),
			Component.translatable(translationKey + ".lore"),
			Component.translatable(translationKey + ".description"),
			false,
			false,
			sourceImageSize
		);
	}

	public static ItemEntry removal(Minecraft minecraft, String markerId, Item icon, String translationKey) {
		return removal(minecraft, markerId, itemTexture(icon), translationKey);
	}

	public static ItemEntry removal(Minecraft minecraft, String markerId, Identifier texture, String translationKey) {
		return new ItemEntry(
			minecraft,
			Identifier.parse(markerId),
			texture,
			null,
			0xFFFFFFFF,
			Component.translatable(translationKey),
			Component.translatable(translationKey + ".lore"),
			Component.translatable(translationKey + ".description"),
			false,
			true
		);
	}

	private static Component uppercaseNativeName(Minecraft minecraft, Item item) {
		String languageCode = minecraft.getLanguageManager().getSelected();
		Locale locale = Locale.forLanguageTag(languageCode.replace('_', '-'));
		String localizedName = Component.translatable(item.getDescriptionId()).getString();
		return Component.literal(localizedName.toUpperCase(locale));
	}

	public void renderBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean hovered) {
		if (this.category) {
			int lineY = y + height / 2;
			graphics.fill(x, lineY, x + width, lineY + 1, 0xFF4A4A4A);
			graphics.fill(x + 7, y + 3, x + 15 + this.minecraft.font.width(this.name), y + height - 5, 0xFF101010);
			return;
		}

		graphics.fill(x, y + 1, x + width, y + height - 2, hovered ? 0xCC333333 : 0x99202020);
		graphics.fill(x, y + 1, x + 1, y + height - 2, this.selected ? 0xFF79C64A : 0xFF555555);
		if (hovered) {
			graphics.outline(x, y + 1, width, height - 3, 0xFFFFFFFF);
		}
	}

	public void renderContent(GuiGraphicsExtractor graphics, int x, int y, int height) {
		if (this.category) {
			graphics.text(this.minecraft.font, this.name, x + 11, y + 9, 0xFFFFD36A, true);
			return;
		}

		drawCheckbox(graphics, x + 8, y + 9);
		if (this.tintedTexture != null) {
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				this.tintedTexture,
				x + 29,
				y + 7,
				0,
				0,
				16,
				16,
				16,
				16,
				this.tintColor
			);
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, x + 29, y + 7, 0, 0, 16, 16, 16, 16);
		} else {
			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				this.texture,
				x + 29,
				y + 7,
				0,
				0,
				16,
				16,
				this.sourceImageSize,
				this.sourceImageSize,
				this.sourceImageSize,
				this.sourceImageSize
			);
		}
		graphics.text(this.minecraft.font, this.name, x + 52, y + 11, 0xFFFFFFFF, true);
	}

	private void drawCheckbox(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 11, y + 11, 0xFF111111);
		graphics.fill(x + 1, y + 1, x + 10, y + 10, 0xFF8B8B8B);
		graphics.fill(x + 2, y + 2, x + 9, y + 9, 0xFF252525);
		if (this.selected) {
			graphics.fill(x + 3, y + 5, x + 5, y + 8, 0xFF8EE36B);
			graphics.fill(x + 5, y + 7, x + 7, y + 9, 0xFF8EE36B);
			graphics.fill(x + 7, y + 3, x + 9, y + 8, 0xFF8EE36B);
		}
	}

	public void toggle() {
		if (!this.category) {
			this.selected = !this.selected;
		}
	}

	public void setSelected(boolean selected) {
		if (!this.category) {
			this.selected = selected;
		}
	}

	public boolean isSelected() {
		return this.selected;
	}

	public Identifier itemId() {
		return this.itemId;
	}

	public boolean isCategory() {
		return this.category;
	}

	public boolean removesDrop() {
		return this.removesDrop;
	}

	public Component name() {
		return this.name;
	}

	public Component description() {
		return this.description;
	}

	public Component lore() {
		return this.lore;
	}
}
