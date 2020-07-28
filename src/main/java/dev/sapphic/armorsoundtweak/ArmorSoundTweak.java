package dev.sapphic.armorsoundtweak;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class ArmorSoundTweak implements ClientModInitializer {
  private final Supplier<Boolean> armor;
  private final Supplier<Boolean> elytra;
  private final Supplier<Boolean> skulls;
  private final Supplier<Boolean> pumpkins;
  private final Supplier<Boolean> anything;

  private List<Item> oldEquipment = new ArrayList<>(0);

  public ArmorSoundTweak() {
    final Path file = FabricLoader.getInstance().getConfigDir().resolve("armorsoundtweak.toml");
    final FileConfig config = FileConfig.builder(file).autoreload().build();
    final ConfigSpec spec = new ConfigSpec();
    spec.define("sounds.armor", true);
    spec.define("sounds.elytra", true);
    spec.define("sounds.skulls", false);
    spec.define("sounds.pumpkins", false);
    spec.define("sounds.anything", false);
    try {
      // No removal as config persists throughout entire runtime
      FileWatcher.defaultInstance().addWatch(file, () -> {
        // Correct file to specification on change
        spec.correct(config);
        config.save();
      });
    } catch (final IOException e) {
      throw new IllegalStateException("Unable to correct config", e);
    }
    this.armor = () -> config.getOrElse("sounds.armor", true);
    this.elytra = () -> config.getOrElse("sounds.elytra", true);
    this.skulls = () -> config.getOrElse("sounds.skulls", false);
    this.pumpkins = () -> config.getOrElse("sounds.pumpkins", false);
    this.anything = () -> config.getOrElse("sounds.anything", false);
    config.load();
    // Initial file correction/generation
    // TODO Use default config file
    spec.correct(config);
    config.save();
  }

  private static boolean isSkull(final Item item) {
    return (item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock);
  }

  @Override
  public void onInitializeClient() {
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      if ((client.player != null) && (client.player.world != null) && client.player.world.isClient) {
        final List<Item> equipment = new ArrayList<>(4);
        for (final ItemStack stack : client.player.getArmorItems()) {
          equipment.add(stack.getItem());
        }
        if (client.currentScreen instanceof ScreenHandlerProvider<?>) {
          final Iterator<Item> newEquipment = equipment.iterator();
          final Iterator<Item> oldEquipment = this.oldEquipment.iterator();
          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final Item newItem = newEquipment.next();
            final Item oldItem = oldEquipment.next();
            if (newItem != oldItem) {
              final Item item = (newItem == Items.AIR) ? oldItem : newItem;
              SoundEvent sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
              if (this.armor.get() && (item instanceof ArmorItem)) {
                sound = ((ArmorItem) item).getMaterial().getEquipSound();
              } else if (this.elytra.get() && (item instanceof ElytraItem)) {
                sound = SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
              } else if (!this.pumpkins.get() || (item != Blocks.CARVED_PUMPKIN.asItem())) {
                if ((!this.skulls.get() || !isSkull(item)) && !this.anything.get()) {
                  continue;
                }
              }
              client.player.playSound(sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            }
          }
        }
        this.oldEquipment = equipment;
      }
    });
  }
}
