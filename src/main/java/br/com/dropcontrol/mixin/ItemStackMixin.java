package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.BiConsumer;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract int getMaxDamage();

	@Shadow
	public abstract int getDamageValue();

	@Shadow
	public abstract boolean isDamageableItem();

	@ModifyArg(
		method = "applyDamage",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"),
		index = 0
	)
	private int dropcontrol$preserveLastDurability(int damageValue) {
		if (!DropControlConfig.eternalRelics()) {
			return damageValue;
		}
		return Math.min(damageValue, this.getMaxDamage() - 1);
	}

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableEntityInteraction(
		Player player,
		LivingEntity target,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableCorrectTool(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableWeaponEffects(
		LivingEntity target,
		LivingEntity attacker,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (this.dropcontrol$isProtectedBroken()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "mineBlock", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$disableMiningEffects(Level level, BlockState state, BlockPos pos, Player player, CallbackInfo ci) {
		if (this.dropcontrol$isProtectedBroken()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void dropcontrol$disableGroupedAttributes(
		net.minecraft.world.entity.EquipmentSlotGroup slotGroup,
		TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer,
		CallbackInfo ci
	) {
		if (this.dropcontrol$isProtectedBroken()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void dropcontrol$disableEquipmentAttributes(
		EquipmentSlot slot,
		BiConsumer<Holder<Attribute>, AttributeModifier> consumer,
		CallbackInfo ci
	) {
		if (this.dropcontrol$isProtectedBroken()) {
			ci.cancel();
		}
	}

	private boolean dropcontrol$isProtectedBroken() {
		return DropControlConfig.eternalRelics()
			&& this.isDamageableItem()
			&& this.getDamageValue() >= this.getMaxDamage() - 1;
	}
}
