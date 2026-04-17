package net.skulknebula.snebula.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.screen.ComputerScreenHandler;

public class ModScreenHandlers {
    public static final ScreenHandlerType<ComputerScreenHandler> COMPUTER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(SkulkNebulaMod.MOD_ID, "computer"),
            new ScreenHandlerType<>(ComputerScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    public static void initialize() {}
}