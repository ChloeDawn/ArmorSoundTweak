package net.insomniakitten.ast;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public static final String MC_VERSIONS = "[1.11,1.13)";

    private static final ImmutableList<EntityEquipmentSlot> ARMOR_SLOTS = ImmutableList.of(
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    );

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null && event.getEntity() instanceof EntityPlayer) {
            if (ARMOR_SLOTS.contains(event.getSlot())
                    && (event.getFrom().getItem() instanceof ItemArmor
                    || event.getTo().getItem() instanceof ItemArmor)) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                ItemStack armor = event.getFrom().isEmpty() ? event.getTo() : event.getFrom();
                if (!armor.isEmpty()) {
                    ItemArmor.ArmorMaterial material = ((ItemArmor) armor.getItem()).getArmorMaterial();
                    if (!player.world.isRemote) {
                        player.world.playSound(null, player.getPosition(), material.getSoundEvent(),
                                SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }
        }
    }

}
