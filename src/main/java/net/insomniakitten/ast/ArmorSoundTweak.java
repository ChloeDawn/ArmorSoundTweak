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

@Mod(modid = ArmorSoundTweak.ID,
     name = ArmorSoundTweak.NAME,
     version = ArmorSoundTweak.VERSION,
     acceptedMinecraftVersions = ArmorSoundTweak.MC_VERSIONS,
     clientSideOnly = true)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ArmorSoundTweak {

    public static final String ID = "armorsoundtweak";
    public static final String NAME = "Armor Sound Tweak";
    public static final String VERSION = "%VERSION%";
    public static final String MC_VERSIONS = "[1.10,1.13)";

    private static List<ItemStack> lastEquipment = new ArrayList<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            Minecraft mc = Minecraft.getMinecraft();
            List<ItemStack> equipment = Lists.newArrayList(mc.player.getArmorInventoryList());

            if (mc.player != null && mc.currentScreen != null) {
                Iterator<ItemStack> newStacks = equipment.iterator();
                Iterator<ItemStack> lastStacks = lastEquipment.iterator();

                while (lastStacks.hasNext() && newStacks.hasNext()) {
                    ItemStack newStack = newStacks.next();
                    ItemStack lastStack = lastStacks.next();

                    if (lastStack != newStack) {
                        ItemStack armorStack;

                        if (newStack != null && newStack.getItem() instanceof ItemArmor) {
                            armorStack = newStack;
                        } else if (lastStack != null && lastStack.getItem() instanceof ItemArmor) {
                            armorStack = lastStack;
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
            }

            lastEquipment.clear();
            lastEquipment.addAll(equipment);
        }
    }

}
