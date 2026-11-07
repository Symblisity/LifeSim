package App;

import Config.Config;
import sim.SimulationLoop;
import sim.World;
import ui.MainWindow;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public final class Main {

    public static void main(String[] args) throws IOException {
        // IntelliJ runs from the project root by default, so config.properties is found there.
        Path configFile = Path.of(args.length > 0 ? args[0] : "config.properties");
        Config config = new Config(configFile);

        World world = new World(config, new Random());

        SimulationLoop loop = new SimulationLoop(
                world,
                config.getInt("simulation.tickRate"),
                config.getInt("simulation.speedMultiplier"));
        Thread simThread = new Thread(loop, "simulation");
        simThread.setDaemon(true);
        simThread.start();

        int boidRadius = config.getInt("boid.radius");
        int repaintMs = config.getInt("ui.repaintIntervalMs");
        SwingUtilities.invokeLater(() -> new MainWindow(world, boidRadius, repaintMs).setVisible(true));
    }
}