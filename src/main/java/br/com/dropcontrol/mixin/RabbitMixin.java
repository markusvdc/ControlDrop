package br.com.dropcontrol.mixin;

import br.com.dropcontrol.config.DropControlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Rabbit.class)
public abstract class RabbitMixin {
	private static final double FENCE_LOOKAHEAD = 1.35;
	private static final double FENCE_SIDE_MARGIN = 0.8;

	@Inject(method = "startJumping", at = @At("HEAD"), cancellable = true)
	private void dropcontrol$avoidJumpingFence(CallbackInfo callback) {
		Rabbit rabbit = (Rabbit)(Object)this;
		if (!DropControlConfig.rabbitsAvoidFences() || !hasFenceAhead(rabbit)) {
			return;
		}

		rabbit.getNavigation().stop();
		callback.cancel();
	}

	private static boolean hasFenceAhead(Rabbit rabbit) {
		MoveControl movement = rabbit.getMoveControl();
		if (!movement.hasWanted()) {
			return false;
		}

		double directionX = movement.getWantedX() - rabbit.getX();
		double directionZ = movement.getWantedZ() - rabbit.getZ();
		double directionLength = Math.sqrt(directionX * directionX + directionZ * directionZ);
		if (directionLength < 1.0E-4) {
			return false;
		}
		directionX /= directionLength;
		directionZ /= directionLength;

		int centerX = rabbit.getBlockX();
		int centerY = rabbit.getBlockY();
		int centerZ = rabbit.getBlockZ();
		for (int y = centerY - 1; y <= centerY; y++) {
			for (int x = centerX - 2; x <= centerX + 2; x++) {
				for (int z = centerZ - 2; z <= centerZ + 2; z++) {
					if (!rabbit.level().getBlockState(new BlockPos(x, y, z)).is(BlockTags.FENCES)) {
						continue;
					}

					double fenceX = x + 0.5 - rabbit.getX();
					double fenceZ = z + 0.5 - rabbit.getZ();
					double forwardDistance = fenceX * directionX + fenceZ * directionZ;
					double sideDistance = Math.abs(fenceX * directionZ - fenceZ * directionX);
					if (forwardDistance > 0.0
						&& forwardDistance <= FENCE_LOOKAHEAD
						&& sideDistance <= FENCE_SIDE_MARGIN) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
