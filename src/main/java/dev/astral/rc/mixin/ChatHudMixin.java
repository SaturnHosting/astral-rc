package dev.astral.rc.mixin;

import dev.astral.rc.modules.RCModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Inject(method = "addMessage", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(
        Component contents,
        MessageSignature signature,
        GuiMessageSource source,
        GuiMessageTag tag,
        CallbackInfo ci
    ) {
        RCModule rcModule = Modules.get().get(RCModule.class);

        if (rcModule != null && rcModule.isActive() && rcModule.normalMessages.get()) {
            if (contents.getString().contains("»")) {
                ci.cancel();
            }
        }
    }
}
