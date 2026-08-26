package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Rabbit.class)
public abstract class RabbitMixin {
	private static final double FENCE_LOOKAHEAD = 1.35;
	private static final double FENCE_SIDE_MARGIN = 0.8;
	private static final int FENCE_ESCAPE_TICKS = 30;
	private static final int FENCE_STUCK_ATTEMPTS = 3;
	private static final int FENCE_STUCK_ATTEMPT_WINDOW = 40;
	private static final double FENCE_STUCK_DISTANCE_SQR = 0.0625;

	@Unique
	private int dropcontrol$fenceEscapeTicks;
	@Unique
	private int dropcontrol$blockedFenceJumps;
	@Unique
	private double dropcontrol$lastBlockedFenceX;
	@Unique
	private double dropcontrol$lastBlockedFenceZ;
	@Unique
	private int dropcontrol$lastBlockedFenceTick;

	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void dropcontrol$tickFenceEscape(ServerLevel level, CallbackInfo callback) {
		if (!DropControlConfig.rabbitsAvoidFences()) {
			dropcontrol$fenceEscapeTicks = 0;
			dropcontrol$blockedFenceJumps = 0;
			return;
		}
		if (dropcontrol$fenceEscapeTicks > 0) {
			dropcontrol$fenceEscapeTicks--;
		}
	}

	@Inject(method = "startJumping", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$avoidJumpingFence(CallbackInfo callback) {
		Rabbit rabbit = (Rabbit)(Object)this;
		if (!DropControlConfig.rabbitsAvoidFences()) {
			return;
		}
		if (dropcontrol$fenceEscapeTicks > 0) {
			return;
		}

		BlockPos fence = findFenceAhead(rabbit);
		if (fence == null) {
			dropcontrol$blockedFenceJumps = 0;
			return;
		}

		double blockedMovementX = rabbit.getX() - dropcontrol$lastBlockedFenceX;
		double blockedMovementZ = rabbit.getZ() - dropcontrol$lastBlockedFenceZ;
		if (dropcontrol$blockedFenceJumps == 0
			|| rabbit.tickCount - dropcontrol$lastBlockedFenceTick > FENCE_STUCK_ATTEMPT_WINDOW
			|| blockedMovementX * blockedMovementX + blockedMovementZ * blockedMovementZ > FENCE_STUCK_DISTANCE_SQR) {
			dropcontrol$blockedFenceJumps = 1;
		} else {
			dropcontrol$blockedFenceJumps++;
		}
		dropcontrol$lastBlockedFenceX = rabbit.getX();
		dropcontrol$lastBlockedFenceZ = rabbit.getZ();
		dropcontrol$lastBlockedFenceTick = rabbit.tickCount;

		if (dropcontrol$blockedFenceJumps < FENCE_STUCK_ATTEMPTS) {
			rabbit.getNavigation().stop();
			callback.cancel();
			return;
		}
		dropcontrol$blockedFenceJumps = 0;
		dropcontrol$fenceEscapeTicks = FENCE_ESCAPE_TICKS;
	}

	private static BlockPos findFenceAhead(Rabbit rabbit) {
		MoveControl movement = rabbit.getMoveControl();
		if (!movement.hasWanted()) {
			return null;
		}

		double directionX = movement.getWantedX() - rabbit.getX();
		double directionZ = movement.getWantedZ() - rabbit.getZ();
		double directionLength = Math.sqrt(directionX * directionX + directionZ * directionZ);
		if (directionLength < 1.0E-4) {
			return null;
		}
		directionX /= directionLength;
		directionZ /= directionLength;

		int centerX = rabbit.getBlockX();
		int centerY = rabbit.getBlockY();
		int centerZ = rabbit.getBlockZ();
		BlockPos closestFence = null;
		double closestDistanceSqr = Double.MAX_VALUE;
		for (int y = centerY - 1; y <= centerY; y++) {
			for (int x = centerX - 2; x <= centerX + 2; x++) {
				for (int z = centerZ - 2; z <= centerZ + 2; z++) {
					BlockPos fence = new BlockPos(x, y, z);
					if (!rabbit.level().getBlockState(fence).is(BlockTags.FENCES)) {
						continue;
					}

					double fenceX = x + 0.5 - rabbit.getX();
					double fenceZ = z + 0.5 - rabbit.getZ();
					double forwardDistance = fenceX * directionX + fenceZ * directionZ;
					double sideDistance = Math.abs(fenceX * directionZ - fenceZ * directionX);
					if (forwardDistance > 0.0
						&& forwardDistance <= FENCE_LOOKAHEAD
						&& sideDistance <= FENCE_SIDE_MARGIN
						&& fenceX * fenceX + fenceZ * fenceZ < closestDistanceSqr) {
						closestFence = fence;
						closestDistanceSqr = fenceX * fenceX + fenceZ * fenceZ;
					}
				}
			}
		}
		return closestFence;
	}
}
