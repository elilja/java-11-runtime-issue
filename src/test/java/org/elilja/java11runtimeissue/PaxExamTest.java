package org.elilja.java11runtimeissue;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.vmOption;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxExamTest {

    private static final Logger LOG = LogManager.getLogger(PaxExamTest.class);

    private static final String PAX_LOGGING_CFG_NAME = "org.ops4j.pax.logging.cfg";

    @Configuration
    public Option[] config() {
        return new Option[]{
                provisionKarafFramework(),
                keepRuntimeFolder(),
                disableLocalShell(),
                vmOptions(),
                setUpContainerLogging()
        };
    }

    @Test
    public void testShowcaseJava11RuntimeIssue() {
        LOG.info("Test case started (which does nothing than bring up the container)...");
        // Also see karaf.log in: target/<uuid>/data/log/karaf.log
    }

    private static Option provisionKarafFramework() {
        final MavenArtifactUrlReference karafFrameworkMavenUrl = maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .versionAsInProject()
                .type("zip");

        return KarafDistributionOption.karafDistributionConfiguration()
                .frameworkUrl(karafFrameworkMavenUrl)
                .unpackDirectory(new File("target"));
    }

    private static Option keepRuntimeFolder() {
        return KarafDistributionOption.keepRuntimeFolder();
    }

    private static Option disableLocalShell() {
        return KarafDistributionOption.configureConsole().ignoreLocalConsole();
    }

    private static String getKarafVersion() {
        final Path depProp = Paths.get("target/classes/META-INF/maven/dependencies.properties");

        try {
            if (Files.exists(depProp)) {
                return Files.lines(depProp)
                        .filter(s -> startsWith(s, "org.apache.karaf/apache-karaf/version ="))
                        .map(s -> substringAfter(s, "=").trim())
                        .filter(StringUtils::isNotBlank)
                        .findFirst()
                        .orElseThrow();
            } else {
                throw new IllegalStateException("Cannot find dependency information");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read dependency information", e);
        }
    }

    private static Option vmOptions() {
        final String karafVersion = getKarafVersion();

        LOG.info("Obtained karaf version '{}'", karafVersion);

        return composite(
                vmOption("--add-exports=java.base/org.apache.karaf.specs.locator=java.xml,ALL-UNNAMED"),
                vmOption("--patch-module"),
                vmOption("java.base=lib/endorsed/org.apache.karaf.specs.locator-" + karafVersion + ".jar")
        );
    }

    private static Option setUpContainerLogging() {
        final URL logCfg = PaxExamTest.class.getResource("/" + PAX_LOGGING_CFG_NAME);

        try {
            return composite(
                    KarafDistributionOption.replaceConfigurationFile("etc/" + PAX_LOGGING_CFG_NAME, new File(logCfg.toURI())),
                    KarafDistributionOption.doNotModifyLogConfiguration());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Problems loading supplied pax-logging config", e);
        }
    }
}
