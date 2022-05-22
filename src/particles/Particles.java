/**
Particles: simulation
Copyright (C) 2022 Kent Fukuda
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
**/

package particles;

/* Author: Kent F.
 * Description: main class of the particle simulation
 * Created: 3-25-2022
 * Status: main class, finished
 * Dependencies: Environment, SimulationGUI
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

public final class Particles {
    
    //private constructor
    private Particles() {
        throw new AssertionError();
    }
    
    //main method
    public static void main( String[] args ) {
        Environment environment = new Environment();
        environment.getMainThread().start();
        SimulationGUI.createGUI( environment, "0.3.0" ).setVisible( true );
    }
}