package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.client.MatchingTransfer;
import br.com.dropcontrol.config.DropControlConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class MatchingTransferKeyMixin {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void dropcontrol$transferMatching(long window, int action, KeyEvent event, CallbackInfo callback) {
		Minecraft minecraft = Minecraft.getInstance();
		if (window == 0 || window != minecraft.getWindow().handle() || action != InputConstants.PRESS
			|| !DropControlConfig.matchingTransfer() || !event.hasControlDown()
			|| !(minecraft.gui.screen() instanceof AbstractContainerScreen<?>)) {
			return;
		}
		LogUtils.getLogger().info("[ControlDrop] Ctrl container key: physical={}, logical={}, modifiers={}",
			event.key(), event.keycode(), event.modifiers());
		// Use modifiers captured with the key event, not a later keyboard-state poll.
		boolean gravePressed = event.key() == InputConstants.KEY_GRAVE || event.keycode() == '`';
		boolean controlPressed = event.key() == InputConstants.KEY_LCONTROL || event.key() == InputConstants.KEY_RCONTROL;
		if ((gravePressed || (controlPressed && InputConstants.isKeyDown(InputConstants.KEY_GRAVE)))
			&& !event.hasShiftDown() && !event.hasAltDown()) {
			MatchingTransfer.request(minecraft);
		}
	}
}
