package sim;

import Config.Config;

public final class Vision {

    private final static double TYPE_NOTHING = 0.0;
    private static final double TYPE_FOOD = 1.0;
    private static final double TYPE_OBSTACLE = -1.0;
    private static final double TYPE_BOID = 0.5;
    private static final double FOOD_RADIUS = 2.0;

    private final int rayCount;
    private final double fieldOfViewRad; // Rad
    private final double visionRange;
    private final double boidRadius;
    private final double energyScale;


    public Vision(Config config) {
        this.rayCount = config.getInt("boid.vision.rays");
        this.fieldOfViewRad = Math.toRadians(config.getDouble("boid.vision.fovDegrees"));
        this.visionRange = config.getDouble("boid.vision.range");
        this.boidRadius = config.getDouble("boid.radius");
        this.energyScale = config.getDouble("evolution.reproduction.energyThreshold");
    }

    /**
     * Count of Net-Inputs: 2 per ray + energy
     */
    public int inputSize() {
        return rayCount * 2 + 1;
    }

    /**
     * Builds the brain input for one boid.
     * Per ray: closeness (0 = nothing seen, 1 = touching) and the type code of the nearest object.
     * Last value: own energy, normalized to 0..1.
     */
    public double[] sense(Boid viewer, World world) {
        double[] brainInputs = new double[inputSize()];

        for(int rayIndex = 0; rayIndex < rayCount; rayIndex++) {

            double rayAngle = viewer.angle + rayAngleOffset(rayIndex);
            double rayDirectionX = Math.cos(rayAngle);
            double rayDirectionY = Math.sin(rayAngle);

            double nearestHitDistance = visionRange;
            double nearestHitType = TYPE_NOTHING;

            for(Obstacle obstacle : world.obstacles()){
                double distance = hitDistance(viewer, rayDirectionX, rayDirectionY, obstacle.x(), obstacle.y(), obstacle.radius());
                if(distance < nearestHitDistance){
                    nearestHitDistance = distance;
                    nearestHitType = TYPE_OBSTACLE;
                }
            }
            for (Food food : world.food()) {
                double distance = hitDistance(viewer, rayDirectionX, rayDirectionY, food.x(), food.y(), FOOD_RADIUS);
                if (distance < nearestHitDistance) {
                    nearestHitDistance = distance;
                    nearestHitType = TYPE_FOOD;
                }
            }
            for(Boid other : world.boids()) {
                if(other == viewer) continue;
                double distance = hitDistance(viewer, rayDirectionX, rayDirectionY, other.x, other.y, boidRadius);
                if(distance < nearestHitDistance){
                    nearestHitDistance = distance;
                    nearestHitType = TYPE_BOID;
                }
            }

            brainInputs[rayIndex * 2] = (nearestHitType == TYPE_NOTHING) ? 0 : 1.0 - nearestHitDistance / visionRange;
            brainInputs[rayIndex * 2 + 1] = nearestHitType;
        }

        brainInputs[brainInputs.length - 1] = Math.min(viewer.energy / energyScale, 1.0);
        return brainInputs;
    }

    public double hitDistance(Boid self, double rayDirectionX, double rayDirectionY, double otherX, double otherY, double radius){
        double toCircleX = otherX - self.x;
        double toCircleY = otherY - self.y;

        double distanceToCenterSquared = toCircleX * toCircleX + toCircleY * toCircleY;

        double maxRelevantDistance = visionRange + radius;
        if(distanceToCenterSquared > maxRelevantDistance * maxRelevantDistance) return Double.POSITIVE_INFINITY;

        double distanceAlongRay = toCircleX * rayDirectionX + toCircleY * rayDirectionY;
        double offAxisDistanceSquared = distanceToCenterSquared - distanceAlongRay * distanceAlongRay;
        double radiusSquared = radius * radius;
        if(offAxisDistanceSquared > radiusSquared) return Double.POSITIVE_INFINITY;

        double halfChord = Math.sqrt(radiusSquared - offAxisDistanceSquared);
        double entryDistance = distanceAlongRay - halfChord;
        double exitDistance = distanceAlongRay + halfChord;

        if(exitDistance < 0) return Double.POSITIVE_INFINITY;

        return Math.max(entryDistance, 0);
    }

    /**
     * Angle of one ray relative to the boid's facing direction.
     * The rays are spread evenly across the field of view, centered on 0.
     * Example with 3 rays and 120°: -60°, 0°, +60°.
     */
    private double rayAngleOffset(int rayIndex) {
        if (rayCount == 1) return 0.0;

        double leftmostAngle = -fieldOfViewRad / 2;
        double angleStep = fieldOfViewRad / (rayCount - 1);
        return leftmostAngle + rayIndex * angleStep;
    }
}
