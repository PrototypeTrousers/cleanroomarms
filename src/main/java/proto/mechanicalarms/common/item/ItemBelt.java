package proto.mechanicalarms.common.item;

import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.tile.Slope;
import proto.mechanicalarms.common.tile.TileArmBasic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
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

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileBeltBasic tbbte) {
                EnumFacing playerFacing = player.getHorizontalFacing();
                boolean connectedToBelt = tryConnectToAdjacentBelt(world, pos, tbbte, playerFacing);

                if (!connectedToBelt) {
                    tbbte.setFront(playerFacing);
                    determineSlope(tbbte, side, hitY, player, pos);
                }
                return true;
            }
        }
        return false;
    }

    private boolean tryConnectToAdjacentBelt(World world, BlockPos pos, TileBeltBasic tbbte, EnumFacing facing) {
        TileEntity forwardAbove = world.getTileEntity(pos.offset(facing).up());
        if (forwardAbove instanceof TileBeltBasic fb) {
            if (fb.getFront() == facing) {
                tbbte.setFront(facing);
                tbbte.setSlope(EnumFacing.UP);
                return true;
            }
        }
        TileEntity oppositeAbove = world.getTileEntity(pos.offset(facing.getOpposite()).up());
        if (oppositeAbove instanceof TileBeltBasic ob) {
            if (ob.getFront() == facing) {
                tbbte.setFront(facing);
                tbbte.setSlope(EnumFacing.DOWN);
                return true;
            }
        }
        TileEntity backwardsAbove = world.getTileEntity(pos.offset(facing.getOpposite()).up());
        if (backwardsAbove instanceof TileBeltBasic ba) {
            if (ba.getFront() == facing) {
                tbbte.setFront(facing);
                tbbte.setSlope(EnumFacing.DOWN);
                return true;
            }
        }

        return false;
    }

    private void determineSlope(TileBeltBasic tbbte, EnumFacing side, float hitY, EntityPlayer player, BlockPos pos) {
        if (side.getHorizontalIndex() != -1 ) {
            if (hitY >= 0.5f) {
                tbbte.setSlope(EnumFacing.UP);
            } else {
                tbbte.setSlope(EnumFacing.DOWN);
            }
        } else {
            tbbte.setSlope(tbbte.getFront());
        }
    }
}
