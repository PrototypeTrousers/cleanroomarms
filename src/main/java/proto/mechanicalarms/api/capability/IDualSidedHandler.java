package proto.mechanicalarms.api.capability;

import net.minecraft.item.ItemStack;

public interface IDualSidedHandler {
    ItemStack insertLeft(ItemStack insert, boolean simulate);
    ItemStack insertRight(ItemStack insert, boolean simulate);
}
