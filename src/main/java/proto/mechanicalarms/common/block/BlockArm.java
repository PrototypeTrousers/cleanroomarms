package proto.mechanicalarms.common.block;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import org.jetbrains.annotations.NotNull;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.common.tile.TileArmBasic;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockArm extends Block implements ITileEntityProvider {

    public BlockArm() {
        super(Material.IRON);
        setRegistryName(MechanicalArms.MODID, "arm_basic");
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
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
        return new TileArmBasic();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileArmBasic) {
            TileArmBasic tileArmBasic = (TileArmBasic) te;
            if (!tileArmBasic.getItemStack().isEmpty()) {
                drops.add(tileArmBasic.getItemStack().copy());
            }
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
