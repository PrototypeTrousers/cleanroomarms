package proto.mechanicalarms.common.item;

import proto.mechanicalarms.MechanicalArms;
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

public class ItemArm extends ItemBlock {

    Pair<BlockPos, EnumFacing> source;
    Pair<BlockPos, EnumFacing> target;

    public ItemArm(Block armBase) {
        super(armBase);
        setRegistryName(MechanicalArms.MODID, "arm_basic");
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity != null) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                this.source = Pair.of(pos, facing);
                MechanicalArms.logger.info("Source pos set to " + source);
                return EnumActionResult.FAIL;
            }
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (source != null && target != null && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            TileEntity tileEntity1 = world.getTileEntity(pos);
            if (tileEntity1 != null) {
                ((TileArmBasic) tileEntity1).setSource(source.getKey(), source.getValue());
                ((TileArmBasic) tileEntity1).setTarget(target.getKey(), target.getValue());
            }
            return true;
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        TileEntity tileEntity = player.world.getTileEntity(pos);
        if (tileEntity != null) {
            RayTraceResult rayTraceResult = ForgeHooks.rayTraceEyes(player, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null) {
                    target = Pair.of(pos, rayTraceResult.sideHit);
                    MechanicalArms.logger.info("Target pos set to " + target);
                }
            }
            return true;
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!isSelected) {
            source = null;
            target = null;
        }
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}
