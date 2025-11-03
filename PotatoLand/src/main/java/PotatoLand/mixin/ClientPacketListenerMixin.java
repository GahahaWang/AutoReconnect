package PotatoLand.mixin;

import PotatoLand.PotatoLand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Unique
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    @Unique
    private static ScheduledFuture<?> future;
    @Inject(at = @At("TAIL"), method = "handleLogin")
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
        ServerData sd = Minecraft.getInstance().getCurrentServer();
        if(sd != null && sd.ip.toLowerCase().endsWith("potatoland.cc")) {
            if(future!=null) future.cancel(true);
            future = EXECUTOR_SERVICE.schedule(PotatoLand::onChangeServersWithinPotatoLand,2, TimeUnit.SECONDS);
        }
    }
}
