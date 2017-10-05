package net.insomniakitten.ast;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
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
    public static final String MC_VERSIONS = "[1.10,1.13)";

    private static List<ItemStack> lastEquipment = new ArrayList<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (event.phase.equals(TickEvent.Phase.END) && mc.player != null && mc.currentScreen != null) {

            List<ItemStack> equipmentCache = new ArrayList<>();
            mc.player.getArmorInventoryList().forEach(equipmentCache::add);

            int oldSize = lastEquipment.size();
            int newSize = equipmentCache.size();

            if (!lastEquipment.isEmpty() && !equipmentCache.isEmpty()) {
                for (int i = 0; i < (oldSize > newSize ? oldSize : newSize); ++i) {
                    if (lastEquipment.get(i) != equipmentCache.get(i)) {
                        ItemStack lastStack = lastEquipment.get(i);
                        ItemStack cacheStack = equipmentCache.get(i);
                        ItemStack stack = checkStackPresence(lastStack) ? lastStack : cacheStack;
                        if (checkStackPresence(equipmentCache.get(i)) && isArmor(stack) && mc.player.world.isRemote) {
                            ItemArmor.ArmorMaterial material = ((ItemArmor) stack.getItem()).getArmorMaterial();
                            mc.player.world.playSound(mc.player, new BlockPos(mc.player),
                                    material.getSoundEvent(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                        }
                    }
                }
            }

            lastEquipment = equipmentCache;
        }
    }

    private static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ItemArmor;
    }

    private static boolean checkStackPresence(ItemStack stack) {
        return hasNonNullStacks() ? !NonNullStackHelper.isEmpty(stack) : stack != null;
    }

    private static boolean hasNonNullStacks() {
        String ver = MinecraftForge.MC_VERSION;
        //noinspection ConstantConditions
        return ver.startsWith("1.11") || ver.startsWith("1.12");
    }

}
