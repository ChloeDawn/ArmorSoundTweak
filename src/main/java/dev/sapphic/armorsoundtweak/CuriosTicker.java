package dev.sapphic.armorsoundtweak;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;

final class CuriosTicker extends EquipmentTicker {
  static void register() {
    MinecraftForge.EVENT_BUS.addListener(new CuriosTicker());
  }

  private static IItemHandler getEquippedCurios(final Player player) {
    return CuriosApi.getCuriosHelper().getEquippedCurios(player)
      .orElse((IItemHandlerModifiable) EmptyHandler.INSTANCE);
  }

  @Override
  protected Iterable<ItemStack> getEquipment(final Player player) {
    final var handler = getEquippedCurios(player);
    final var curios = new ArrayList<ItemStack>(handler.getSlots());

    for (var slot = 0; slot < handler.getSlots(); ++slot) {
      curios.add(handler.getStackInSlot(slot).copy());
    }

    return curios;
  }

  @Override
  protected void playEquipSound(final Player player, final Item item) {
    if (!ArmorSoundTweak.config().allowsCurios()) {
      return;
    }

    CuriosApi.getCuriosHelper().getCurio(new ItemStack(item))
      .map(curio -> curio.getEquipSound(null)).ifPresentOrElse(sound -> {
        player.playNotifySound(sound.soundEvent(), SoundSource.NEUTRAL, sound.volume(), sound.pitch());
      }, () -> {
        player.playNotifySound(SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.NEUTRAL, 1.0F, 1.0F);
      });
  }
}
