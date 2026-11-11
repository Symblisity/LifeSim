package brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuronLayer {

    private final List<Neuron> neurons = new ArrayList<>();
    private int inputSize;

    public NeuronLayer(int inputSize, int size, Random rand){
        this.inputSize = inputSize;
        for(int i = 0; i < size; i++){
            neurons.add(new Neuron(inputSize, rand));
        }
    }

    private NeuronLayer(int inputSize){
        this.inputSize = inputSize;
    }

    // Passtrough Neuron Layer
    public static NeuronLayer identity(int size){
        NeuronLayer neuronLayer = new NeuronLayer(size);
        for(int i = 0; i < size; i++){
            neuronLayer.neurons.add(Neuron.identity(size, i));
        }
        return neuronLayer;
    }

    public double[] forward(double[] inputs){
        double[] outputs = new double[neurons.size()];
        for(int i = 0; i < outputs.length; i++){
            outputs[i] = neurons .get(i).activate(inputs);
        }
        return outputs;
    }

    public NeuronLayer copy() {
        NeuronLayer layer = new NeuronLayer(inputSize);
        for(Neuron neuron : neurons){
            layer.neurons.add(neuron.copy());
        }
        return layer;
    }

    public void mutate(double rate, double strength, Random rand) {
        for(Neuron neuron : neurons) {
            neuron.mutate(rate, strength, rand);
        }
    }

    public void addNeuron(Random rand){
        neurons.add(new Neuron(inputSize, rand));
    }

    public void addInput(){
        inputSize++;
        for(Neuron neuron : neurons){
            neuron.addInput();
        }
    }

    public int size() {
        return neurons.size();
    }

    public int getInputSize() {
        return inputSize;
    }
}
