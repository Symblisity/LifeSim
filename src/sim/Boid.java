package sim;

public final class Boid {

    public double x;
    public double y;
    public double angle;
    public double speed;
    public double energy;

    public Boid(double x, double y, double angle, double speed, double energy) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.energy = energy;
    }
}
