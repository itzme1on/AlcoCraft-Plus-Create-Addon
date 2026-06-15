package me.itzme1on.alcocreate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class BeerFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CreateAlcoCraft.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.Keys.FLUIDS, CreateAlcoCraft.MOD_ID);

    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation FLOW_TEXTURE = new ResourceLocation("minecraft", "block/water_flow");

    private static final Map<Integer, RegistryObject<FluidType>> TYPES = new HashMap<>();
    private static final Map<Integer, RegistryObject<FlowingFluid>> SOURCES = new HashMap<>();
    private static final Map<Integer, RegistryObject<FlowingFluid>> FLOWINGS = new HashMap<>();
    private static final Map<Integer, ForgeFlowingFluid.Properties> PROPS = new HashMap<>();

    static {
        beer(1, "sun_pale_ale", 0xE8C24A);
        beer(2, "digger_bitter", 0xB5651D);
        beer(3, "nether_porter", 0x5A1E12);
        beer(4, "wither_stout", 0x241A12);
        beer(5, "magnet_pilsner", 0xE3B23C);
        beer(6, "drowned_ale", 0x3A7D6E);
        beer(7, "night_rauch", 0x3E2C1C);
        beer(8, "ice_beer", 0xAEE0F0);
        beer(9, "kvass", 0x6B3A1E);
        beer(10, "leprechaun_cider", 0x4CAF50);
        beer(11, "chorus_ale", 0x8E5BA6);
        beer(12, "nether_star_lager", 0xD8F0E8);
    }

    private BeerFluids() {
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    public static Fluid sourceForType(int beerType) {
        RegistryObject<FlowingFluid> source = SOURCES.get(beerType);
        return source == null ? null : source.get();
    }

    public static boolean isBeerFluid(Fluid fluid) {
        for (RegistryObject<FlowingFluid> source : SOURCES.values()) {
            if (source.get() == fluid) return true;
        }
        return false;
    }

    private static void beer(int type, String name, int rgb) {
        TYPES.put(type, FLUID_TYPES.register(name, () -> new FluidType(
                FluidType.Properties.create().density(1100).viscosity(1100)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL_TEXTURE;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOW_TEXTURE;
                    }

                    @Override
                    public int getTintColor() {
                        return 0xFF000000 | rgb;
                    }
                });
            }
        }));

        SOURCES.put(type, FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(props(type))));
        FLOWINGS.put(type, FLUIDS.register(name + "_flowing", () -> new ForgeFlowingFluid.Flowing(props(type))));
    }

    private static ForgeFlowingFluid.Properties props(int type) {
        return PROPS.computeIfAbsent(type, t ->
                new ForgeFlowingFluid.Properties(TYPES.get(t), SOURCES.get(t), FLOWINGS.get(t)));
    }
}
