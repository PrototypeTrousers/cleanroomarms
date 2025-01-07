package proto.mechanicalarms.common.cap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import proto.mechanicalarms.api.capability.IDualSidedHandler;

import javax.annotation.Nullable;

public class CapabilityDualSidedHandler {
    @CapabilityInject(IDualSidedHandler.class)
    public static Capability<IDualSidedHandler> DUAL_SIDED_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IDualSidedHandler.class, new DefaultStorage(), DefaultImplementation::new);
    }

    private static class DefaultStorage implements Capability.IStorage<IDualSidedHandler> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IDualSidedHandler> capability, IDualSidedHandler instance, EnumFacing side) {
            if (instance instanceof INBTSerializable serializable) {
                return serializable.serializeNBT();
            }
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IDualSidedHandler> capability, IDualSidedHandler instance, EnumFacing side, NBTBase nbt) {
            if (instance instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(nbt);
            }
        }
    }

    private static class DefaultImplementation implements IDualSidedHandler {
        @Override
        public ItemStack insertLeft(ItemStack insert, boolean simulate) {
            return insert;
        }

        @Override
        public ItemStack insertRight(ItemStack insert, boolean simulate) {
            return insert;
        }
    }
}
