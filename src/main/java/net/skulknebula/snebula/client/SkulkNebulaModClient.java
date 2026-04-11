package net.skulknebula.snebula.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.client.update.UpdateNotifier;
import net.skulknebula.snebula.network.ClientNetworking;

import java.util.logging.Logger;

@Environment(EnvType.CLIENT)
public class SkulkNebulaModClient implements ClientModInitializer {
    public static final Logger LOGGER_CLIENT = Logger.getLogger(SkulkNebulaMod.MOD_ID + "_client");

    @Override
    public void onInitializeClient() {
        LOGGER_CLIENT.info("SkulkNebulaMod Client side initializing...");

        UpdateNotifier.init();
        ClientNetworking.register();
    }
}