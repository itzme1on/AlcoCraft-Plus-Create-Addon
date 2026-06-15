package me.itzme1on.alcocreate;

import me.itzme1on.alcocraftplus.core.blocks.keg.KegEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CreateAlcoCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KegFluidCapability {
    private static final ResourceLocation KEG_FLUID = new ResourceLocation(CreateAlcoCraft.MOD_ID, "keg_fluid");

    private KegFluidCapability() {
    }

    @SubscribeEvent
    public static void onAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof KegEntity keg) {
            Provider provider = new Provider(keg);
            event.addCapability(KEG_FLUID, provider);
            event.addListener(provider::invalidate);
        }
    }

    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IFluidHandler> fluid;

        Provider(KegEntity keg) {
            this.fluid = LazyOptional.of(() -> new KegFluidHandler(keg));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.FLUID_HANDLER ? fluid.cast() : LazyOptional.empty();
        }

        void invalidate() {
            fluid.invalidate();
        }
    }

    private static final class KegFluidHandler implements IFluidHandler {
        private static final int WATER_TANK = 0;
        private static final int BEER_TANK = 1;

        private final KegEntity keg;

        KegFluidHandler(KegEntity keg) {
            this.keg = keg;
        }

        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank == WATER_TANK) {
                return new FluidStack(Fluids.WATER, keg.getWaterMb());
            }

            Fluid beer = BeerFluids.sourceForType(keg.beerType);
            if (beer == null || keg.getBeerMb() <= 0) return FluidStack.EMPTY;
            return new FluidStack(beer, keg.getBeerMb());
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == WATER_TANK ? keg.getWaterCapacityMb() : keg.getBeerCapacityMb();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == WATER_TANK ? stack.getFluid() == Fluids.WATER : BeerFluids.isBeerFluid(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != Fluids.WATER) return 0;
            return keg.fillWaterMb(resource.getAmount(), action.execute());
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;

            Fluid beer = BeerFluids.sourceForType(keg.beerType);
            if (beer == null || resource.getFluid() != beer) return FluidStack.EMPTY;

            return drainBeer(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return drainBeer(maxDrain, action);
        }

        private FluidStack drainBeer(int maxDrain, FluidAction action) {
            Fluid beer = BeerFluids.sourceForType(keg.beerType);
            if (beer == null || keg.getBeerMb() <= 0) return FluidStack.EMPTY;

            int drained = keg.drainBeerMb(maxDrain, action.execute());
            if (drained <= 0) return FluidStack.EMPTY;

            return new FluidStack(beer, drained);
        }
    }
}
