package org.elilja.java11runtimeissue;

import static org.ops4j.pax.exam.CoreOptions.maven;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;

@RunWith(PaxExam.class)
public class PaxExamTest {

    @Configuration
    public Option[] config() {
        return new Option[]{
                provisionKarafFramework(),
                keepRuntimeFolder(),
                disableLocalShell(),
        };
    }

    @Test
    public void testShowcaseJava11RuntimeIssue() {
        // See logs in target folder afterwards, will con
    }

    private static Option provisionKarafFramework() {
        final MavenUrlReference karafFrameworkMavenUrl = maven()
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
}
