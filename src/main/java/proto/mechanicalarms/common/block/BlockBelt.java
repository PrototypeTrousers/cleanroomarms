package proto.mechanicalarms.common.block;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.jetbrains.annotations.NotNull;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.block.properties.PropertyBeltDirection;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import javax.annotation.Nullable;

public class BlockBelt extends Block implements ITileEntityProvider {

    AxisAlignedBB boundBox = new AxisAlignedBB(0, 0, 0, 1, 0.2F, 1);
    public static final PropertyBeltDirection FACING = PropertyBeltDirection.create("facing");


    public BlockBelt() {
        super(Material.IRON);
        setRegistryName(MechanicalArms.MODID, "belt_basic");
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, Directions.NORTH));
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        EnumFacing facing = state.getValue(FACING).getHorizontalFacing();
        TileEntity te =
                switch (state.getValue(FACING).getRelativeHeight()) {
                    case ABOVE -> worldIn.getTileEntity(pos.offset(facing).up());
                    case LEVEL -> worldIn.getTileEntity(pos.offset(facing));
                    case BELOW -> worldIn.getTileEntity(pos.offset(facing).down());
                };
        if (te instanceof TileBeltBasic tbb) {
            tbb.updateConnected();
        }
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
        super.onPlayerDestroy(worldIn, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileBeltBasic tbb = new TileBeltBasic();
        tbb.setDirection(Directions.VALUES[meta]);
        return tbb;
    }

    @Override
    public int getMetaFromState(IBlockState blockState) {
        return blockState.getValue(FACING).ordinal();
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, Directions.VALUES[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @NotNull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing playerFacing = placer.getHorizontalFacing();

        boolean connectedToBelt = false;
        Directions placementDirection = null;
        TileEntity forwardAbove = world.getTileEntity(pos.offset(playerFacing).up());
        if (forwardAbove instanceof TileBeltBasic fb) {
            if (fb.getFront() == playerFacing) {
                placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.ABOVE));
            connectedToBelt = true;
            }
        }
        TileEntity oppositeAbove = world.getTileEntity(pos.offset(playerFacing.getOpposite()).up());
        if (oppositeAbove instanceof TileBeltBasic ob) {
            if (ob.getFront() == playerFacing) {
                placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.ABOVE));
                connectedToBelt = true;
            }
        }
        TileEntity backwardsAbove = world.getTileEntity(pos.offset(playerFacing.getOpposite()).up());
        if (backwardsAbove instanceof TileBeltBasic ba) {
            if (ba.getFront() == playerFacing) {
                placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.BELOW));
                connectedToBelt = true;
            }
        }


        if (!connectedToBelt) {

            if (facing.getHorizontalIndex() != -1) {
                if (hitY - (int) hitY >= 0.5f) {
                    placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.ABOVE));
                } else {
                    placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.BELOW));
                }
            } else {
                placementDirection = (Directions.getFromFacingAndLevel(playerFacing, Directions.RelativeHeight.LEVEL));
            }
        }

        return getDefaultState().withProperty(FACING, placementDirection);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileBeltBasic tileBeltBasic) {
//            IItemHandler side = tileBeltBasic.getMainItemHandler();
//            for (int s = 0; s < side.getSlots(); s++) {
//                if (side.getStackInSlot(s).isEmpty()) {
//                    continue;
//                }
//                drops.add(side.getStackInSlot(s).copy());
//            }
//            side = tileBeltBasic.getSideItemHandler();
//            for (int s = 0; s < side.getSlots(); s++) {
//                if (side.getStackInSlot(s).isEmpty()) {
//                    continue;
//                }
//                drops.add(side.getStackInSlot(s).copy());
//            }
        }
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!playerIn.getHeldItem(hand).isEmpty()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileBeltBasic tbb) {
                tbb.getLogic().getLeftItemHandler().setStackInSlot(0, playerIn.getHeldItem(hand).copy());
            }
        } else {
            if (!worldIn.isRemote) {
                TileEntityGuiFactory.INSTANCE.open(playerIn, pos);
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileBeltBasic tbb) {
            tbb.updateConnected();
            tbb.updateRedstone();
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return boundBox;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (state.getValue(FACING).getRelativeHeight() == Directions.RelativeHeight.ABOVE) {
            EnumFacing facing = state.getValue(FACING).getHorizontalFacing();
            TileEntity te = worldIn.getTileEntity(pos.offset(facing).up());
            if (te instanceof TileBeltBasic tbb) {
                tbb.updateConnected();
            }
        }
    }
}
