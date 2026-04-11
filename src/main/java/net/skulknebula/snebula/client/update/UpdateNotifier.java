package net.skulknebula.snebula.client.update;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class UpdateNotifier {
    private static boolean checked = false;
    private static boolean hasUpdate = false;
    private static String latestVersion = "";
    private static long lastCheckTime = 0;
    private static final long CHECK_COOLDOWN = 60000; // 1 минута
    private static boolean notificationShown = false;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null && client.currentScreen instanceof TitleScreen) {
                long now = System.currentTimeMillis();

                // Проверяем обновления раз в минуту
                if (!checked && (now - lastCheckTime > CHECK_COOLDOWN)) {
                    checked = true;
                    lastCheckTime = now;

                    ModUpdater.checkForUpdates().thenAccept(result -> {
                        if (result != null && result.hasUpdate()) {
                            hasUpdate = true;
                            latestVersion = result.latestVersion();
                            notificationShown = false;
                        }
                    });
                }

                // Показываем уведомление
                if (hasUpdate && !notificationShown) {
                    showUpdateNotification(client);
                }
            }
        });
    }

    public static void showUpdateNotification(MinecraftClient client) {
        if (hasUpdate && !notificationShown) {
            notificationShown = true;

            // Звук уведомления
            client.getSoundManager().play(PositionedSoundInstance.ui(
                    SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F
            ));

            // Показываем тост
            client.getToastManager().add(new UpdateToast(latestVersion));
        }
    }
}