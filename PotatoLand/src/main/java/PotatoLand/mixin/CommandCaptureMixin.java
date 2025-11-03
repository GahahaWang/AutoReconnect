package PotatoLand.mixin;

import PotatoLand.PotatoLand;
import PotatoLand.config.PotatoLandConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPacketListener.class)
public class CommandCaptureMixin {

    static {
        PotatoLand.sendCommandToServer = CommandCaptureMixin::sendCommandToServer;
    }
    @Unique
    private static final Pattern SURVIVAL_PATTERN = Pattern.compile("^survival (1|2|3|vip)$", Pattern.CASE_INSENSITIVE);
    @Unique
    private static boolean sendByPlayer = true;

    @Inject(method = "sendCommand", at = @At("HEAD"))
    private void captureSurvivalCommand(String command, CallbackInfo ci) {
        Matcher matcher = SURVIVAL_PATTERN.matcher(command);
        if(matcher.matches() && sendByPlayer) {
            PotatoLand.survivalTransferByPlayerCommand = true;
            PotatoLand.logger.info("成功擷取命令: {}", command);
            PotatoLandConfig.Config.last_survival_command = command;
        }
        sendByPlayer = true;
    }

    @Unique
    private static void sendCommandToServer(String command) {
        Minecraft mc = Minecraft.getInstance();
        try {
            PotatoLand.logger.info("發送命令到伺服器: {}", command);
            sendByPlayer = false;
            mc.player.connection.sendCommand(command);
        } catch (Exception e) {
            PotatoLand.logger.error("發送命令到伺服器失敗: ", e);
        }
    }

}