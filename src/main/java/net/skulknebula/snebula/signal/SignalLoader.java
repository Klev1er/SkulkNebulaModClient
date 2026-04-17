package net.skulknebula.snebula.signal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.skulknebula.snebula.SkulkNebulaMod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SignalLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, SignalData> SIGNALS = new HashMap<>();
    private static Path signalsFolder;
    private static boolean initialized = false;

    public static void init() {
        signalsFolder = FabricLoader.getInstance().getConfigDir().resolve("snebula/signals");

        try {
            // Создаём папки если их нет
            Files.createDirectories(signalsFolder);
            SkulkNebulaMod.LOGGER.info("Signals folder created at: {" + signalsFolder + "}");

            // Создаём дефолтные сигналы
            createDefaultSignals();

            // Загружаем все сигналы
            loadAllSignals();

            initialized = true;
        } catch (IOException e) {
            SkulkNebulaMod.LOGGER.warning("Failed to initialize signals folder " + e);
        }
    }

    private static void createDefaultSignals() throws IOException {
        // Проверяем, есть ли уже файлы
        if (Files.exists(signalsFolder) && Files.list(signalsFolder).findAny().isPresent()) {
            SkulkNebulaMod.LOGGER.info("Signals folder already contains files, skipping default creation");
            return;
        }

        SkulkNebulaMod.LOGGER.info("Creating default signals...");

        // Создаём дефолтные сигналы
        Map<String, SignalData> defaultSignals = new LinkedHashMap<>();
        defaultSignals.put("signal_hello", new SignalData(
                "signal_hello",
                "Приветствие с Земли. Мы мирные исследователи. Просим установить контакт.",
                1
        ));
        defaultSignals.put("signal_warning", new SignalData(
                "signal_warning",
                "ВНИМАНИЕ! Обнаружена аномалия в секторе 7. Всем кораблям избегать этой зоны. Повторяю: избегать сектор 7!",
                2
        ));
        defaultSignals.put("signal_secret", new SignalData(
                "signal_secret",
                "Проект 'NEBULA' активирован. Координаты базы: 45.2, -78.9, 12.4. Код доступа: OMEGA-7-ALPHA.",
                3
        ));

        for (Map.Entry<String, SignalData> entry : defaultSignals.entrySet()) {
            Path signalFile = signalsFolder.resolve(entry.getKey() + ".json");
            String json = GSON.toJson(entry.getValue());
            Files.writeString(signalFile, json);
            SkulkNebulaMod.LOGGER.warning("Created default signal: {" + entry.getKey() + "}");
        }
    }

    private static void loadAllSignals() {
        SIGNALS.clear();

        try {
            Files.list(signalsFolder)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            SignalData signal = GSON.fromJson(content, SignalData.class);
                            String id = path.getFileName().toString().replace(".json", "");
                            SIGNALS.put(id, signal);
                            SkulkNebulaMod.LOGGER.info("Loaded signal: {} -> {} + " + id + signal.content().substring(0, Math.min(30, signal.content().length())) + "...");
                        } catch (IOException e) {
                            SkulkNebulaMod.LOGGER.warning("Failed to load signal: {" + e +"," +path + "}");
                        }
                    });

            SkulkNebulaMod.LOGGER.info("Total signals loaded: {" + SIGNALS.size() + "}");
        } catch (IOException e) {
            SkulkNebulaMod.LOGGER.warning("Failed to list signals folder " + e);
        }
    }

    public static SignalData loadSignal(String signalId) {
        if (!initialized) {
            init();
        }

        SignalData signal = SIGNALS.get(signalId);
        if (signal == null) {
            SkulkNebulaMod.LOGGER.warning("Signal not found: {"+ signalId + "}, reloading...");
            loadAllSignals();
            signal = SIGNALS.get(signalId);
        }

        if (signal == null) {
            SkulkNebulaMod.LOGGER.warning("Signal still not found after reload: {" + signalId + "} ");
        }

        return signal;
    }

    public static void reload() {
        loadAllSignals();
    }

    public static void listSignals(ServerCommandSource source) {
        if (!initialized) {
            init();
        }

        source.sendFeedback(() -> Text.literal("§e=== Доступные сигналы ==="), false);
        source.sendFeedback(() -> Text.literal("§7Папка: " + signalsFolder), false);

        if (SIGNALS.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§cСигналы не найдены!"), false);
        } else {
            for (String id : SIGNALS.keySet()) {
                source.sendFeedback(() -> Text.literal("§a- " + id), false);
            }
        }
    }
}