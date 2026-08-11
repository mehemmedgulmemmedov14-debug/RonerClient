package com.ronerclient.module;

import net.minecraft.client.MinecraftClient;

public abstract class Module {
    private final String name;
    private boolean enabled;
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name) {
        this.name = name;
        this.enabled = false;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void onTick();
}
package com.ronerclient.module.impl;

import com.ronerclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerbotModule extends Module {

    public TriggerbotModule() {
        super("Triggerbot");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.crosshairTarget == null || mc.interactionManager == null) return;

        if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (entity instanceof PlayerEntity && entity.isAlive()) {
                if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                    mc.interactionManager.attackEntity(mc.player, entity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }
}
package com.ronerclient.module.impl;

import com.ronerclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssistModule extends Module {
    private final double range = 4.5;
    private final float speed = 0.15f;

    public AimAssistModule() {
        super("AimAssist");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        PlayerEntity target = null;
        double closestDist = range;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive()) continue;
            double dist = mc.player.distanceTo(player);
            if (dist < closestDist) {
                closestDist = dist;
                target = player;
            }
        }

        if (target != null) {
            Vec3d targetPos = target.getEyePos();
            Vec3d playerPos = mc.player.getEyePos();

            double dx = targetPos.x - playerPos.x;
            double dy = targetPos.y - playerPos.y;
            double dz = targetPos.z - playerPos.z;
            double dh = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float targetPitch = (float) MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(dy, dh)));

            mc.player.setYaw(mc.player.getYaw() + (targetYaw - mc.player.getYaw()) * speed);
            mc.player.setPitch(mc.player.getPitch() + (targetPitch - mc.player.getPitch()) * speed);
        }
    }
}
package com.ronerclient.module.impl;

import com.ronerclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;

public class ESPModule extends Module {

    public ESPModule() {
        super("ESP");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        if (mc.world != null) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player) {
                    player.setGlowing(false);
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isAlive()) {
                player.setGlowing(true);
            }
        }
    }
}
package com.ronerclient.module.impl;

import com.ronerclient.module.Module;

public class FOVModule extends Module {
    private final int customFov = 120;
    private int defaultFov = 110;

    public FOVModule() {
        super("FOV");
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            defaultFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(customFov);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getFov().setValue(defaultFov);
        }
    }

    @Override
    public void onTick() {}
}
package com.ronerclient.gui;

import com.ronerclient.RonerClient;
import com.ronerclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class RonerGuiScreen extends Screen {

    public RonerGuiScreen() {
        super(Text.literal("RonerClient Menu"));
    }

    @Override
    protected void init() {
        int yPos = 50;

        for (Module module : RonerClient.modules) {
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(module.getName() + " -> " + (module.isEnabled() ? "[ON]" : "[OFF]")),
                btn -> {
                    module.toggle();
                    btn.setMessage(Text.literal(module.getName() + " -> " + (module.isEnabled() ? "[ON]" : "[OFF]")));
                }
            )
            .dimensions(this.width / 2 - 100, yPos, 200, 20)
            .build();

            this.addDrawableChild(button);
            yPos += 25;
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
        return false;
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
        modules.add(new AimAssistModule());
        modules.add(new TriggerbotModule());
        modules.add(new ESPModule());
        modules.add(new FOVModule());

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "RonerClient Menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "RonerClient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (openGuiKey.wasPressed()) {
                client.setScreen(new RonerGuiScreen());
            }

            for (Module module : modules) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        });
    }
}
