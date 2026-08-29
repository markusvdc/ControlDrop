package br.com.dropcontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class DropControlConfig {
	public static final String CONSTANT_THREAT = "constant_threat";
	public static final String PARKED_SADDLED_HORSES = "parked_saddled_horses";
	public static final String RABBITS_AVOID_FENCES = "rabbits_avoid_fences";
	public static final String ENDERMEN_DONT_PICK_UP_BLOCKS = "endermen_dont_pick_up_blocks";
	public static final String ETERNAL_RELICS = "eternal_relics";
	public static final String CHESTPLATE_ELYTRA_SWAP = "chestplate_elytra_swap";
	public static final String INVENTORY_SORTING = "inventory_sorting";
	public static final String ASCENDING_TREASURE = "ascending_treasure";
	public static final String PAUSE_WHEN_MOUSE_IDLE = "pause_when_mouse_idle";
	public static final String EXACT_HORSE_HEALTH = "exact_horse_health";
	public static final String SOVEREIGN_VOID = "sovereign_void";
	public static final String ARCANE_REDSTONE = "arcane_redstone";
	private static final int CURRENT_CONFIG_VERSION = 41;
	private static final String PILLAGER_WEALTH = "dropcontrol:pillager_wealth";
	private static final String LEGACY_PILLAGER_EMERALDS = "dropcontrol:pillager_emeralds";
	private static final String ZOMBIE_SULFUR = "dropcontrol:zombie_sulfur";
	private static final String ZOMBIE_POISONOUS_POTATO = "dropcontrol:zombie_poisonous_potato";
	private static final String DROWNED_TROPICAL_FISH = "dropcontrol:drowned_tropical_fish";
	private static final String DROWNED_KELP = "dropcontrol:drowned_kelp";
	private static final String ENDERMAN_AMETHYST_SHARD = "dropcontrol:enderman_amethyst_shard";
	private static final String ENDERMAN_PARTICLES = "dropcontrol:enderman_particles";
	private static final String WITCH_WART = "dropcontrol:witch_wart";
	private static final String WARDEN_HORN = "dropcontrol:warden_horn";
	private static final String PILLAGER_APPLE = "dropcontrol:pillager_apple";
	private static final String PHANTOM_GLOW_INK_SAC = "dropcontrol:phantom_glow_ink_sac";
	private static final String PILLAGER_CROSSBOW = "dropcontrol:pillager_crossbow";
	private static final String SKELETON_ARMOR = "dropcontrol:skeleton_armor";
	private static final String LEGACY_SKELETON_BOW = "dropcontrol:skeleton_bow";
	private static final String ZOMBIE_ARMOR = "dropcontrol:zombie_armor";
	private static final String LEGACY_ZOMBIE_WEAPONS = "dropcontrol:zombie_weapons";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dropcontrol.json");
	private static final Set<String> ADDED_MARKERS = Set.of(
		WITCH_WART,
		WARDEN_HORN
	);
	private static final Set<String> REMOVED_MARKERS = Set.of(
		SKELETON_ARMOR,
		PILLAGER_CROSSBOW,
		ZOMBIE_ARMOR,
		"dropcontrol:witch_all"
	);
	private static final Set<String> AVAILABLE_MARKERS = availableMarkers();
	private static final Set<String> AVAILABLE_OPTIONS = Set.of(
		CONSTANT_THREAT,
		PARKED_SADDLED_HORSES,
		RABBITS_AVOID_FENCES,
		ENDERMEN_DONT_PICK_UP_BLOCKS,
		ETERNAL_RELICS,
		CHESTPLATE_ELYTRA_SWAP,
		INVENTORY_SORTING,
		ASCENDING_TREASURE,
		PAUSE_WHEN_MOUSE_IDLE,
		EXACT_HORSE_HEALTH,
		SOVEREIGN_VOID,
		ARCANE_REDSTONE
	);

	private static volatile Set<String> selectedItems = AVAILABLE_MARKERS;
	private static volatile Set<String> enabledOptions = Set.of();

	private DropControlConfig() {
	}

	public static synchronized void load() {
		if (!Files.exists(CONFIG_PATH)) {
			applyFirstInstallDefaults();
			return;
		}
		try {
			ConfigData data = GSON.fromJson(Files.readString(CONFIG_PATH, StandardCharsets.UTF_8), ConfigData.class);
			boolean needsMigration =
				data == null || data.configVersion == null || data.configVersion < CURRENT_CONFIG_VERSION;
			selectedItems = migrateSelection(data);
			enabledOptions = needsMigration ? migrateOptions(data) : sanitizeOptions(data.enabledOptions);
			if (needsMigration) {
				save(selectedItems, enabledOptions);
			}
		} catch (IOException | JsonParseException exception) {
			applyFirstInstallDefaults();
		}
	}

	public static synchronized boolean saveSelection(Collection<Identifier> itemIds) {
		Set<String> sanitized = sanitize(itemIds.stream().map(Identifier::toString).toList());
		if (!save(sanitized, enabledOptions)) {
			return false;
		}
		selectedItems = sanitized;
		return true;
	}

	public static synchronized boolean saveOptions(Collection<String> optionIds) {
		Set<String> sanitized = sanitizeOptions(optionIds);
		if (!save(selectedItems, sanitized)) {
			return false;
		}
		enabledOptions = sanitized;
		return true;
	}

	public static boolean isOptionEnabled(String optionId) {
		return enabledOptions.contains(optionId);
	}

	public static boolean constantThreat() { return isOptionEnabled(CONSTANT_THREAT); }
	public static boolean saddledHorseStaysPut() { return isOptionEnabled(PARKED_SADDLED_HORSES); }
	public static boolean rabbitsAvoidFences() { return isOptionEnabled(RABBITS_AVOID_FENCES); }
	public static boolean endermenDontPickUpBlocks() { return isOptionEnabled(ENDERMEN_DONT_PICK_UP_BLOCKS); }
	public static boolean eternalRelics() { return isOptionEnabled(ETERNAL_RELICS); }
	public static boolean chestplateElytraSwap() { return isOptionEnabled(CHESTPLATE_ELYTRA_SWAP); }
	public static boolean inventorySorting() { return isOptionEnabled(INVENTORY_SORTING); }
	public static boolean ascendingTreasure() { return isOptionEnabled(ASCENDING_TREASURE); }
	public static boolean pauseWhenMouseIdle() { return isOptionEnabled(PAUSE_WHEN_MOUSE_IDLE); }
	public static boolean exactHorseHealth() { return isOptionEnabled(EXACT_HORSE_HEALTH); }
	public static boolean sovereignVoid() { return isOptionEnabled(SOVEREIGN_VOID); }
	public static boolean arcaneRedstone() { return isOptionEnabled(ARCANE_REDSTONE); }

	public static boolean isSelected(Identifier markerId) {
		return selectedItems.contains(markerId.toString());
	}

	public static int addedRuleCount() {
		return selectedCount(ADDED_MARKERS);
	}

	public static int removedRuleCount() {
		return selectedCount(REMOVED_MARKERS);
	}

	public static int activeRuleCount() {
		return addedRuleCount() + removedRuleCount();
	}

	private static boolean save(Set<String> items, Set<String> options) {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
			ConfigData data = new ConfigData(
				CURRENT_CONFIG_VERSION,
				items.stream().sorted().toList(),
				options.stream().sorted().toList(),
				null,
				null
			);
			Files.writeString(temporaryPath, GSON.toJson(data), StandardCharsets.UTF_8);
			Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException exception) {
			return false;
		}
	}

	private static Set<String> sanitize(Collection<String> ids) {
		LinkedHashSet<String> sanitized = new LinkedHashSet<>();
		if (ids != null) {
			for (String id : ids) {
				if (id != null && AVAILABLE_MARKERS.contains(id)) {
					sanitized.add(id);
				}
			}
		}
		return Set.copyOf(sanitized);
	}

	private static Set<String> sanitizeOptions(Collection<String> ids) {
		if (ids == null) {
			return Set.of();
		}
		return ids.stream()
			.filter(AVAILABLE_OPTIONS::contains)
			.collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	private static Set<String> migrateOptions(ConfigData data) {
		if (data == null) {
			return Set.of();
		}
		LinkedHashSet<String> migrated = new LinkedHashSet<>(sanitizeOptions(data.enabledOptions));
		if (Boolean.TRUE.equals(data.optionOne)) {
			migrated.add(CONSTANT_THREAT);
		}
		if (Boolean.TRUE.equals(data.optionTwo)) {
			migrated.add(PARKED_SADDLED_HORSES);
		}
		return Set.copyOf(migrated);
	}

	private static Set<String> migrateSelection(ConfigData data) {
		if (data == null || data.configVersion == null || data.selectedItems == null) {
			return AVAILABLE_MARKERS;
		}

		LinkedHashSet<String> migrated = new LinkedHashSet<>(sanitize(data.selectedItems));
		if (data.configVersion < 3) {
			migrated.add(PILLAGER_CROSSBOW);
		}
		if (data.configVersion < 4) {
			if (data.selectedItems.contains(SKELETON_ARMOR) || data.selectedItems.contains(LEGACY_SKELETON_BOW)) {
				migrated.add(SKELETON_ARMOR);
			}
			if (data.selectedItems.contains(ZOMBIE_ARMOR) || data.selectedItems.contains(LEGACY_ZOMBIE_WEAPONS)) {
				migrated.add(ZOMBIE_ARMOR);
			}
		}
		if (data.configVersion < 5) {
			migrated.add(PILLAGER_WEALTH);
		}
		if (data.configVersion < 7 && data.selectedItems.contains(LEGACY_PILLAGER_EMERALDS)) {
			migrated.add(PILLAGER_WEALTH);
		}
		if (data.configVersion < 10) {
			migrated.add(ZOMBIE_SULFUR);
		}
		if (data.configVersion < 15) {
			migrated.add(ENDERMAN_AMETHYST_SHARD);
		}
		if (data.configVersion < 18) {
			migrated.add(ZOMBIE_POISONOUS_POTATO);
		}
		if (data.configVersion < 20) {
			migrated.add(WITCH_WART);
		}
		if (data.configVersion < 26) {
			migrated.add(DROWNED_TROPICAL_FISH);
		}
		if (data.configVersion < 27) {
			migrated.add(DROWNED_KELP);
			migrated.add(PILLAGER_APPLE);
			migrated.add(PHANTOM_GLOW_INK_SAC);
		}
		if (data.configVersion < 36) {
			migrated.add(WARDEN_HORN);
		}
		if (data.configVersion < 37) {
			migrated.add(ENDERMAN_PARTICLES);
		}
		if (data.configVersion >= 38 && data.configVersion < 40) {
			migrated.add(SKELETON_ARMOR);
			migrated.add(PILLAGER_CROSSBOW);
			migrated.add(ZOMBIE_ARMOR);
			migrated.add("dropcontrol:witch_all");
		}
		return sanitize(migrated);
	}

	private static int selectedCount(Set<String> markers) {
		int count = 0;
		for (String marker : markers) {
			if (selectedItems.contains(marker)) {
				count++;
			}
		}
		return count;
	}

	private static Set<String> availableMarkers() {
		LinkedHashSet<String> markers = new LinkedHashSet<>(ADDED_MARKERS);
		markers.addAll(REMOVED_MARKERS);
		return Set.copyOf(markers);
	}

	private static void applyFirstInstallDefaults() {
		selectedItems = AVAILABLE_MARKERS;
		enabledOptions = Set.of();
	}

	private record ConfigData(
		Integer configVersion,
		List<String> selectedItems,
		List<String> enabledOptions,
		Boolean optionOne,
		Boolean optionTwo
	) {
	}
}
