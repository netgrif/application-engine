package com.netgrif.workflow

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import java.awt.Font
import java.awt.GraphicsEnvironment

@Component
class FontInstaller implements CommandLineRunner {

    private final FONT_PATH = "src/main/resources/fonts/"
    private final FONT_NAMES = ["Klavika Rg", "Klavika Md", "Klavika Lt", "Klavika Bd"]

    private final Logger log = LoggerFactory.getLogger(FontInstaller.class)

    @Override
    void run(String... strings) throws Exception {
        if (isNotInstalled()) {
            log.info("Fonts ${FONT_NAMES} not found.")
            try {
                install()
                log.info("Fonts ${FONT_NAMES} installed succesfully")
            } catch (FontInstallException ignored) {
                log.error("Installation of fonts ${FONT_NAMES} failed")
            }
        } else {
            log.info("Fonts ${FONT_NAMES} are already installed")
        }
    }

    private boolean isNotInstalled() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        String[] fonts = graphicsEnvironment.getAvailableFontFamilyNames()

        return (fonts as Set).intersect(FONT_NAMES).size() != FONT_NAMES.size()
    }

    private void install() {
        if (isWindows()) {
            installOnWindows()
        } else {
            installOnUnix()
        }
    }

    private boolean isWindows() {
        return System.properties['os.name'].toString().toLowerCase().contains('windows')
    }

    private void installOnWindows() throws FontInstallException {
        try {
            GraphicsEnvironment gr = GraphicsEnvironment.getLocalGraphicsEnvironment()
            new File(FONT_PATH).eachFile { file ->
                Font fontFile = Font.createFont(Font.TRUETYPE_FONT, file)
                gr.registerFont(fontFile)
            }
        } catch (Exception ignored) {
            throw new FontInstallException(ignored.message)
        }
    }

    private void installOnUnix() throws FontInstallException {
        def result = "cp -r ${FONT_PATH} /usr/share/fonts/truetype/".execute()
        if (!result.exitValue()) {
            throw new FontInstallException("Command exited with value ${result.exitValue()}: ${result.text}")
        }
    }

    class FontInstallException extends Exception {
        FontInstallException(String message) {
            super(message)
        }
    }
}