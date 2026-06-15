package io.github.tessG.processing.generator

import java.nio.file.Files
import java.nio.file.Path

object ProjectGenerator {

    data class ProjectConfig(
        val projectRoot: Path,
        val artifactId: String,
        val groupId: String,
        val mainClassName: String,
        val processingVersion: String
    )

    fun generate(config: ProjectConfig) {
        Files.createDirectories(config.projectRoot)
        writePom(config)
        writeSketch(config)
    }

    private fun writePom(config: ProjectConfig) {
        val content = """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
            <modelVersion>4.0.0</modelVersion>

            <groupId>${config.groupId}</groupId>
            <artifactId>${config.artifactId}</artifactId>
            <version>1.0-SNAPSHOT</version>

            <dependencies>
                <dependency>
                    <groupId>org.processing</groupId>
                    <artifactId>core</artifactId>
                    <version>${config.processingVersion}</version>
                </dependency>
            </dependencies>
            
            
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>exec-maven-plugin</artifactId>
                        <version>3.3.0</version>
                        <configuration>
                            <mainClass>${config.groupId}.${config.mainClassName}</mainClass>
                            <jvmArgs>
                                <jvmArg>--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED</jvmArg>
                            </jvmArgs>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </project>
    """.trimIndent()

        config.projectRoot.resolve("pom.xml")
            .toFile().writeText(content)
    }

    private fun writeSketch(config: ProjectConfig) {
        val groupPath = config.groupId.replace('.', '/')
        val sketchDir = config.projectRoot
            .resolve("src/main/java/$groupPath")

        Files.createDirectories(sketchDir)

        val content = """
        package ${config.groupId};
        
        import processing.core.PApplet;
        
        public class ${config.mainClassName} extends PApplet {
        
            @Override
            public void settings() {
                size(800, 600);
            }
        
            @Override
            public void setup() {
                background(30);
            }
        
            @Override
            public void draw() {
                ellipse(mouseX, mouseY, 50, 50);
            }
        
            public static void main(String[] args) {
                PApplet.main(${config.mainClassName}.class.getName());
            }
        }
    """.trimIndent()

        sketchDir.resolve("${config.mainClassName}.java")
            .toFile().writeText(content)
    }

}