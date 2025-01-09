package proto.mechanicalarms.common.item;

import proto.mechanicalarms.MechanicalArms;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.tile.TileBeltBasic;

public class ItemBelt extends ItemBlock {

    public ItemBelt(Block block) {
        super(block);
        setRegistryName(MechanicalArms.MODID, "belt_basic");
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
