package dev.lukebemish.biomesquisher.test.fabric;

import dev.lukebemish.biomesquisher.test.CustomTestReporter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GlobalTestReporter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;

public class BiomeSquisherTest implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                var testOutputDir = FabricLoader.getInstance().getGameDir().resolve("build");
                Files.createDirectories(testOutputDir);
                GlobalTestReporter.replaceWith(new CustomTestReporter(testOutputDir.resolve("junit.xml").toFile()));
            } catch (IOException | ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
