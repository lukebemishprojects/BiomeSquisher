package dev.lukebemish.biomesquisher.test;

import com.google.common.collect.Maps;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public class CustomTestReporter implements TestReporter {
    private final Document document;
    private final Element testSuites;
    private final File destination;

    private final Map<String, Element> batchTestSuites = Maps.newHashMap();
    private final Map<String, Long> testSuiteTimes = Maps.newHashMap();

    public CustomTestReporter(File destination) throws ParserConfigurationException {
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.testSuites = this.document.createElement("testsuites");
        this.testSuites.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        this.document.appendChild(testSuites);
        this.destination = destination;
    }

    @Override
    public void onTestFailed(@NotNull GameTestInfo testInfo) {
        var errorMessage = Objects.requireNonNull(testInfo.getError()).getMessage();
        if (testInfo.isRequired()) {
            var element = this.document.createElement("failure");
            element.setAttribute("message", errorMessage);
            this.createTestCase(testInfo).appendChild(element);
        } else {
            var element = this.document.createElement("skipped");
            element.setAttribute("message", errorMessage);
            this.createTestCase(testInfo).appendChild(element);
        }
    }

    private Element createTestCase(GameTestInfo testInfo) {
        Element element = this.document.createElement("testcase");
        element.setAttribute("name", testInfo.getTestName());
        element.setAttribute("classname", testInfo.getTestName());
        element.setAttribute("time", String.valueOf((double)testInfo.getRunTime() / 1000.0));
        var batch = batchTestSuites.computeIfAbsent(testInfo.getTestFunction().getBatchName(), s -> {
            Element batchElement = this.document.createElement("testsuite");
            batchElement.setAttribute("name", s);
            batchElement.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            this.testSuites.appendChild(batchElement);
            return batchElement;
        });
        var batchTime = testSuiteTimes.getOrDefault(testInfo.getTestFunction().getBatchName(), 0L);
        testSuiteTimes.put(testInfo.getTestFunction().getBatchName(), batchTime + testInfo.getRunTime());
        batch.appendChild(element);
        return element;
    }

    @Override
    public void onTestSuccess(@NotNull GameTestInfo testInfo) {
        this.createTestCase(testInfo);
    }

    @Override
    public void finish() {
        long totalTime = 0;
        for (var entry : testSuiteTimes.entrySet()) {
            var batchElement = Objects.requireNonNull(batchTestSuites.get(entry.getKey()));
            batchElement.setAttribute("time", String.valueOf((double)entry.getValue() / 1000.0));
            totalTime += entry.getValue();
        }
        this.testSuites.setAttribute("time", String.valueOf((double)totalTime / 1000.0));

        try {
            TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(this.document),
                new StreamResult(destination)
            );
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
