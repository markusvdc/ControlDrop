package br.com.dropcontrol.client.screen.component;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class AlphabeticalOrder {
	private AlphabeticalOrder() {
	}

	static Comparator<Component> components(Minecraft minecraft) {
		String languageCode = minecraft.getLanguageManager().getSelected();
		Collator collator = Collator.getInstance(Locale.forLanguageTag(languageCode.replace('_', '-')));
		collator.setStrength(Collator.PRIMARY);
		return (first, second) -> collator.compare(first.getString(), second.getString());
	}
}
