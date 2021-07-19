package dev.sapphic.armorsoundtweak;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.ArrayList;
import java.util.List;

final class CuriosTicker extends EquipmentTicker {
  static void register() {
    MinecraftForge.EVENT_BUS.addListener(new CuriosTicker());
  }

  static void sendIdeImc() {
    InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> {
      return SlotTypePreset.RING.getMessageBuilder().build();
    });
  }

  private static IItemHandler getEquippedCurios(final PlayerEntity player) {
    return CuriosApi.getCuriosHelper().getEquippedCurios(player)
      .orElse((IItemHandlerModifiable) EmptyHandler.INSTANCE);
  }

  @Override
  protected List<Item> getEquipment(final PlayerEntity player) {
    final IItemHandler handler = getEquippedCurios(player);

    final List<Item> curios = new ArrayList<>(handler.getSlots());

    for (int slot = 0; slot < handler.getSlots(); slot++) {
      curios.add(handler.getStackInSlot(slot).getItem());
    }

    return curios;
  }

  @Override
  protected void playEquipSound(final PlayerEntity player, final Item item) {
    if (!ArmorSoundTweak.config().allowsCurios()) {
      return;
    }

    final ICurio.@Nullable SoundInfo sound = CuriosApi.getCuriosHelper()
      .getCurio(new ItemStack(item))
      .map(o -> o.getEquipSound(new SlotContext("", player)))
      .orElse(null);

    if (sound != null) {
      player.playSound(sound.getSoundEvent(), SoundCategory.NEUTRAL, sound.getVolume(), sound.getPitch());
    } else {
      player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
  }
}
