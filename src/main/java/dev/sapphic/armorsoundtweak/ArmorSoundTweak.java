package dev.sapphic.armorsoundtweak;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;

import java.util.function.Supplier;

@Mod(ArmorSoundTweak.MOD_ID)
public final class ArmorSoundTweak {
  static final String MOD_ID = "armorsoundtweak";

  private static final Supplier<EquipmentConfig> CONFIG =
    DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> EquipmentConfig::lazy);

  public ArmorSoundTweak() {
    ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> {
      return new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, v) -> true);
    });

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ArmorTicker::register);

    if (ModList.get().isLoaded("curios")) {
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CuriosTicker::register);
    }
  }

  public static EquipmentConfig config() {
    return CONFIG.get();
  }
}
