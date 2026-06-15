package me.itzme1on.alcocreate;

import me.itzme1on.alcocraftplus.core.registries.ItemsRegistry;
import me.itzme1on.alcocraftplus.core.utils.BeerTypeMapperUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CreateAlcoCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BeerMugFluidCapability {
    public static final int MUG_FLUID_MB = 400;

    private static final ResourceLocation BEER_MUG_CAPS = new ResourceLocation(CreateAlcoCraft.MOD_ID, "beer_mug_fluid");

    private BeerMugFluidCapability() {
    }

    @SubscribeEvent
    public static void onAttachItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (BeerTypeMapperUtil.getBeerType(event.getObject().getItem()) != 0) {
            event.addCapability(BEER_MUG_CAPS, new Provider(event.getObject()));
        }
    }

    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IFluidHandlerItem> handler;

        Provider(ItemStack stack) {
            this.handler = LazyOptional.of(() -> new BeerMugFluidHandler(stack));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.FLUID_HANDLER_ITEM ? handler.cast() : LazyOptional.empty();
        }
    }

    private static final class BeerMugFluidHandler implements IFluidHandlerItem {
        private ItemStack container;

        BeerMugFluidHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            Fluid beer = BeerFluids.sourceForType(BeerTypeMapperUtil.getBeerType(container.getItem()));
            return beer == null ? FluidStack.EMPTY : new FluidStack(beer, MUG_FLUID_MB);
        }

        @Override
        public int getTankCapacity(int tank) {
            return MUG_FLUID_MB;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack resource) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack available = getFluidInTank(0);

            if (resource.isEmpty() || available.isEmpty() || resource.getFluid() != available.getFluid()) {
                return FluidStack.EMPTY;
            }

            return drainMug(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return drainMug(maxDrain, action);
        }

        private FluidStack drainMug(int maxDrain, FluidAction action) {
            FluidStack available = getFluidInTank(0);

            if (available.isEmpty() || container.getCount() != 1 || maxDrain < MUG_FLUID_MB) {
                return FluidStack.EMPTY;
            }

            if (action.execute()) {
                container = new ItemStack(ItemsRegistry.MUG.get());
            }

            return available;
        }
    }
}
