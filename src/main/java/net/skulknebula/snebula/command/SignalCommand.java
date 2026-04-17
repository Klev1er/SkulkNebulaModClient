package net.skulknebula.snebula.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.skulknebula.snebula.signal.DecryptionManager;
import net.skulknebula.snebula.signal.SignalLoader;

public class SignalCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("signal")
                .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS))) // Уровень 2 = GameMaster
                .then(CommandManager.literal("send")
                        .then(CommandManager.argument("signal_id", StringArgumentType.word())
                                .executes(context -> {
                                    String signalId = StringArgumentType.getString(context, "signal_id");

                                    // Отправляем сигнал ВСЕМ игрокам
                                    for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        DecryptionManager.getInstance().addSignal(signalId);
                                        player.sendMessage(Text.literal("§a[СИГНАЛ] §eОбнаружен новый сигнал: " + signalId), false);
                                    }

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("§aСигнал '" + signalId + "' отправлен всем игрокам!"),
                                            true
                                    );

                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            SignalLoader.init(); // Инициализируем заново
                            context.getSource().sendFeedback(
                                    () -> Text.literal("§aСигналы перезагружены!"),
                                    true
                            );
                            return 1;
                        })
                )
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            SignalLoader.listSignals(context.getSource());
                            return 1;
                        })
                )
        );
    }
}