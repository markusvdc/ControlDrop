package br.com.dropcontrol.item;

import br.com.dropcontrol.DropControl;
import br.com.dropcontrol.block.DropControlBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.entity.EquipmentSlot;

public final class DropControlItems {
	private static final Identifier WITCH_WART_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "witch_wart");
	private static final ResourceKey<Item> WITCH_WART_KEY =
		ResourceKey.create(Registries.ITEM, WITCH_WART_ID);
	private static final Identifier WARDEN_HORN_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "warden_horn");
	private static final ResourceKey<Item> WARDEN_HORN_KEY =
		ResourceKey.create(Registries.ITEM, WARDEN_HORN_ID);
	private static final Identifier ENDERMAN_PARTICLES_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "enderman_particles");
	private static final ResourceKey<Item> ENDERMAN_PARTICLES_KEY =
		ResourceKey.create(Registries.ITEM, ENDERMAN_PARTICLES_ID);
	private static final Identifier SPIDER_HEAD_ID =
		Identifier.fromNamespaceAndPath(DropControl.MOD_ID, "spider_head");
	private static final ResourceKey<Item> SPIDER_HEAD_KEY =
		ResourceKey.create(Registries.ITEM, SPIDER_HEAD_ID);

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
	public static final Item ENDERMAN_PARTICLES = Registry.register(
		BuiltInRegistries.ITEM,
		ENDERMAN_PARTICLES_KEY,
		new Item(new Item.Properties().setId(ENDERMAN_PARTICLES_KEY).stacksTo(64))
	);
	public static final Item SPIDER_HEAD = Registry.register(
		BuiltInRegistries.ITEM,
		SPIDER_HEAD_KEY,
		new StandingAndWallBlockItem(
			DropControlBlocks.SPIDER_HEAD,
			DropControlBlocks.SPIDER_WALL_HEAD,
			Direction.DOWN,
			new Item.Properties()
				.setId(SPIDER_HEAD_KEY)
				.rarity(Rarity.UNCOMMON)
				.equippableUnswappable(EquipmentSlot.HEAD)
		)
	);

	private DropControlItems() {
	}

	public static void initialize() {
	}
}
