package dev.sapphic.armorsoundtweak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.event.TickEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

abstract class EquipmentTicker implements Consumer<TickEvent.ClientTickEvent> {
  private List<Item> oldEquipment = Collections.emptyList();

  @Override
  public void accept(final TickEvent.ClientTickEvent event) {
    final Minecraft client = Minecraft.getInstance();

    if ((event.phase == TickEvent.Phase.START) && (client.player != null)) {
      if ((client.player.world != null) && client.player.world.isRemote) {
        final List<Item> equipment = this.getEquipment(client.player);

        if (client.currentScreen instanceof ContainerScreen<?>) {
          final Iterator<Item> newEquipment = equipment.iterator();
          final Iterator<Item> oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final Item newItem = newEquipment.next();
            final Item oldItem = oldEquipment.next();

            if (newItem != oldItem) {
              this.playEquipSound(client.player, (newItem == Items.AIR) ? oldItem : newItem);
            }
          }
        }

        this.oldEquipment = equipment;
      }
    }
  }

  protected abstract List<Item> getEquipment(final ClientPlayerEntity player);

  protected abstract void playEquipSound(final ClientPlayerEntity player, final Item item);
}
