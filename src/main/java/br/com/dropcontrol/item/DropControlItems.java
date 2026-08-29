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
	private static final Identifier WARDEN_HORN_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "warden_horn");
	private static final ResourceKey<Item> WARDEN_HORN_KEY =
		ResourceKey.create(Registries.ITEM, WARDEN_HORN_ID);

	public static final Item WITCH_WART = Registry.register(
		BuiltInRegistries.ITEM,
		WITCH_WART_KEY,
		new Item(new Item.Properties().setId(WITCH_WART_KEY))
	);
	public static final Item WARDEN_HORN = Registry.register(
		BuiltInRegistries.ITEM,
		WARDEN_HORN_KEY,
		new Item(new Item.Properties().setId(WARDEN_HORN_KEY).stacksTo(64))
	);

	private DropControlItems() {
	}

	public static void initialize() {
	}
}
