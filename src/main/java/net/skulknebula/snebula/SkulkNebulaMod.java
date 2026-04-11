package net.skulknebula.snebula;

import net.fabricmc.api.ModInitializer;
import net.skulknebula.snebula.network.ModNetworking;

public class SkulkNebulaMod implements ModInitializer {
    public static final String MOD_ID = "snebula";


    @Override
    public void onInitialize() {
        System.out.println("SkulkNebulaMod Global side initializing...");

        // Регистрируем сетевые пакеты ТОЛЬКО ОДИН РАЗ
        ModNetworking.register();
    }
}