package me.itzme1on.alcocreate;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateAlcoCraft.MOD_ID)
public final class CreateAlcoCraft {
    public static final String MOD_ID = "alcocraftpluscreate";

    public CreateAlcoCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BeerFluids.register(modBus);
    }
}
