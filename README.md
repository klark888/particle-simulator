# particle-simulator
**Particle Simulator Project** is yet another N-body gravity particle simulator. However, this application can simulate more complex structures, such as the formation of protoplanetary disks, moons, galatic filaments, and planetary rings.
#
**Download** binaries from the release directory, or click this [link](https://raw.githubusercontent.com/klark888/particle-simulator/main/releases/Particle%20Simulator%20v0.3.0.jar) for the newest version.
#
**Usage - Menu Bar**

Simulation can be controlled using the menu bar. Here are descriptions of what some of the menu items do.

File >> Save: Save the current simulation as a file.

Simulation >> Tick Length: Changes the minimum time each tick length takes in miliseconds. For example, 20ms tick length would result in the simulation calculating 50 time steps in a second. To run the simulation as fast as possible, set the minimum time to -1.

Simulation >> Time Step: Changes how much time the simulation moves forwards each tick. Default is set to 1. Decreasing the time step would result in a slower simulation speed but for better accuracy, while an increase would result in a higher speed in exchange for accuracy. Users should decrease the time step if they see unexpected particle behavior (i.e. planets exploding).

View >> Zoom Out: Zooms the camera out in order to fit more particles on the screen.

**Usage - Simulators**
Change the type simulation engine and optimizations the application runs with this panel. The following are the three default types of simulator engines.

Simple: Basic implementation of an simulation engine.

Anti-Singularity: An implementation that focuses on preventing unstable acceleration vectors during close particle encounters. This is done by decreasing the time step when these encounters happen to ensure simulation accuracy.

Multi-Thread: An implementation that allows the simulation to utilize multiple threads.

**Usage - Experiments**

This menu tab spawns in various pre-made experiments or scenarios in the application that simulate various structures or phenomena in the universe.

Ring Formation: Shows how planetary rings form from moons that were ripped apart from tidal forces

Black Hole: Demonstrates how a star is consumed by a black hole and its accretion disk.

Penetration Collision: Shows how a shockwave forms when a dense object collides with a fragile structure.

Hit-And-Run Collision: Simulates a collision where a larger planet steals the mantle of a smaller one. This is similar to a collision that occurred between Mercury and another protoplanet in the early solar system that resulted in Mercury's elliptical orbit and large core relative to its total size.

Cosmological Sponge: Shows the formation of galactic filaments in the universe. There are invisible dark matter particles present in this simulation.

Moon-Creating Collision: Simulates a collision which produces a sizable moon that orbits the resulting planet. This is similar to how Earth's Moon formed.

Mantle Differentiation: Shows how the different layers in a planet's structure form after its mantle melts, where higher density material sink to the bottom to form a core while lighter elements float up to the top.

Angular Momentum: Demonstrates conservation of angular momentum.

Accretion Disk: Shows star and planet formation. Tick length should be set to -1 in order to move the simulation as fast as possible with the required accuracy.

Protoplanetary Disk: Similar to Accretion Disk experiment, but with a large star already formed at the center of the disk. Also recommend a tick length of -1.

**Usage - Object Creation Options Panel**

This allows the user to change the properties of the planets they spawn in. This panel can also change the properties of the experiments from the experiments menu tab by enabling File >> Reflect Experiments.

**Usage - Mouse**

*Click* to spawn in a planet. This planet would reflect the parameters specified by the object creation options panel.

*Drag* the mouse to move the camera.

*Scroll* to zoom the camera in or out.
