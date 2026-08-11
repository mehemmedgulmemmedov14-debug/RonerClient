
package com.ronerclient.gui;

import com.ronerclient.RonerClient;
import com.ronerclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RonerGuiScreen extends Screen {

    public RonerGuiScreen() {
        super(Text.literal("RonerClient Menyu"));
    }

    @Override
    protected void init() {
        int yPos = 50; // Düymələrin başlayacağı Y koordinatı

        for (Module module : RonerClient.modules) {
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(module.getName() + " -> " + (module.isEnabled() ? "§a[AÇIK]" : "§c[BAĞLI]")),
                btn -> {
                    module.toggle(); // Modulu yandırır/söndürür
                    btn.setMessage(Text.literal(module.getName() + " -> " + (module.isEnabled() ? "§a[AÇIK]" : "§c[BAĞLI]")));
                }
            )
            .dimensions(this.width / 2 - 100, yPos, 200, 20)
            .build();

            this.addDrawableChild(button);
            yPos += 25; // Hər düymə arasında məsafə
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false; // Menyu açılanda oyunun arxa fonda dayanmamasını (Pause olmamasını) təmin edir
    }
}
package com.ronerclient;

import com.ronerclient.gui.RonerGuiScreen;
import com.ronerclient.module.Module;
import com.ronerclient.module.impl.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class RonerClient implements ClientModInitializer {
    public static final List<Module> modules = new ArrayList<>();
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        // Modulları siyahıya əlavə edirik
        modules.add(new AimAssistModule());
        modules.add(new TriggerbotModule());
        modules.add(new ESPModule());
        modules.add(new FOVModule());

        // 'M' düyməsini Minecraft klaviatura tənzimləmələrinə qeydiyyata alırıq
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "RonerClient Menyusunu Aç",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "RonerClient"
        ));

        // Oyun dövrəsində (tick) düyməni və modulları dinləyirik
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 'M' düyməsinə basılıbsa menyunu açır
            while (openGuiKey.wasPressed()) {
                client.setScreen(new RonerGuiScreen());
            }

            // Aktiv olan modulların xüsusiyyətlərini işlədir
            for (Module module : modules) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        });
    }
}
