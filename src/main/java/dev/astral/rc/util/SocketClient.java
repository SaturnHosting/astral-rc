package dev.astral.rc.util;

import dev.astral.rc.modules.RCModule;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class SocketClient {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;
    private volatile boolean running = true;

    public SocketClient(String host, int port, String displayName, String token) throws IOException {
        socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(host, port), 5000); // 5 second connection timeout
        socket.setSoTimeout(30000);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        sendRaw(String.format("AUTH %s %s", displayName, token));

        String authResponse = in.readLine();
        if (authResponse == null || !authResponse.startsWith("AUTH_SUCCESS")) {
            String errorMsg = authResponse != null && authResponse.startsWith("ERROR ")
                ? authResponse.substring(6)
                : "Unknown error";
            throw new IOException("Authentication failed: " + errorMsg);
        }

        String username = authResponse.split(" ", 2)[1];

        System.out.println("Authentication successful for user: " + username);
    }

    public void sendMessage(String message) throws IOException {
        sendRaw("MSG " + message);
    }

    public void sendInfo() throws IOException {
        sendRaw("INFO");
    }

    public void sendChannelChange(String channel) throws IOException {
        sendRaw("JOIN " + channel);
    }

    private void sendRaw(String msg) throws IOException {
        synchronized (out) {
            out.write(msg);
            out.newLine();
            out.flush();
        }
    }

    public void start() {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    System.out.println("Received: " + line);

                    if (line.startsWith("INFO ")) {
                        String[] information = line.substring("INFO ".length()).split(" ", 2);
                        String channel = information[0];
                        String playersList = information[1];
                        String[] players = playersList.split(",");
                        StringBuilder formattedPlayerList = new StringBuilder();

                        for (int i = 0; i < players.length; i++) {
                            if (i > 0) formattedPlayerList.append(", ");
                            formattedPlayerList.append(players[i]);
                        }

                        String result = formattedPlayerList.toString();

                        MutableComponent playerMessage = Component.literal("Current Channel: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(channel)
                                .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("\nOnline Users in Channel: ")
                                .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(result)
                                .withStyle(ChatFormatting.WHITE));

                        Utils.chatMessage(playerMessage);
                    }
                    else if (line.startsWith("MSG ")) {
                        String messageContent = line.substring("MSG ".length());
                        Utils.chatMessage(messageContent, Utils.MessageType.MESSAGE);
                    }
                    else if (line.startsWith("ERROR ")) {
                        String errorContent = line.substring("ERROR ".length());
                        Utils.chatMessage(errorContent, Utils.MessageType.ERROR);
                    }
                    else if (line.startsWith("JOIN") && Modules.get().get(RCModule.class).connectionMsgs.get()) {
                        String joinedUser = line.substring("JOIN ".length());
                        Utils.chatMessage(joinedUser + " connected", Utils.MessageType.INFO);
                    } else if (line.startsWith("LEAVE") && Modules.get().get(RCModule.class).connectionMsgs.get()) {
                        String leftUser = line.substring("LEAVE ".length());
                        Utils.chatMessage(leftUser + " disconnected", Utils.MessageType.INFO);
                    } else if (line.startsWith("CHANNEL_CHANGED")) {
                        String newChannel = line.substring("CHANNEL_CHANGED ".length());
                        MutableComponent message = Component.literal("You are now connected to channel ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(newChannel)
                                .withStyle(ChatFormatting.WHITE));

                        Utils.chatMessage(message);
                    }
                    else if (line.equals("KEEPALIVE")) {
                        sendRaw("ALIVE");
                    }
                }
            } catch (SocketTimeoutException e) {
                if (running) {
                    System.out.println("Socket read timeout: " + e.getMessage());
                    Utils.chatMessage("Connection timeout to RC server", Utils.MessageType.ERROR);
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Disconnected from server: " + e.getMessage());
                    Utils.chatMessage("Disconnected from server: " + e.getMessage(), Utils.MessageType.ERROR);
                }
            } finally {
                close();
            }
        });

        listener.setName("RC-Listener-Thread");
        listener.setDaemon(true);
        listener.start();
    }

    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
