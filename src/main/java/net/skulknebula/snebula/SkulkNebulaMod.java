package net.skulknebula.snebula;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.skulknebula.snebula.block.ModBlockEntities;
import net.skulknebula.snebula.block.ModBlocks;
import net.skulknebula.snebula.block.custom.screen.ComputerScreen;
import net.skulknebula.snebula.command.SignalCommand;
import net.skulknebula.snebula.component.ModDataComponentTypes;
import net.skulknebula.snebula.item.ModItemGroups;
import net.skulknebula.snebula.item.ModItems;
import net.skulknebula.snebula.network.BrewingParticlePayload;
import net.skulknebula.snebula.network.ModNetworking;
import net.skulknebula.snebula.screen.ModScreenHandlers;
import net.skulknebula.snebula.signal.DecryptionManager;
import net.skulknebula.snebula.signal.DecryptionTicker;
import net.skulknebula.snebula.signal.SignalLoader;
import net.skulknebula.snebula.sound.ModSounds;

import java.util.logging.Logger;

public class SkulkNebulaMod implements ModInitializer {
    public static final String MOD_ID = "snebula";
    public static final Logger LOGGER = Logger.getLogger(SkulkNebulaMod.MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SkulkNebulaMod Global side initializing...");

        ModBlocks.registerModBlocks();
        ModItems.register();
        ModSounds.registerSounds();
        ModBlockEntities.register();
        ModItemGroups.registerItemGroups();
        ModScreenHandlers.initialize();
        ModDataComponentTypes.initialize();
        SignalLoader.init();
        DecryptionManager.init();
        DecryptionTicker.init();

        ModNetworking.register();
        HandledScreens.register(ModScreenHandlers.COMPUTER_SCREEN_HANDLER, ComputerScreen::new);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SignalCommand.register(dispatcher, registryAccess, environment);
        });
    }
}