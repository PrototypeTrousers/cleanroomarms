package proto.mechanicalarms.common.block;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
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
import proto.mechanicalarms.common.tile.TileSplitter;
import proto.mechanicalarms.common.tile.TileSplitterDummy;

import javax.annotation.Nullable;

public class BlockSplitter extends Block implements ITileEntityProvider {

    public static final PropertyBool controller = PropertyBool.create("controller");

    public BlockSplitter() {
        super(Material.IRON);
        setRegistryName(MechanicalArms.MODID, "splitter");
        setDefaultState(this.blockState.getBaseState().withProperty(controller, false));
    }

    @NotNull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(controller, meta == 1);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, controller);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState blockState) {
        return blockState.getValue(controller) ? 1 : 0;
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
//        if (state.getValue(controller)) {
//            TileEntity te = worldIn.getTileEntity(pos);
//            if (te instanceof TileSplitter ts) {
//                worldIn.setBlockToAir(pos.offset(ts.getFront().rotateY()));
//            }
//        } else{
//            for (EnumFacing e : EnumFacing.HORIZONTALS) {
//                BlockPos p = pos.offset(e);
//                TileEntity te = worldIn.getTileEntity(p);
//                if (te instanceof TileSplitter ts) {
//                    if (ts.getFront() == e.rotateY()) {
//                        worldIn.setBlockToAir(pos.offset(e));
//                    }
//                }
//            }
//        }
        super.breakBlock(worldIn, pos, state);
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
        if (meta == 1) {
            return new TileSplitter();
        }
        return new TileSplitterDummy();
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
            TileEntityGuiFactory.open(playerIn, pos);
        }
        return true;
    }
}
