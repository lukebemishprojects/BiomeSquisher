package dev.lukebemish.biomesquisher.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.io.IOException;
import java.util.Locale;

public class BiomeSquisherCommands {
    private static final SimpleCommandExceptionType ERROR_LEVEL_NOT_MULTINOISE = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.dump.level_not_multinoise")
    );

    private static final SimpleCommandExceptionType ERROR_SAVING_DUMP = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.dump.failed_to_save")
    );

    private static final SimpleCommandExceptionType ERROR_GENERATING_DUMP = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.dump.failed_to_generate")
    );

    private static final SimpleCommandExceptionType ERROR_STARTING_SERVER = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.serve.failed_to_start")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal(Utils.MOD_ID)
                .then(
                    Commands.literal("dump")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(
                            Commands.argument("x", StringArgumentType.word())
                                .suggests((c, builder) -> SharedSuggestionProvider.suggest(BiomeDumper.EXAMPLES, builder))
                                .then(
                                    Commands.argument("y", StringArgumentType.word())
                                        .suggests((c, builder) -> SharedSuggestionProvider.suggest(BiomeDumper.EXAMPLES, builder))
                                        .then(
                                            Commands.argument("i", FloatArgumentType.floatArg())
                                                .then(
                                                    Commands.argument("j", FloatArgumentType.floatArg())
                                                        .then(
                                                            Commands.argument("k", FloatArgumentType.floatArg())
                                                                .then(
                                                                    Commands.argument("l", FloatArgumentType.floatArg())
                                                                        .then(
                                                                            Commands.argument("xMin", FloatArgumentType.floatArg())
                                                                                .then(
                                                                                    Commands.argument("xMax", FloatArgumentType.floatArg())
                                                                                        .then(
                                                                                            Commands.argument("yMin", FloatArgumentType.floatArg())
                                                                                                .then(
                                                                                                    Commands.argument("yMax", FloatArgumentType.floatArg())
                                                                                                        .executes(
                                                                                                            commandContext -> {
                                                                                                                Dimension x;
                                                                                                                Dimension y;
                                                                                                                try {
                                                                                                                    x = Dimension.valueOf(commandContext.getArgument("x", String.class).toUpperCase(Locale.ROOT));
                                                                                                                } catch (IllegalArgumentException e) {
                                                                                                                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(commandContext.getArgument("x", String.class));
                                                                                                                }
                                                                                                                try {
                                                                                                                    y = Dimension.valueOf(commandContext.getArgument("y", String.class).toUpperCase(Locale.ROOT));
                                                                                                                } catch (IllegalArgumentException e) {
                                                                                                                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(commandContext.getArgument("y", String.class));
                                                                                                                }
                                                                                                                float i = commandContext.getArgument("i", Float.class);
                                                                                                                float j = commandContext.getArgument("j", Float.class);
                                                                                                                float k = commandContext.getArgument("k", Float.class);
                                                                                                                float l = commandContext.getArgument("l", Float.class);
                                                                                                                float xMin = commandContext.getArgument("xMin", Float.class);
                                                                                                                float xMax = commandContext.getArgument("xMax", Float.class);
                                                                                                                float yMin = commandContext.getArgument("yMin", Float.class);
                                                                                                                float yMax = commandContext.getArgument("yMax", Float.class);
                                                                                                                BiomeDumper.SliceLocation location = new BiomeDumper.SliceLocation(i, j, k, l);
                                                                                                                BiomeDumper.SliceFrame frame = new BiomeDumper.SliceFrame(xMin, xMax, yMin, yMax);
                                                                                                                return exportFor(
                                                                                                                    commandContext,
                                                                                                                    x, y, location, frame
                                                                                                                );
                                                                                                            }
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("serve")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(
                            Commands.literal("stop")
                                .executes(
                                    commandContext -> {
                                        WebServerThread.stopServer();
                                        return Command.SINGLE_SUCCESS;
                                    }
                                )
                        )
                        .then(
                            Commands.literal("start")
                                .then(
                                    Commands.argument("port", IntegerArgumentType.integer())
                                        .executes(
                                            commandContext -> {
                                                int port = commandContext.getArgument("port", Integer.class);
                                                setupServer(commandContext, port);
                                                commandContext.getSource().sendSuccess(() -> Component.translatable("commands.biomesquisher.serve.on_port",
                                                    Component.literal("http://localhost:"+port+"/").withStyle(
                                                        Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://localhost:"+port+"/")).withUnderlined(true)
                                                    )
                                                ), true);
                                                return Command.SINGLE_SUCCESS;
                                            }
                                        )
                                )
                        )
                )
        );
    }

    private static void setupServer(CommandContext<CommandSourceStack> commandContext, int port) throws CommandSyntaxException {
        var biomeSource = commandContext.getSource().getLevel().getChunkSource().getGenerator().getBiomeSource();

        if (!(biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            throw ERROR_LEVEL_NOT_MULTINOISE.create();
        }

        try {
            WebServerThread.startServer(
                new WebServerThread(
                    port,
                    biomeSource.possibleBiomes(),
                    (x, y, location, frame, output) -> BiomeDumper.dump(commandContext.getSource().getLevel(), multiNoiseBiomeSource, x, y, location, output, frame)
                )
            );
        } catch (Exception e) {
            Utils.LOGGER.error("Failed to start web server", e);
            throw ERROR_STARTING_SERVER.create();
        }
    }

    private static int exportFor(CommandContext<CommandSourceStack> commandContext, Dimension x, Dimension y, BiomeDumper.SliceLocation location, BiomeDumper.SliceFrame frame) throws CommandSyntaxException {
        var biomeSource = commandContext.getSource().getLevel().getChunkSource().getGenerator().getBiomeSource();

        if (!(biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            throw ERROR_LEVEL_NOT_MULTINOISE.create();
        }

        try {
            BiomeDumper.dumpPng(commandContext.getSource().getLevel(), multiNoiseBiomeSource, x, y, location, frame);
        } catch (IOException e) {
            Utils.LOGGER.error("Failed to save biome dump", e);
            throw ERROR_SAVING_DUMP.create();
        } catch (Exception e) {
            Utils.LOGGER.error("Failed to generate biome dump", e);
            throw ERROR_GENERATING_DUMP.create();
        }

        return Command.SINGLE_SUCCESS;
    }
}
