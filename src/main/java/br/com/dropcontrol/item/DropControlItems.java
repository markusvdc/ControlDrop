package br.com.dropcontrol.item;

import br.com.dropcontrol.DropControl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class DropControlItems {
	private static final Identifier WITCH_WART_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "witch_wart");
	private static final ResourceKey<Item> WITCH_WART_KEY =
		ResourceKey.create(Registries.ITEM, WITCH_WART_ID);

	public static final Item WITCH_WART = Registry.register(
		BuiltInRegistries.ITEM,
		WITCH_WART_KEY,
		new Item(new Item.Properties().setId(WITCH_WART_KEY))
	);

	private DropControlItems() {
	}

	public static void initialize() {
	}
}
