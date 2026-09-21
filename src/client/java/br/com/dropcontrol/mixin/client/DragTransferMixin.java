package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.config.DropControlConfig;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class DragTransferMixin {
	@Unique private boolean dropcontrol$dragging;
	@Unique private final Set<Integer> dropcontrol$visited = new HashSet<>();

	@Shadow
	private Slot getHoveredSlot(double x, double y) {
		throw new AssertionError();
	}

	@Shadow
	protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput input);

	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void dropcontrol$resetClick(MouseButtonEvent event, boolean doubleClick,
		CallbackInfoReturnable<Boolean> callback) {
		dropcontrol$reset();
	}

	// Start only after vanilla has accepted an actual Shift-click on a slot.
	@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;ILnet/minecraft/client/input/MouseButtonEvent;Lnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"))
	private void dropcontrol$beginDrag(Slot slot, int slotId, MouseButtonEvent event, ContainerInput input,
		CallbackInfo callback) {
		if (DropControlConfig.dragTransfer() && input == ContainerInput.QUICK_MOVE
			&& event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.hasShiftDown()
			&& slot != null && slot.isActive()
			&& ((AbstractContainerScreen<?>)(Object)this).getMenu().getCarried().isEmpty()) {
			dropcontrol$dragging = true;
			dropcontrol$visited.add(slotId);
		}
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$transferHovered(MouseButtonEvent event, double deltaX, double deltaY,
		CallbackInfoReturnable<Boolean> callback) {
		if (!dropcontrol$dragging) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		var screen = (AbstractContainerScreen<?>)(Object)this;
		if (!DropControlConfig.dragTransfer() || event.button() != InputConstants.MOUSE_BUTTON_LEFT
			|| !event.hasShiftDown() || !minecraft.isWindowActive() || minecraft.player == null
			|| minecraft.gui.screen() != screen || !screen.getMenu().getCarried().isEmpty()) {
			dropcontrol$reset();
			return;
		}
		Slot slot = getHoveredSlot(event.x(), event.y());
		if (slot != null && slot.isActive() && slot.hasItem() && slot.mayPickup(minecraft.player)
			&& dropcontrol$visited.add(slot.index)) {
			// SDL left mouse is 1; the vanilla container protocol uses 0 for left click.
			slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
		}
		callback.setReturnValue(true);
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"))
	private void dropcontrol$endDrag(MouseButtonEvent event, CallbackInfoReturnable<Boolean> callback) {
		dropcontrol$reset();
	}

	@Unique
	private void dropcontrol$reset() {
		dropcontrol$dragging = false;
		dropcontrol$visited.clear();
	}
}
