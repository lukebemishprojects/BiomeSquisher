package dev.lukebemish.biomesquisher.test.neoforge;

import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.test.BiomeSquisherGameTests;
import dev.lukebemish.biomesquisher.test.TestUtils;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Mod(TestUtils.MOD_ID)
public class BiomeSquisherTest {
    public BiomeSquisherTest() throws ParserConfigurationException, IOException {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(RegisterGameTestsEvent.class, this::registerTests);

        var testOutputDir = FMLPaths.GAMEDIR.get().resolve("build");
        Files.createDirectories(testOutputDir);
        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(testOutputDir.resolve("junit.xml").toFile()));
    }

    private void registerTests(RegisterGameTestsEvent event) {
        event.register(GeneratedTests.class);
    }



    @GameTestHolder(Utils.MOD_ID+"tests")
    public static class GeneratedTests {
        @GameTestGenerator
        public static Collection<TestFunction> exampleTests() {
            List<TestFunction> tests = new ArrayList<>();

            fromClass(BiomeSquisherGameTests.class, tests);

            return tests;
        }

        public static void fromClass(Class<?> clazz, List<TestFunction> tests) {
            for (var method : clazz.getMethods()) {
                if (method.isAnnotationPresent(GameTest.class)) {
                    var gameTest = method.getAnnotation(GameTest.class);
                    String string3 = method.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT) + '.' + method.getName().toLowerCase(Locale.ROOT);
                    String string4 = gameTest.template().isEmpty() ? method.getName().toLowerCase(Locale.ROOT) : gameTest.template();
                    String string5 = gameTest.batch();
                    Rotation rotation = StructureUtils.getRotationForRotationSteps(gameTest.rotationSteps());
                    tests.add(new TestFunction(
                        string5,
                        string3,
                        string4,
                        rotation,
                        gameTest.timeoutTicks(),
                        gameTest.setupTicks(),
                        gameTest.required(),
                        gameTest.requiredSuccesses(),
                        gameTest.attempts(),
                        makeConsumer(method)
                    ));
                } else if (method.isAnnotationPresent(GameTestGenerator.class)) {
                    try {
                        var uncheckedTest = (Collection<?>) method.invoke(null);
                        for (var test : uncheckedTest) {
                            tests.add((TestFunction) test);
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        private static Consumer<GameTestHelper> makeConsumer(Method method) {
            return helper -> {
                try {
                    Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                    method.invoke(instance, helper);
                } catch (InvocationTargetException var3) {
                    if (var3.getCause() instanceof RuntimeException) {
                        throw (RuntimeException)var3.getCause();
                    } else {
                        throw new RuntimeException(var3.getCause());
                    }
                } catch (ReflectiveOperationException var4) {
                    throw new RuntimeException(var4);
                }
            };
        }
    }
}
