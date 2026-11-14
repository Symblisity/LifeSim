# LifeSim

A 2D desktop simulation of boids whose behavior is **not** driven by hand-written steering rules (separation, alignment, cohesion) but by a small **neural network per boid** that evolves over generations. The goal is to sit back and watch intelligent behavior emerge in a living world.

This is a personal hobby project. The README is written for the author and for AI coding agents working on the codebase, so it is explicit about the current state and the intended end goal.

---

## Vision (target state)

- **Brain = neural network.** Every boid decides how to move using its own feed-forward network.
- **Evolution instead of training.** There is no backpropagation. Boids that gather enough energy reproduce; children inherit the parent's brain with random mutations.
- **Structural mutation only at reproduction.** A child may get changed weights, new neurons or new layers. A living boid's brain never changes.
- **Open-ended world.** 2D world with food and obstacles. Other boids can act as enemies/prey, so different diets can emerge (plant-eaters vs. boids that specialize in hunting other boids).
- **Everyone evolves at the same time.** All boids live side by side in one world; learning is observable live.
- **Scale.** Start with a few dozen agents, later hundreds up to roughly a thousand.
- **Single config file.** All tunable values live in `config.properties`.

## Current status

| Area | Status |
|---|---|
| Config loading (`config.properties`) | Done |
| Fixed-timestep simulation loop on its own thread | Done |
| Swing rendering (boids, food, obstacles) | Done |
| Neural network (feed-forward, `tanh`) | Done |
| Network mutation (weights, add neuron, add layer) | Done, **not yet used** (no reproduction yet) |
| Sensing via vision rays (`Vision`) | Done |
| Boid movement driven by its brain | Done (random initial brains, no selection yet) |
| Energy, eating, starvation | Not started (field, config keys and brain input exist) |
| Food respawn | Not started (config key exists) |
| Obstacle collision | Not started |
| Reproduction and inheritance | Not started |
| Predator/prey interaction | Not started |
| Live statistics / UI controls | Not started |

### What runs today

Starting the app opens a window with `population.start` white boids. Each boid has a randomly initialized neural network, "sees" its surroundings through vision rays and steers and changes speed based on the network output. Because nothing rewards or punishes behavior yet, boids do not eat, do not lose energy, do not die and do not collide with obstacles. The brains are random, so movement looks erratic until selection pressure is added.

## Requirements

- **JDK 27** (developed with Oracle OpenJDK 27.0.1). The code uses records, so Java 16+ is the hard minimum.
- A desktop environment (Swing/AWT window, no headless mode).
- No external libraries and no build tool. The project is a plain IntelliJ IDEA project.

## Running

**IntelliJ IDEA (recommended):** run `App.Main`. The default working directory is the project root, where `config.properties` is found.

**Command line:** compile all sources into an output folder, then run from the directory that contains `config.properties`:

```bash
javac -d out $(find . -name "*.java")
java -cp out App.Main
```

A custom config file can be passed as the first argument:

```bash
java -cp out App.Main path/to/other.properties
```

## Project structure

Packages are top-level (no `src/main/java` convention is enforced). Note that `Main` lives in package `App` and the config class in package `Config` (capitalized).

| Package | Class | Purpose |
|---|---|---|
| `App` | `Main` | Entry point. Loads config, builds the world, starts the simulation thread, opens the window. |
| `Config` | `Config` | Thin wrapper around `java.util.Properties`. `getInt`, `getDouble`, `getString`; throws `IllegalStateException` on missing keys. |
| `sim` | `World` | Holds boids, food and obstacles; `tick()` senses, thinks and moves every boid. |
| `sim` | `Boid` | Mutable state: position, angle, speed, energy, plus its `NeuralNetwork brain`. |
| `sim` | `Vision` | Casts vision rays and builds the brain's input vector (see below). |
| `sim` | `Food`, `Obstacle` | Immutable records (position, and radius for obstacles). |
| `sim` | `SimulationLoop` | Runs `world.tick()` at a fixed rate on a daemon thread. Supports a speed multiplier (ticks per step). |
| `brain` | `NeuralNetwork` | Layer stack with `feedForward`, `copy`, `offspring`, `mutate`, `describe`. |
| `brain` | `NeuronLayer` | List of neurons; supports adding a neuron or an input, and an identity (pass-through) layer. |
| `brain` | `Neuron` | Weights + bias, `tanh` activation (or linear for identity neurons), Gaussian mutation. |
| `ui` | `MainWindow`, `SimPanel` | Swing window and custom-painted panel; repaint driven by a `javax.swing.Timer`. |

## How a boid decides (per tick)

1. `Vision.sense(boid, world)` builds the input vector.
2. `boid.brain.feedForward(inputs)` returns two values in (-1, 1).
3. Output 0 turns the boid: `angle += out[0] * boid.turn.max`.
4. Output 1 sets the speed: `speed = (out[1] + 1) / 2 * boid.speed.max`, i.e. 0..max.
5. The boid moves along its angle and wraps around the world edges.

### Brain inputs (`Vision`)

Input size is `rays * 2 + 1`:

| Index | Meaning |
|---|---|
| `2*i` | Closeness of the nearest object hit by ray `i`: `0` = nothing seen, `1` = touching |
| `2*i + 1` | Type code of that object |
| last | Own energy divided by `evolution.reproduction.energyThreshold`, capped at 1 |

