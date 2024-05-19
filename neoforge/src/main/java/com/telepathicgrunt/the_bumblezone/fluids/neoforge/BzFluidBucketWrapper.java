package com.telepathicgrunt.the_bumblezone.fluids.neoforge;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class BzFluidBucketWrapper implements IFluidHandlerItem {
    protected ItemStack container;

    public BzFluidBucketWrapper(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return !fluid.getFluidType().getBucket(fluid).isEmpty();
    }

    public FluidStack getFluid() {
        Item item = container.getItem();
        if (item instanceof BucketItem) {
            return new FluidStack(((BucketItem) item).content, FluidType.BUCKET_VOLUME);
        }
        else {
            return FluidStack.EMPTY;
        }
    }

    protected void setFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            container = new ItemStack(Items.BUCKET);
        }
        else {
            container = FluidUtil.getFilledBucket(fluidStack);
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME || !getFluid().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        if (action.execute()) {
            setFluid(resource);
        }

        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }

        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }

        return FluidStack.EMPTY;
    }
}
