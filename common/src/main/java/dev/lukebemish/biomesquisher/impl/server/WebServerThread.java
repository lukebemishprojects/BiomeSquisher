package dev.lukebemish.biomesquisher.impl.server;

import dev.lukebemish.biomesquisher.impl.Dimension;
import dev.lukebemish.biomesquisher.impl.Platform;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import dev.lukebemish.biomesquisher.impl.dump.PngOutput;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WebServerThread extends Thread {
    private final HttpServer server;
    private final ImageProvider imageProvider;
    private final Map<ResourceKey<Biome>, Integer> biomeColorHash;

    public WebServerThread(int listenerPort, Set<Holder<Biome>> possibleBiomes, ImageProvider imageProvider) {
        this.server = ServerBootstrap.bootstrap()
            .setListenerPort(listenerPort)
            .registerHandler("*", new BiomeRequestHandler())
            .create();
        this.imageProvider = imageProvider;
        this.biomeColorHash = PngOutput.biomeColorHash(possibleBiomes);
    }

    @Override
    public void run() {
        try {
            this.server.start();
            synchronized (this) {
                this.wait();
            }
        } catch (IOException | InterruptedException e) {
            this.server.shutdown(5, TimeUnit.SECONDS);
        }
    }

    private class BiomeRequestHandler implements HttpRequestHandler {
        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!"GET".equals(method) && !"HEAD".equals(method)) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            final String path = request.getRequestLine().getUri();
            if ("/".equals(path)) {
                response.setStatusCode(HttpStatus.SC_OK);
                byte[] bytes = Files.readAllBytes(Platform.INSTANCE.getRootResource("biome_dump.html").orElseThrow(() -> new IOException("biome_dump.html does not exist!")));
                final ByteArrayEntity body = new ByteArrayEntity(bytes, ContentType.create("text/html"));
                response.setEntity(body);
                return;
            } else {
                String[] parts = path.substring(1).split("\\+");
                if (parts.length == 10) {
                    try {
                        Dimension x = Dimension.values()[Integer.parseInt(parts[0])];
                        Dimension y = Dimension.values()[Integer.parseInt(parts[1])];
                        float i = Float.parseFloat(parts[2]);
                        float j = Float.parseFloat(parts[3]);
                        float k = Float.parseFloat(parts[4]);
                        float l = Float.parseFloat(parts[5]);
                        float xMin = Float.parseFloat(parts[6]);
                        float xMax = Float.parseFloat(parts[7]);
                        float yMin = Float.parseFloat(parts[8]);
                        float yMax = Float.parseFloat(parts[9]);
                        BiomeDumper.SliceLocation location = new BiomeDumper.SliceLocation(i, j, k, l);
                        BiomeDumper.SliceFrame frame = new BiomeDumper.SliceFrame(xMin, xMax, yMin, yMax);
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        BiomeDumper.Output output = (level, getter, biomes) ->
                            PngOutput.INSTANCE_256.dumpToOutput(outputStream, getter, biomes);
                        imageProvider.provide(x, y, location, frame, output);
                        response.setStatusCode(HttpStatus.SC_OK);
                        final ByteArrayEntity body = new ByteArrayEntity(outputStream.toByteArray(), ContentType.create("image/png"));
                        response.setEntity(body);
                        return;
                    } catch (IllegalArgumentException ignored) {
                        // fall through
                    }
                }
            }
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            final ByteArrayEntity body = new ByteArrayEntity("Page not found".getBytes(StandardCharsets.UTF_8), ContentType.create("text/html", StandardCharsets.UTF_8));
            response.setEntity(body);
        }
    }

    private static WebServerThread activeThread;
    private static final Lock SERVER_LOCK = new ReentrantLock();

    public static void startServer(WebServerThread thread) {
        if (!BiomeDumper.IS_PNGJ_PRESENT) {
            throw new IllegalStateException("PNGJ is not present; cannot start biome dump server!");
        }
        Thread startup = new Thread(() -> {
            SERVER_LOCK.lock();
            if (activeThread != null) {
                activeThread.interrupt();
            }
            activeThread = thread;
            thread.start();
            SERVER_LOCK.unlock();
        });
        startup.start();
    }

    public static void stopServer() {
        Thread shutdown = new Thread(WebServerThread::waitOnStopServer);
        shutdown.start();
    }

    public static void waitOnStopServer() {
        SERVER_LOCK.lock();
        if (activeThread != null) {
            activeThread.interrupt();
        }
        SERVER_LOCK.unlock();
    }

    public interface ImageProvider {
        void provide(Dimension x, Dimension y, BiomeDumper.SliceLocation location, BiomeDumper.SliceFrame frame, BiomeDumper.Output output) throws IOException;
    }
}
