package net.insomniakitten.ast;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    private static List<ItemStack> lastEquipment = Lists.newArrayList();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (event.phase == TickEvent.Phase.START && mc.player != null) {
            List<ItemStack> equipment = Lists.newArrayList();

            mc.player.getArmorInventoryList().forEach(
                    stack -> equipment.add(stack.copy())
            );

            if (mc.currentScreen != null) {
                Iterator<ItemStack> newStacks = equipment.iterator();
                Iterator<ItemStack> lastStacks = lastEquipment.iterator();

                while (lastStacks.hasNext() && newStacks.hasNext()) {
                    ItemStack newStack = newStacks.next();
                    ItemStack lastStack = lastStacks.next();

                    if (!ItemStack.areItemsEqualIgnoreDurability(newStack, lastStack)) {
                        if (isValidEquipment(newStack)) {
                            playEquipSound(newStack, mc.player);
                        } else if (isValidEquipment(lastStack)) {
                            playEquipSound(lastStack, mc.player);
                        }
                    }
                }
            }

            lastEquipment = equipment;
        }
    }

    private static boolean isValidEquipment(ItemStack stack) {
        return stack != null
                && (stack.getItem() instanceof ItemArmor
                || stack.getItem() instanceof ItemElytra);
    }

    private static void playEquipSound(ItemStack stack, EntityPlayer player) {
        Item item = stack.getItem();
        SoundEvent sound = null;

        if (item instanceof ItemArmor) {
            sound = ((ItemArmor) item).getArmorMaterial().getSoundEvent();
        } else if (item instanceof ItemElytra) {
            sound = SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA;
        }

        if (sound != null) {
            player.world.playSound(player, new BlockPos(player),
                    sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

}
