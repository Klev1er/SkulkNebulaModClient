package net.skulknebula.snebula.signal;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class DecryptionTicker {
    private static int dayCounter = 0;
    private static final int TICKS_PER_DAY = 6000; // 5 минут = 1/4 игровой день

    public static final DecryptionManager DECRYPTION_MANAGER = DecryptionManager.getInstance();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(DecryptionTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        dayCounter++;

        // Каждый игровой день (24000 тиков)
        if (dayCounter >= TICKS_PER_DAY) {
            dayCounter = 0;

            // Вызываем тик расшифровки для всех игроков
            DECRYPTION_MANAGER.tick();
        }
    }

    public DecryptionManager getDecryptionManager() {
        return DECRYPTION_MANAGER;
    }

}