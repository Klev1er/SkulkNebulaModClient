package net.skulknebula.snebula.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;

public class ModSounds {

    // Регистрируем звуки с правильными идентификаторами
    public static final SoundEvent SERVER_SOUND_MAIN = registerSoundEvent("ambient.server_sound_main");
    public static final SoundEvent SERVER_SOUND_HARDWORK = registerSoundEvent("ambient.server_sound_hardwork");
    public static final SoundEvent SERVER_DISK_WORKING = registerSoundEvent("ambient.server_disk_working");

    public static final SoundEvent SERVER_SOUND_BROKEN = registerSoundEvent("ambient.server_sound_broken");
    public static final SoundEvent SERVER_SOUND_BROKEN_2 = registerSoundEvent("ambient.server_sound_broken_2");


    // Метод для регистрации SoundEvent
    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(SkulkNebulaMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        SkulkNebulaMod.LOGGER.info("Registering Mod Sounds for " + SkulkNebulaMod.MOD_ID);

        // Просто регистрируем - всё уже сделано в статических полях
    }
}