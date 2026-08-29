package br.com.dropcontrol.mixin.client;

import br.com.dropcontrol.block.SpiderSkullType;
import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
	@Shadow
	@Final
	private static Map<SkullBlock.Type, Identifier> SKIN_BY_TYPE;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void dropcontrol$registerSpiderHeadTexture(CallbackInfo callback) {
		SKIN_BY_TYPE.put(
			SpiderSkullType.INSTANCE,
			Identifier.fromNamespaceAndPath("dropcontrol", "textures/entity/spider_head.png")
		);
	}

	@Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
	private static void dropcontrol$createSpiderHeadModel(
		EntityModelSet modelSet,
		SkullBlock.Type type,
		CallbackInfoReturnable<SkullModelBase> callback
	) {
		if (type == SpiderSkullType.INSTANCE) {
			callback.setReturnValue(new SkullModel(modelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
		}
	}
}
