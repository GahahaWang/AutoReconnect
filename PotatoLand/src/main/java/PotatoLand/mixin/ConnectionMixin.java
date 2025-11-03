package PotatoLand.mixin;

import PotatoLand.PotatoLand;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(method = "disconnect*", at = @At("HEAD"))
    private void resetIsJoinPotatoLandByAutoReconnect(Component component, CallbackInfo ci) {
        PotatoLand.isJoinPotatoLandByAutoReconnect = false;
    }
}
