package dev.astral.rc.mixin;

import dev.astral.rc.commands.RCCommand;
import dev.astral.rc.modules.RCModule;
import dev.astral.rc.util.Utils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ClientPacketListener.class)
public class ChatMixin {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) throws IOException {
        RCModule rcModule = Modules.get().get(RCModule.class);
        if(rcModule.socketClient == null) return;

        if (rcModule == null || !rcModule.isActive()) return;

        if (!rcModule.meteorCommands.get()) return;

        if (!RCCommand.rcChatEnabled) return;

        String meteorPrefix = Config.get().prefix.get();
        List<String> otherPrefixes = rcModule.otherPrefixes.get();

        if (message.startsWith(meteorPrefix)) return;

        for (String prefix : otherPrefixes) {
            if (message.startsWith(prefix)) {
                return;
            }
        }

        assert mc.player != null;
        assert rcModule.socketClient != null;
        try {
            rcModule.socketClient.sendMessage(message);
        } catch (IOException e) {
            Utils.chatMessage("Failed to send message, is the server online?", Utils.MessageType.ERROR);
            e.printStackTrace();
        }
        ci.cancel();
    }
}