Type codes: food `1.0`, boid `0.5`, nothing `0.0`, obstacle `-1.0`.

Rays are spread evenly over `boid.vision.fovDegrees`, centered on the boid's facing direction, and reach `boid.vision.range` pixels. Each ray is tested against every obstacle, food item and other boid (ray vs. circle) and reports the nearest hit. Food is treated as a circle of radius 2, boids with `boid.radius`.

### Brain structure and mutation

- Fully connected feed-forward network, `tanh` activation on every normal neuron.
- Created with `new NeuralNetwork(rand, inputSize, outputSize, hiddenLayers...)`. Currently: one hidden layer of `boid.brain.hidden` neurons and 2 outputs.
- `offspring(params, rand)` copies the network and mutates the copy using `MutationParams`:

| Parameter | Default | Meaning |
|---|---|---|
| `weightRate` | 0.1 | Chance per weight/bias to be changed |
| `weightStrength` | 0.3 | Std. deviation of the Gaussian change |
| `addNeuronChance` | 0.05 | Chance per reproduction to add a neuron to a hidden layer |
| `addLayerChance` | 0.02 | Chance per reproduction to insert a new hidden layer |
| `maxNeuronsPerLayer` | 32 | Upper limit for neurons per hidden layer |
| `maxHiddenLayers` | 6 | Upper limit for hidden layers |

- **Neutral growth:** a new neuron gets zero weight on the next layer's input, so adding a neuron does not change the output. A new layer is an identity layer made of *linear* neurons (weight 1 on one input, bias 0, no `tanh`), so inserting it does not change the output either.
- **Identity neurons lose their linearity on mutation:** as soon as a linear neuron's weight or bias is actually mutated, it becomes a normal `tanh` neuron. The flag is copied when a network is copied.

## Configuration

All values are in `config.properties`. Keys currently read by code are marked with a check.

| Key | Default | Used | Description |
|---|---|---|---|
| `world.width` / `world.height` | 1200 / 800 | ✔ | World size in pixels (also the window size) |
| `world.obstacle.count` | 8 | ✔ | Number of obstacles |
| `world.obstacle.radius.min` / `.max` | 15 / 45 | ✔ | Obstacle radius range |
| `world.food.count` | 150 | ✔ | Initial food items |
| `world.food.respawn.rate` | 0.5 | – | Planned: food respawn rate |
| `population.start` | 30 | ✔ | Initial number of boids |
| `boid.speed.max` | 3.0 | ✔ | Upper limit for speed (brain output scales to it) |
| `boid.turn.max` | 0.15 | ✔ | Max turn per tick in radians |
| `boid.radius` | 4 | ✔ | Drawing radius, also used for sensing other boids |
| `boid.energy.start` | 100 | ✔ | Starting energy |
| `boid.vision.range` | 100 | ✔ | Ray length in pixels |
| `boid.vision.rays` | 5 | ✔ | Number of vision rays |
| `boid.vision.fovDegrees` | 120 | ✔ | Total field of view in degrees |
| `boid.brain.hidden` | 8 | ✔ | Neurons in the initial hidden layer |
| `evolution.mutation.rate` | 0.05 | – | Planned: weight mutation chance |
| `evolution.mutation.strength` | 0.3 | – | Planned: weight mutation strength |
| `evolution.reproduction.energyThreshold` | 150 | ✔ | Energy needed to reproduce (currently only used to normalize the energy input) |
| `simulation.tickRate` | 60 | ✔ | Ticks per second |
| `simulation.speedMultiplier` | 1 | ✔ | Simulation ticks per step |
| `ui.repaintIntervalMs` | 16 | ✔ | Repaint interval (about 60 FPS) |

Missing keys throw `IllegalStateException("Missing config key: ...")` on first access, so new code should add its keys to `config.properties` together with the code that reads them.

Note: the network's input size depends on `boid.vision.rays`. Changing it only affects newly created brains; there is no saved-brain format yet.

## Threading model

- The simulation runs on a daemon thread named `simulation`.
- The Swing thread paints on a timer.
- Both synchronize on the `World` instance: `SimulationLoop` holds the lock while ticking, `SimPanel.paintComponent` holds it while drawing. Any new code that reads or modifies world state from another thread must also `synchronize (world)`.

## Known issues and notes

- **Sensing ignores world wrapping:** boids move across the world edges, but rays do not. A boid near the right edge does not see food just behind the wrap on the left.
- **Type code is a single number:** food, boid and obstacle are encoded on one scale, which suggests an ordering that does not exist. It works, but may need more generations to learn. The constants are in `Vision`.
- **Sensing cost:** each boid tests every object per ray, so cost grows with boids x objects. A spatial grid will be needed for hundreds to a thousand boids.
- **Sensing order effect:** boids are updated one after another, so a boid later in the list sees already-moved neighbors.
- **Unseeded randomness:** `Main` uses `new Random()`, so runs are not reproducible. Consider a configurable seed for debugging.

## Roadmap

1. Energy model: movement cost, eating food, starvation and death.
2. Food respawn and obstacle collision.
3. Reproduction at the energy threshold with `offspring(...)` mutation.
4. Predation: boids can eat other boids; observe diet specialization.
5. Live statistics (population, average energy, network sizes) and simulation controls (speed, pause).
6. Performance work for hundreds to a thousand boids (spatial grid for neighbor lookups).
7. Optional: saving and loading brains.