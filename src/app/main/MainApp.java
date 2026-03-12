package app.main;

import app.ui.IncFormApp;

public class MainApp {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(() -> {

            new IncFormApp().setVisible(true);

        });

    }
}