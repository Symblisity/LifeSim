package ui;

import sim.Boid;
import sim.Food;
import sim.Obstacle;
import sim.World;

import javax.swing.*;
import java.awt.*;

public final class SimPanel extends JPanel {

    private final World world;
    private final int boidRadius;

    public SimPanel(World world, int boidRadius) {
        this.world = world;
        this.boidRadius = boidRadius;
        setPreferredSize(new Dimension(world.width(), world.height()));
        setBackground(new Color(20, 24, 30));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        synchronized (world) {
            g.setColor(new Color(70, 75, 85));
            for(Obstacle o : world.obstacles()){
                int r = (int) o.radius();
                g.fillOval((int) o.x() - r, (int) o.y() -r, r * 2, r * 2);
            }

            g.setColor(new Color(80, 200, 100));
            for (Food food : world.food()){
                g.fillOval((int) food.x() - 2, (int) food.y() - 2, 4, 4);
            }

            g.setColor(new Color(240, 240, 240));
            for (Boid b : world.boids()) {
                int x = (int) b.x;
                int y = (int) b.y;
                g.fillOval(x - boidRadius, y - boidRadius, boidRadius * 2, boidRadius * 2);
                g.drawLine(x, y,
                        x + (int) (Math.cos(b.angle) * boidRadius * 2),
                        y + (int) (Math.sin(b.angle) * boidRadius * 2));
            }
        }
    }
}
