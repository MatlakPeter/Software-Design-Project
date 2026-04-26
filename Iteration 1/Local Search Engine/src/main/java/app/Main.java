package app;

import javax.swing.*;
import ui.SearchEngineUI;

public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(SearchEngineUI::new);
    }
}