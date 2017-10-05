package net.insomniakitten.ast;

import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = ArmorSoundTweak.MOD_ID,
     name = ArmorSoundTweak.MOD_NAME,
     version = ArmorSoundTweak.MOD_VERSION,
     acceptedMinecraftVersions = ArmorSoundTweak.MC_VERSIONS,
     clientSideOnly = true)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ArmorSoundTweak {

    public static final String MOD_ID = "armorsoundtweak";
    public static final String MOD_NAME = "Armor Sound Tweak";
    public static final String MOD_VERSION = "%VERSION%";
    public static final String MC_VERSIONS = "[1.10,1.11)";

    private static List<ItemStack> lastEquipment = new ArrayList<>();
    private static BlockPos.MutableBlockPos playerPos = new BlockPos.MutableBlockPos();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase.equals(TickEvent.Phase.END) && mc.player != null && mc.currentScreen != null) {
            List<ItemStack> equipmentCache = new ArrayList<>();
            for (ItemStack stack : mc.player.getArmorInventoryList()) {
                if (stack != null) equipmentCache.add(stack);
            }
            if (lastEquipment.size() != equipmentCache.size()) {
                if (mc.player.world.isRemote) {
                    playerPos.setPos(mc.player);
                    mc.player.world.playSound(mc.player, playerPos,
                            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
                            SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
            lastEquipment = equipmentCache;
        }
    }

}
