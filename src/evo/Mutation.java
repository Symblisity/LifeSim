package evo;

import java.util.Random;

public class Mutation {

    private Mutation(){}

    public static Genome mutate(Genome parent, double rate, double strength, Random random){
        double[] weights = parent.weights().clone();
        for(int i = 0; i < weights.length; i++){
            if(random.nextDouble() < rate){
                weights[i] += random.nextGaussian() * strength;
            }
        }
        return new Genome(weights);
    }
}
