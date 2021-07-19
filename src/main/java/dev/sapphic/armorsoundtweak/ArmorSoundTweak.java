package dev.sapphic.armorsoundtweak;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

@Mod(ArmorSoundTweak.MOD_ID)
public final class ArmorSoundTweak {
  static final String MOD_ID = "armorsoundtweak";

  private static final Supplier<EquipmentConfig> CONFIG =
    DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> EquipmentConfig::lazy);

  public ArmorSoundTweak() {
    ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> {
      return Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, v) -> true);
    });

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ArmorTicker::register);

    if (ModList.get().isLoaded("curios")) {
      DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CuriosTicker::register);

      if (!FMLLoader.isProduction()) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CuriosTicker::sendIdeImc);
      }
    }
  }

  public static EquipmentConfig config() {
    return CONFIG.get();
  }
}
