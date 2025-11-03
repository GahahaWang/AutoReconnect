package PotatoLand.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;

public final class PotatoLandConfig {
    private static final String FILE_NAME = "autoreconnectpotatoland.json";
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).setPrettyPrinting().create();
    private static Config config = new Config();
    private PotatoLandConfig() { }

    public record Config() {
        public static boolean enabled = true;
        public static String last_survival_command = "";
    }

    public static void load() {
        var targetFile = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!targetFile.toFile().exists()) {
            LogUtils.getLogger().info("AutoReconnectPotatoLand config does not yet exist, generating new file");
            save();
            return;
        }

        try {
            config = GSON.fromJson(
                    Files.readString(targetFile),
                    Config.class);
        } catch (IOException | JsonSyntaxException ex) {
            LogUtils.getLogger().warn("AutoReconnectPotatoLand could not load the config", ex);
            config = new Config();
            save();
        }
    }


    public static void save() {
        try {
            Files.writeString(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME), GSON.toJson(config));
        } catch (IOException ex) {
            LogUtils.getLogger().error("AutoReconnect could not save the config", ex);
        }
    }
}
