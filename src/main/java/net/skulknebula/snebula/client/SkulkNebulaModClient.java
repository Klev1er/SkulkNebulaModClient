package net.skulknebula.snebula.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.ModBlockEntities;
import net.skulknebula.snebula.block.custom.ComputerBlockEntity;
import net.skulknebula.snebula.block.custom.ComputerBlockRenderer;
import net.skulknebula.snebula.block.custom.MicroscopeBlockRenderer;
import net.skulknebula.snebula.block.custom.ServerBlockRenderer;
import net.skulknebula.snebula.block.custom.screen.ComputerScreen;
import net.skulknebula.snebula.client.update.UpdateNotifier;
import net.skulknebula.snebula.item.ModItems;
import net.skulknebula.snebula.network.ClientNetworking;
import net.skulknebula.snebula.network.ModNetworking;
import net.skulknebula.snebula.screen.ModScreenHandlers;

import java.util.logging.Logger;

@Environment(EnvType.CLIENT)
public class SkulkNebulaModClient implements ClientModInitializer {
    public static final Logger LOGGER_CLIENT = Logger.getLogger(SkulkNebulaMod.MOD_ID + "_client");

    @Override
    public void onInitializeClient() {
        LOGGER_CLIENT.info("SkulkNebulaMod Client side initializing...");

        UpdateNotifier.init();
        ClientNetworking.register();

        //ModNetworking.register();

        //HandledScreens.register(ModScreenHandlers.COMPUTER_SCREEN_HANDLER, ComputerScreen::new);

        BlockEntityRendererFactories.register(
                ModBlockEntities.SERVER_BLOCK_ENTITY,
                (context) -> new ServerBlockRenderer(ModBlockEntities.SERVER_BLOCK_ENTITY)
        );

        BlockEntityRendererFactories.register(
                ModBlockEntities.COMPUTER_BLOCK_ENTITY,
                (context) -> new ComputerBlockRenderer(ModBlockEntities.COMPUTER_BLOCK_ENTITY)
        );
        BlockEntityRendererFactories.register(
                ModBlockEntities.MICROSCOPE_BLOCK_ENTITY,
                (context) -> new MicroscopeBlockRenderer(ModBlockEntities.MICROSCOPE_BLOCK_ENTITY)
        );
    }
}