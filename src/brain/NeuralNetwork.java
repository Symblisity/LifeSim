package brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {

    public record MutationParams(
            double weightRate,        // Chance pro Gewicht, verändert zu werden
            double weightStrength,    // Stärke der Veränderung (Standardabweichung)
            double addNeuronChance,   // Chance pro Fortpflanzung
            double addLayerChance,    // Chance pro Fortpflanzung
            int maxNeuronsPerLayer,
            int maxHiddenLayers
    ) {
        public static MutationParams defaults() {
            return new MutationParams(0.1, 0.3, 0.05, 0.02, 32, 6);
        }
    }

    private final int inputSize;
    private final List<NeuronLayer> layers = new ArrayList<NeuronLayer>();

    public NeuralNetwork(Random rand, int inputSize, int outputSize, int... hiddenLayers) {
        this.inputSize = inputSize;
        int prev = inputSize;
        for (int h : hiddenLayers) {
            layers.add(new NeuronLayer(prev, h, rand));
            prev = h;
        }
        layers.add(new NeuronLayer(prev, outputSize, rand));
    }

    private NeuralNetwork(int inputSize) {
        this.inputSize = inputSize;
    }

    public double[] feedForward(double[] inputs) {
        if (inputs.length != inputSize) {
            throw new IllegalArgumentException(
                    "Expected " + inputSize + " inputs, got " + inputs.length
            );
        }
        double[] current = inputs;
        for (NeuronLayer layer : layers) {
            current = layer.forward(current);
        }
        return current;
    }

    public NeuralNetwork copy() {
        NeuralNetwork copy = new NeuralNetwork(inputSize);
        for (NeuronLayer layer : layers) {
            copy.layers.add(layer.copy());
        }
        return copy;
    }

    public NeuralNetwork offspring(MutationParams params, Random rand) {
        NeuralNetwork child = this.copy();
        child.mutate(params, rand);
        return child;
    }

    public void mutate(MutationParams params, Random rand) {
        for (NeuronLayer layer : layers) {
            layer.mutate(params.weightRate(), params.weightStrength(), rand);
        }
        if (rand.nextDouble() < params.addNeuronChance()) {
            addNeuron(params.maxNeuronsPerLayer(), rand);
        }
        if (rand.nextDouble() < params.addLayerChance()) {
            addLayer(params.maxHiddenLayers(), rand);
        }
    }

    private void addNeuron(int maxNeuronsPerLayer, Random rand) {
        int hiddenCount = layers.size() - 1;
        if(hiddenCount < 1) return;
        int index = rand.nextInt(hiddenCount);
        NeuronLayer layer = layers.get(index);
        if(layer.size() >= maxNeuronsPerLayer) return;
        layer.addNeuron(rand);
        layers.get(index + 1).addInput();
    }

    private void addLayer(int maxHiddenLayers, Random rand){
        int hiddenCount = layers.size() - 1;
        if(hiddenCount >= maxHiddenLayers) return;
        int position = rand.nextInt(hiddenCount + 1);
        int width = (position == 0) ? inputSize : layers.get(position - 1).size();
        layers.add(position, NeuronLayer.identity(width));
    }

    public int getHiddenLayersCount() {
        return layers.size() - 2;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder(String.valueOf(inputSize));
        for(NeuronLayer layer : layers) {
            sb.append(" -> ").append(layer.size());
        }
        return sb.toString();
    }
}