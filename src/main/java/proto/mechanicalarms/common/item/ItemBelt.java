package proto.mechanicalarms.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import proto.mechanicalarms.MechanicalArms;
import proto.mechanicalarms.client.renderer.InstanceRender;
import proto.mechanicalarms.common.block.Blocks;
import proto.mechanicalarms.common.block.properties.Directions;
import proto.mechanicalarms.common.tile.TileBeltBasic;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBelt extends ItemBlock {

    static IBlockState bs;
    static BlockPos pos;
    static IBlockAccess blockAccess;


    public ItemBelt(Block block) {
        super(block);
        setRegistryName(MechanicalArms.MODID, "belt_basic");
        setTileEntityItemStackRenderer(new BeltItemRender());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (worldIn.isRemote) {
            if (entityIn instanceof EntityPlayer player) {
                if (isSelected) {
                    RayTraceResult rt = this.rayTrace(worldIn, player, false);
                    if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
                        bs = Blocks.BELT_BASE.getStateForPlacement(worldIn, rt.getBlockPos(), rt.sideHit, (float) rt.hitVec.x, (float) rt.hitVec.y, (float) rt.hitVec.z, 0, player);
                        pos = rt.getBlockPos();
                        blockAccess = worldIn;
                    }
                } else {
                    bs = null;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRender(DrawBlockHighlightEvent event) {
        if (bs != null) {
            Entity entity = event.getPlayer();
            TileEntity tileEntity = Blocks.BELT_BASE.createTileEntity(Minecraft.getMinecraft().world, bs);
            //tileEntity.setWorld((World) blockAccess);
            TileEntitySpecialRenderer<TileEntity> terd = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
            BlockPos ghostPos = event.getTarget().getBlockPos().offset(event.getTarget().sideHit);

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)event.getPartialTicks();
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)event.getPartialTicks();
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)event.getPartialTicks();

//            terd.renderTileEntityFast(tileEntity, (double) ghostPos.getX() - x,
//                    (double) ghostPos.getY() - y,
//                    (double) ghostPos.getZ() - z, 1, -1, 0, Tessellator.getInstance().getBuffer());
//            //InstanceRender.draw();
        }
    }

    static class BeltItemRender extends TileEntityItemStackRenderer {
        TileBeltBasic beltBasic = new TileBeltBasic();

        @Override
        public void renderByItem(ItemStack p_192838_1_, float p_192838_2_) {

            beltBasic.setDirection(Directions.getFromHorizontalFacing(Minecraft.getMinecraft().player.getHorizontalFacing()));
            TileEntitySpecialRenderer<TileEntity> terd = TileEntityRendererDispatcher.instance.getRenderer(beltBasic);
            terd.renderTileEntityFast(beltBasic, 0,  0, 0, 1, -1, 0, Tessellator.getInstance().getBuffer());
            InstanceRender.draw();
        }
    }
}
