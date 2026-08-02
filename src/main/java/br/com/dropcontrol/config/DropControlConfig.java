package br.com.dropcontrol.config;

import br.com.dropcontrol.DropControl;
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
	public static final String CHESTPLATE_ELYTRA_SWAP = "chestplate_elytra_swap";
	public static final String PAUSE_WHEN_MOUSE_IDLE = "pause_when_mouse_idle";
	public static final String EXACT_HORSE_HEALTH = "exact_horse_health";
	private static final int CURRENT_CONFIG_VERSION = 3;
	private static final String PILLAGER_CROSSBOW = "dropcontrol:pillager_crossbow";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dropcontrol.json");
	private static final Set<String> ADDED_MARKERS = Set.of(
		"dropcontrol:witch_potions",
		"dropcontrol:creeper_tnt"
	);
	private static final Set<String> REMOVED_MARKERS = Set.of(
		"dropcontrol:skeleton_bow",
		"dropcontrol:skeleton_armor",
		PILLAGER_CROSSBOW,
		"dropcontrol:zombie_armor",
		"dropcontrol:zombie_weapons",
		"dropcontrol:witch_all"
	);
	private static final Set<String> AVAILABLE_MARKERS = availableMarkers();
	private static final Set<String> AVAILABLE_OPTIONS = Set.of(
		CONSTANT_THREAT,
		PARKED_SADDLED_HORSES,
		RABBITS_AVOID_FENCES,
		ENDERMEN_DONT_PICK_UP_BLOCKS,
		CHESTPLATE_ELYTRA_SWAP,
		PAUSE_WHEN_MOUSE_IDLE,
		EXACT_HORSE_HEALTH
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
			DropControl.LOGGER.error("Could not load {}. Restoring UI defaults.", CONFIG_PATH, exception);
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
	public static boolean chestplateElytraSwap() { return isOptionEnabled(CHESTPLATE_ELYTRA_SWAP); }
	public static boolean pauseWhenMouseIdle() { return isOptionEnabled(PAUSE_WHEN_MOUSE_IDLE); }
	public static boolean exactHorseHealth() { return isOptionEnabled(EXACT_HORSE_HEALTH); }

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
			DropControl.LOGGER.error("Could not save {}.", CONFIG_PATH, exception);
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
		LinkedHashSet<String> migrated = new LinkedHashSet<>();
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
		return Set.copyOf(migrated);
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
