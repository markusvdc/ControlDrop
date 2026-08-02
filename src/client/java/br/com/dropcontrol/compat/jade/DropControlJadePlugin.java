package br.com.dropcontrol.compat.jade;

import net.minecraft.world.entity.animal.equine.Horse;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class DropControlJadePlugin implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerEntityComponent(ExactHorseHealthProvider.INSTANCE, Horse.class);
	}
}
