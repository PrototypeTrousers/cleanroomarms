package proto.mechanicalarms.common.block;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.tile.TileBeltBasic;
import proto.mechanicalarms.common.tile.TileSplitter;
import proto.mechanicalarms.common.tile.TileSplitterDummy;

import javax.annotation.Nullable;

public class BlockSplitter extends Block implements ITileEntityProvider {

    public static final PropertyBool controller = PropertyBool.create("controller");
    public static final PropertyDirection facing = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockSplitter() {
        super(Material.IRON);
        setRegistryName(MechanicalArms.MODID, "splitter");
        setDefaultState(this.blockState.getBaseState().withProperty(controller, false).withProperty(facing, EnumFacing.NORTH));
    }

    @NotNull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(controller, (meta >> 2) == 1)
                .withProperty(facing, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState blockState) {
        int meta = 0;
        if (blockState.getValue(controller)) {
            meta |= 1 << 2; // set the 3rd bit if controller is true
        }
        meta |= blockState.getValue(facing).getHorizontalIndex() & 3; // set the lower 2 bits to facing
        return meta;
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, controller, facing);
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState blockState) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getValue(controller)) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileSplitter ts) {
                worldIn.setBlockToAir(pos.offset(ts.getFront().rotateY()));
            }
        } else{
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileSplitterDummy ts) {
                worldIn.setBlockToAir(pos.offset(ts.getFront().rotateYCCW()));
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(BlockSplitter.facing, facing).withProperty(controller, (meta >> 2) == 1);
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
        if ((meta >> 2) == 1) {
            TileSplitter ts =  new TileSplitter();
            ts.setDirection(Directions.getFromHorizontalFacing(EnumFacing.byHorizontalIndex(meta & 3)));
            return ts;
        }
        TileSplitterDummy tileSplitterDummy = new TileSplitterDummy();
        tileSplitterDummy.setDirection(Directions.getFromHorizontalFacing(EnumFacing.byHorizontalIndex(meta & 3)));

        return tileSplitterDummy;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSplitter tileSplitter) {
//            if (!tileSpliiter.getItemStack().isEmpty()) {
//                drops.add(tileSpliiter.getItemStack().copy());
//            }
        }
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileSplitter) {
                TileEntityGuiFactory.open(playerIn, pos);
            } else if (te instanceof TileSplitterDummy tsd) {
                TileEntityGuiFactory.open(playerIn, pos.offset(tsd.getFront().rotateYCCW()));
            }
        }
        return true;
    }
}
