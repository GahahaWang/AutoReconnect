package PotatoLand.mixin;

import PotatoLand.PotatoLand;
import autoreconnect.AutoReconnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntConsumer;

@Mixin(AutoReconnect.class)
public abstract class AutoReconnectMixin {

    @Inject(method = "countdown", at = @At(value = "RETURN", ordinal = 1), remap = false)
    private void isConnectByAutoReconnect(int seconds, IntConsumer callback, CallbackInfo ci) {
        PotatoLand.isJoinPotatoLandByAutoReconnect = true;
    }
    @Inject(method = "reconnect", at = @At("HEAD"), remap = false)
    private void justConnect(CallbackInfo ci) {
        PotatoLand.justConnected = true;
    }
}
