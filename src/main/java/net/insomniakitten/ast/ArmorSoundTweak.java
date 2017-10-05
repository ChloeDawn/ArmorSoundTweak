package net.insomniakitten.ast;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
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
    public static final String MC_VERSIONS = "[1.10,1.13)";

    private static List<ItemStack> lastEquipment = new ArrayList<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (event.phase.equals(TickEvent.Phase.END) && mc.player != null && mc.currentScreen != null) {
            List<ItemStack> equipmentCache = Lists.newArrayList(mc.player.getArmorInventoryList());
            Iterator<ItemStack> lastStacks = lastEquipment.iterator();
            Iterator<ItemStack> cacheStacks = equipmentCache.iterator();

            while (lastStacks.hasNext() && cacheStacks.hasNext()) {
                ItemStack lastStack = lastStacks.next();
                ItemStack cacheStack = cacheStacks.next();

                if (lastStack != cacheStack) {
                    ItemStack armorStack;

                    if (lastStack != null && lastStack.getItem() instanceof ItemArmor) {
                        armorStack = lastStack;
                    } else if (cacheStack != null && cacheStack.getItem() instanceof ItemArmor) {
                        armorStack = cacheStack;
                    } else {
                        armorStack = null;
                    }

                    if (armorStack != null) {
                        ItemArmor armor = ((ItemArmor) armorStack.getItem());

                        mc.player.world.playSound(mc.player, new BlockPos(mc.player),
                                armor.getArmorMaterial().getSoundEvent(),
                                SoundCategory.PLAYERS, 1.0f, 1.0f);
                        break;
                    }
                }
            }

            lastEquipment = equipmentCache;
        }
    }

}
