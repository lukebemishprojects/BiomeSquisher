package dev.lukebemish.biomesquisher.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.io.IOException;
import java.util.Locale;

public class BiomeSquisherCommands {
    private static final SimpleCommandExceptionType ERROR_LEVEL_NOT_MULTINOISE = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.dumpbiomes.level_not_multinoise")
    );

    private static final SimpleCommandExceptionType ERROR_SAVING_DUMP = new SimpleCommandExceptionType(
        Component.translatable("commands.biomesquisher.dumpbiomes.failed_to_save")
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
                                            Commands.argument("i", FloatArgumentType.floatArg(-1, 1))
                                                .then(
                                                    Commands.argument("j", FloatArgumentType.floatArg(-1, 1))
                                                        .then(
                                                            Commands.argument("k", FloatArgumentType.floatArg(-1, 1))
                                                                .then(
                                                                    Commands.argument("l", FloatArgumentType.floatArg(-1, 1))
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
                                                                                BiomeDumper.SliceLocation location = new BiomeDumper.SliceLocation(i, j, k, l);
                                                                                return exportFor(
                                                                                    commandContext,
                                                                                    x, y, location
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
        );
    }

    private static int exportFor(CommandContext<CommandSourceStack> commandContext, Dimension x, Dimension y, BiomeDumper.SliceLocation location) throws CommandSyntaxException {
        var biomeSource = commandContext.getSource().getLevel().getChunkSource().getGenerator().getBiomeSource();

        if (!(biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            throw ERROR_LEVEL_NOT_MULTINOISE.create();
        }

        try {
            BiomeDumper.dumpPng(commandContext.getSource().getLevel(), multiNoiseBiomeSource, x, y, location);
        } catch (IOException e) {
            Utils.LOGGER.error("Failed to save biome dump", e);
            throw ERROR_SAVING_DUMP.create();
        }

        return Command.SINGLE_SUCCESS;
    }
}
