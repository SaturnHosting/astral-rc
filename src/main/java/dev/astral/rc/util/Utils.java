package dev.astral.rc.util;

import dev.astral.rc.modules.RCModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.awt.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Utils {
    public enum MessageType {
        MESSAGE,
        ERROR,
        INFO,
        WARNING
    }

    public static void chatMessage(String playerMessage, MessageType type) {
        if (playerMessage == null || playerMessage.isEmpty()) return;

        String prefix = Modules.get().get(RCModule.class).prefix.get();
        TextColor prefixColor = Modules.get().get(RCModule.class).prefixColor.get().toTextColor();
        TextColor messageColor = Modules.get().get(RCModule.class).messageColor.get().toTextColor();
        TextColor usernameColor = Modules.get().get(RCModule.class).usernameColor.get().toTextColor();

        String usernameFormat = Modules.get().get(RCModule.class).username.get();

        String message = playerMessage;
        String username = "";

        if (type == MessageType.MESSAGE) {
            String[] parts = playerMessage.split(" ", 2);
            username = parts[0];
            message = parts.length > 1 ? parts[1] : "";
        }
        MutableComponent prefixText = Component.literal(prefix + " ")
            .withStyle(style -> style.withColor(prefixColor));

        MutableComponent nameText = Component.literal("");
        if (type == MessageType.MESSAGE) {
            nameText = Component.literal(usernameFormat.replace("%username%", username) + " ")
                .withStyle(style -> style.withColor(usernameColor));
        }

        MutableComponent typeText = Component.literal("");
        if (type == MessageType.ERROR) {
            typeText = Component.literal("Error: ")
                .withStyle(style -> style.withColor(ChatFormatting.RED));
        } else if (type == MessageType.WARNING) {
            typeText = Component.literal("Warning: ")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW));
        }

        MutableComponent messageText = Component.literal(message);

        switch(type) {
            case MESSAGE:
                messageText.withStyle(style -> style.withColor(messageColor));
                break;
            case ERROR:
                messageText.withStyle(style -> style.withColor(ChatFormatting.RED));
                break;
            case WARNING:
                messageText.withStyle(style -> style.withColor(ChatFormatting.YELLOW));
                break;
            case INFO:
                messageText.withStyle(style -> style.withColor(ChatFormatting.GRAY));
                break;
        }

        MutableComponent finalMessage = prefixText.append(type == MessageType.MESSAGE ? nameText : typeText)
            .append(messageText);

        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendSystemMessage(finalMessage);
            }
        });
    }

    public static void chatMessage(Component message) {
        if (message == null) return;

        MutableComponent finalMessage = Component.literal(Modules.get().get(RCModule.class).prefix.get() + " ")
            .withStyle(style -> style.withColor(
                Modules.get().get(RCModule.class).prefixColor.get().toTextColor()
            ));

        finalMessage.append(message);

        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendSystemMessage(finalMessage);
            }
        });
    }
}
