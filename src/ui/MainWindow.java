package ui;

import sim.World;

import javax.swing.*;
import java.awt.*;

public final class MainWindow extends JFrame {

    public MainWindow(World world, int boidRadius, int repaintIntervalMs) {
        super("LifeSim");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SimPanel simPanel = new SimPanel(world, boidRadius);
        add(simPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        new Timer(repaintIntervalMs, e -> simPanel.repaint()).start();
    }

}
