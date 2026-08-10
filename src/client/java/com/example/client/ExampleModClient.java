package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static boolean aimAssist = false;
    public static boolean triggerbot = false;
    public static boolean hitbox = false;
    public static boolean esp = false;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.ronerclient.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.ronerclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new CheatMenuScreen());
                }
            }
        });
    }

    public static class CheatMenuScreen extends Screen {
        public CheatMenuScreen() {
            super(Text.of("RonerClient Menu"));
        }

        @Override
        protected void init() {
            int x = this.width / 2 - 100;
            int y = this.height / 2 - 60;

            this.addDrawableChild(ButtonWidget.builder(
                Text.of("Aim Assist: " + (aimAssist ? "ACIQ" : "QAPALI")),
                btn -> {
                    aimAssist = !aimAssist;
                    btn.setMessage(Text.of("Aim Assist: " + (aimAssist ? "ACIQ" : "QAPALI")));
                }
            ).dimensions(x, y, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                Text.of("Triggerbot: " + (triggerbot ? "ACIQ" : "QAPALI")),
                btn -> {
                    triggerbot = !triggerbot;
                    btn.setMessage(Text.of("Triggerbot: " + (triggerbot ? "ACIQ" : "QAPALI")));
                }
            ).dimensions(x, y + 25, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                Text.of("Hitbox: " + (hitbox ? "ACIQ" : "QAPALI")),
                btn -> {
                    hitbox = !hitbox;
                    btn.setMessage(Text.of("Hitbox: " + (hitbox ? "ACIQ" : "QAPALI")));
                }
            ).dimensions(x, y + 50, 200, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                Text.of("ESP: " + (esp ? "ACIQ" : "QAPALI")),
                btn -> {
                    esp = !esp;
                    btn.setMessage(Text.of("ESP: " + (esp ? "ACIQ" : "QAPALI")));
                }
            ).dimensions(x, y + 75, 200, 20).build());
        }
    }
}
        
