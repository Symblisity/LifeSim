package sim;

import Config.Config;
import brain.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class World {

    private final int width;
    private final int height;
    private final double maxTurn;
    private final double maxSpeed;
    private final double hidden;

    private final Random random;
    private final Vision vision;

    private final List<Boid> boids = new ArrayList<>();
    private final List<Food> food = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    public World(Config config, Random random) {
        this.random = random;
        this.width = config.getInt("world.width");
        this.height = config.getInt("world.height");
        this.maxTurn = config.getDouble("boid.turn.max");

        this.maxSpeed = config.getDouble("boid.speed.max");
        double startEnergy = config.getDouble("boid.energy.start");

        int obstacleCount = config.getInt("world.obstacle.count");
        double rMin = config.getDouble("world.obstacle.radius.min");
        double rMax = config.getDouble("world.obstacle.radius.max");

        this. hidden = config.getDouble("boid.brain.hidden");

        this.vision = new Vision(config);

        for (int i = 0; i < obstacleCount; i++) {
            obstacles.add(new Obstacle(
                    random.nextDouble() * width,
                    random.nextDouble() * height,
                    rMin + random.nextDouble() * (rMax - rMin)));
        }

        int foodCount = config.getInt("world.food.count");
        for (int i = 0; i < foodCount; i++) {
            food.add(new Food(random.nextDouble() * width, random.nextDouble() * height));
        }

        int boidCount = config.getInt("population.start");

        for (int i = 0; i < boidCount; i++) {
            boids.add(
                new Boid(
                    random.nextDouble() * width,
                    random.nextDouble() * height,
                    random.nextDouble() * Math.PI * 2,
                    maxSpeed,
                    startEnergy,
                    new NeuralNetwork(
                            random,
                            vision.inputSize(),
                            2,
                            (int) hidden
                    )
                )
            );
        }

    }

    public void tick() {
        for(Boid b : boids) {
            double[] out = b.brain.feedForward(vision.sense(b, this));

            b.angle += out[0] * maxTurn;
            b.speed = (out[1] + 1) / 2 * maxSpeed;

            b.x += Math.cos(b.angle) * b.speed;
            b.y += Math.sin(b.angle) * b.speed;

            b.x = ((b.x % width) + width) % width;
            b.y = ((b.y % height) + height) % height;
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<Boid> boids() {
        return boids;
    }

    public List<Food> food() {
        return food;
    }

    public List<Obstacle> obstacles() {
        return obstacles;
    }

}
