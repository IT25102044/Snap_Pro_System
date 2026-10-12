package com.event.snappro.SnapPro.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
public class BrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {

        String url =
                "http://localhost:8080/equipment-dashboard.html";

        try {

            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop()
                    .isSupported(Desktop.Action.BROWSE)) {

                Desktop.getDesktop()
                        .browse(new URI(url));

            } else {
                System.out.println(
                        "Open manually: " + url
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not automatically open browser."
            );

            System.out.println(
                    "Open manually: " + url
            );
        }
    }
}