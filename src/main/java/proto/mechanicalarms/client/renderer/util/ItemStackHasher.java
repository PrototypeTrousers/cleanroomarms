package proto.mechanicalarms.client.renderer.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

public class ItemStackHasher implements Hash.Strategy<ItemStack> {
    @Override
    public int hashCode(ItemStack o) {
        int hash = o.getItem().hashCode();
        hash += 31 * o.getMetadata();
        hash += 31 * o.getItemDamage();
        if (o.getTagCompound() != null) {
            hash += 31 * o.getTagCompound().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        } if (a == null || b == null) {
            return false;
        }
        return (a.getItem() == b.getItem() &&
                a.getMetadata() == b.getMetadata() &&
                a.getItemDamage() == b.getItemDamage() &&
                ItemStack.areItemStackTagsEqual(a,b));
    }
}
