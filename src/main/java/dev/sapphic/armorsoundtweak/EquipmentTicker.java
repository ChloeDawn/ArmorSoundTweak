package dev.sapphic.armorsoundtweak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

import java.util.Collections;
import java.util.function.Consumer;

abstract class EquipmentTicker implements Consumer<TickEvent.ClientTickEvent> {
  private Iterable<ItemStack> oldEquipment = Collections::emptyIterator;

  @Override
  public void accept(final TickEvent.ClientTickEvent event) {
    final var client = Minecraft.getInstance();

    if ((event.phase == TickEvent.Phase.START) && (client.player != null) && client.player.level.isClientSide) {
      final var equipment = this.getEquipment(client.player);

      if (client.screen instanceof AbstractContainerScreen<?>) {
        final var newEquipment = equipment.iterator();
        final var oldEquipment = this.oldEquipment.iterator();

        while (oldEquipment.hasNext() && newEquipment.hasNext()) {
          final var newItem = newEquipment.next();
          final var oldItem = oldEquipment.next();

          if (!ItemStack.matches(newItem, oldItem)) {
            this.playEquipSound(client.player, (newItem.isEmpty() ? oldItem : newItem).getItem());
          }
        }
      }

      this.oldEquipment = equipment;
    }
  }

  protected abstract Iterable<ItemStack> getEquipment(final Player player);

  protected abstract void playEquipSound(final Player player, final Item item);
}
