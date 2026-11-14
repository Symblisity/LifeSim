package brain;

import java.util.Arrays;
import java.util.Random;

public class Neuron {

    private double[] weights;
    private double bias;
    private boolean linear;

    public Neuron(int inputsize, Random rand){
        this.weights = new double[inputsize];
        for(int i = 0; i < inputsize; i++){
            this.weights[i] = rand.nextGaussian() * 0.5;
        }
        this.bias = rand.nextGaussian() * 0.1;
    }

    private Neuron() {}

    /**
     * A Neuron wich just passes all Values through to the Neuron specified at index.
     */
    public static Neuron identity(int inputSize, int index){
        Neuron neuron = new Neuron();
        neuron.weights = new double[inputSize];
        neuron.weights[index] = 1.0;
        neuron.bias = 0.0;
        neuron.linear = true;
        return neuron;
    }

    public double activate(double[] inputs){
        double sum = this.bias;
        for(int i = 0; i < this.weights.length; i++){
            sum += this.weights[i] * inputs[i];
        }
        return linear ? sum : Math.tanh(sum);
    }

    public Neuron copy() {
        Neuron n = new Neuron();
        n.weights = this.weights.clone();
        n.bias = this.bias;
        n.linear = this.linear;
        return n;
    }

    public void mutate(double rate, double strength, Random rand) {
        for(int i = 0; i < this.weights.length; i++){
            if(rand.nextDouble() < rate){
                this.weights[i] += rand.nextGaussian() * strength;
                this.linear = false;
            }
        }
        if(rand.nextDouble() < rate){
            this.bias += rand.nextGaussian() * strength;
            this.linear = false;
        }
    }

    public void addInput() {
        this.weights = Arrays.copyOf(this.weights, this.weights.length + 1);
    }

    public int getInputSize() {
        return this.weights.length;
    }
}
