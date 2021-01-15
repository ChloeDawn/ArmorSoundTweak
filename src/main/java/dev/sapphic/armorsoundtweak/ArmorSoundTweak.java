package dev.sapphic.armorsoundtweak;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(ArmorSoundTweak.MOD_ID)
public final class ArmorSoundTweak {
  static final String MOD_ID = "armorsoundtweak";

  public ArmorSoundTweak() {
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> EquipmentTicker::register);
    ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> {
      return Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, v) -> true);
    });
  }
}
