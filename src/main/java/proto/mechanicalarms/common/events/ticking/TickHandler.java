package proto.mechanicalarms.common.events.ticking;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import proto.mechanicalarms.common.logic.belt.BeltGroup;
import proto.mechanicalarms.common.logic.belt.BeltNet;
import proto.mechanicalarms.common.tile.BeltHoldingEntity;
import proto.mechanicalarms.common.tile.TileBeltBasic;

import static proto.mechanicalarms.common.logic.belt.BeltNet.beltNets;

public class TickHandler {
    public static TickHandler INSTANCE = new TickHandler();

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {
            return;
        }
        if (beltNets.get(ev.world) == null) {
            return;
        }
        BeltNet beltNet = beltNets.get(ev.world);
        if (!beltNet.toRemove.isEmpty()) {
            beltNet.handleRemovals();
        }

        if (!beltNet.toAddBelt.isEmpty() || !beltNet.addFirst.isEmpty()) {
            beltNets.get(ev.world).groupBelts();
        }
        for (BeltGroup group : beltNets.get(ev.world).groups) {
            for (BeltHoldingEntity entity : group.getBelts()) {
                if (entity instanceof TileBeltBasic tbb) {
                    tbb.update();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent ev) {
        if (ev.phase == TickEvent.Phase.START) {
            return;
        }
        if (beltNets.get(Minecraft.getMinecraft().world) == null) {
            return;
        }
        BeltNet beltNet = beltNets.get(Minecraft.getMinecraft().world);
        if (!beltNet.toRemove.isEmpty()) {
            beltNet.handleRemovals();
        }
        if (!beltNet.toAddBelt.isEmpty() || !beltNet.addFirst.isEmpty()) {
            beltNets.get(Minecraft.getMinecraft().world).groupBelts();
        }
        for (BeltGroup group : beltNets.get(Minecraft.getMinecraft().world).groups) {
            for (BeltHoldingEntity entity : group.getBelts()) {
                if (entity instanceof TileBeltBasic tbb) {
                    tbb.update();
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload ev) {
        beltNets.remove(ev.getWorld());
        }
}
