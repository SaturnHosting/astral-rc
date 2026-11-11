package dev.astral.rc.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.astral.rc.modules.RCModule;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

public class RCCommand extends Command {
    public RCCommand() {
        super("rc", "RC chat commands");
    }

    public static boolean rcChatEnabled = true;

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("toggle").executes(context -> {
            RCModule module = Modules.get().get(RCModule.class);

            if (module == null) {
                error("RC module not found, what the fuck?");
                return SINGLE_SUCCESS;
            }

            rcChatEnabled = !rcChatEnabled;

            if (rcChatEnabled) {
                info("§aMessages are sent in RC Chat.§r");
            } else {
                info("§cMessages are sent in Minecraft Chat.§r");
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("channel")
                .then(literal("join")
                    .then(argument("channel", StringArgumentType.string())
                        .executes(context -> {
                            RCModule module = Modules.get().get(RCModule.class);
                            String channel = StringArgumentType.getString(context, "channel");

                            try {
                                module.socketClient.sendChannelChange(channel);
                            } catch (Exception e) {
                                error("Failed to change channel: " + e.getMessage());
                            }

                            return SINGLE_SUCCESS;
                        })
                    )
                )
            .then(literal("info")
                .executes(context -> {
                    RCModule module = Modules.get().get(RCModule.class);

                    try {
                        module.socketClient.sendInfo();
                    } catch (Exception e) {
                        error("Failed to send info request: " + e.getMessage());
                    }

                    return SINGLE_SUCCESS;
                }))
        );

        builder.executes(context -> {
            RCModule module = Modules.get().get(RCModule.class);

            if (module == null) {
                error("RC module not found, what the fuck?");
                return SINGLE_SUCCESS;
            }

            module.toggle();

            if (module.isActive()) {
                info("§aenabled§r");
            } else {
                info("§cdisabled§r");
            }

            return SINGLE_SUCCESS;
        });
    }
}
