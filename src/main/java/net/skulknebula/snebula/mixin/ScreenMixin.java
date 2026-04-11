package net.skulknebula.snebula.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.input.KeyInput;
import net.skulknebula.snebula.client.update.ModUpdaterScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        Screen self = (Screen) (Object) this;

        // Срабатывает только для TitleScreen
        if (self instanceof TitleScreen) {
            if (keyInput.getKeycode() == GLFW.GLFW_KEY_F1) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.currentScreen == self) {
                    client.setScreen(new ModUpdaterScreen(self));
                    cir.setReturnValue(true);
                }
            }
        }
    }
}