package br.com.dropcontrol.mixin;

import br.com.dropcontrol.access.PhantomBiteAccess;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Phantom.class)
public abstract class PhantomMixin implements PhantomBiteAccess {
	@Unique
	private static final String DROP_CONTROL_BITTEN_PLAYER_KEY = "dropcontrol_bitten_player";

	@Unique
	private boolean dropcontrol$bittenPlayer;

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void dropcontrol$readPlayerBite(ValueInput input, CallbackInfo callback) {
		this.dropcontrol$bittenPlayer = input.getBooleanOr(DROP_CONTROL_BITTEN_PLAYER_KEY, false);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void dropcontrol$savePlayerBite(ValueOutput output, CallbackInfo callback) {
		output.putBoolean(DROP_CONTROL_BITTEN_PLAYER_KEY, this.dropcontrol$bittenPlayer);
	}

	@Override
	public boolean dropcontrol$hasBittenPlayer() {
		return this.dropcontrol$bittenPlayer;
	}

	@Override
	public void dropcontrol$markPlayerBitten(Player player) {
		if (!this.dropcontrol$bittenPlayer) {
			this.dropcontrol$bittenPlayer = true;
		}
	}
}
