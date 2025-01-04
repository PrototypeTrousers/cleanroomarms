package proto.mechanicalarms.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.block.BlockSplitter;
import proto.mechanicalarms.common.block.Blocks;
import proto.mechanicalarms.common.tile.TileSplitter;
import proto.mechanicalarms.common.tile.TileSplitterDummy;

public class ItemSplitter extends ItemBlock {

    public ItemSplitter(Block block) {
        super(block);
        setRegistryName(MechanicalArms.MODID, "splitter");
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            boolean flag = block.isReplaceable(worldIn, pos);

            if (!flag) {
                pos = pos.up();
            }

            EnumFacing playerFacing = player.getHorizontalFacing();
            BlockPos dummyPos = pos.offset(playerFacing.rotateY());
            ItemStack itemstack = player.getHeldItem(hand);

            if (player.canPlayerEdit(pos, facing, itemstack) && player.canPlayerEdit(dummyPos, facing, itemstack)) {
                IBlockState iblockstate1 = worldIn.getBlockState(dummyPos);
                boolean flag1 = iblockstate1.getBlock().isReplaceable(worldIn, dummyPos);
                boolean flag2 = flag || worldIn.isAirBlock(pos);
                boolean flag3 = flag1 || worldIn.isAirBlock(dummyPos);

                if (flag2 && flag3 && worldIn.getBlockState(pos.down()).isTopSolid() && worldIn.getBlockState(dummyPos.down()).isTopSolid()) {
                    IBlockState iblockstate2 = Blocks.SPLITTER.getDefaultState();
                    worldIn.setBlockState(dummyPos, iblockstate2.withProperty(BlockSplitter.controller, false), 10);
                    worldIn.setBlockState(pos, iblockstate2.withProperty(BlockSplitter.controller, true), 10);
                    SoundType soundtype = iblockstate2.getBlock().getSoundType(iblockstate2, worldIn, pos, player);
                    worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    TileEntity tileentity1 = worldIn.getTileEntity(pos);

                    if (tileentity1 instanceof TileSplitter tileSplitter) {
                        tileSplitter.setFront(playerFacing);


                        TileEntity tileentity = worldIn.getTileEntity(dummyPos);

                        if (tileentity instanceof TileSplitterDummy splitterDummy) {
                            splitterDummy.setFront(playerFacing);
                            splitterDummy.setController(tileSplitter);
                        }
                    }

                    worldIn.notifyNeighborsRespectDebug(pos, block, false);
                    worldIn.notifyNeighborsRespectDebug(dummyPos, iblockstate1.getBlock(), false);

                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
                    }

                    itemstack.shrink(1);
                    return EnumActionResult.SUCCESS;
                } else {
                    return EnumActionResult.FAIL;
                }
            } else {
                return EnumActionResult.FAIL;
            }
        }
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileSplitter splitter) {
                EnumFacing playerFacing = player.getHorizontalFacing();
                splitter.setFront(playerFacing);
                return true;
            }
        }
        return false;
    }
}
