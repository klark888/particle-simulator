package particles;

/* Author: Kent F.
 * Description: class for implementing physics engines for the simulation
 * Created: 5-18-2022
 * Status: singleton class, wip
 * Dependencies: Environment, Particle
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;

public abstract class Simulator {
    
    //static variables for holding information about all simulators
    private static final HashMap<Class<?>,Simulator> SIMULATORS = new HashMap<>();//stores all instances of simulators
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();//lookup for calling constructors
    
    //implemention with the most basic simulation system
    public static final Simulator DEFAULT = new Simulator() {
        @Override
        protected void simulate( double timeStep ) {
            List<Particle> particles = environment.particles;
            int size = particles.size();
            for( int i = 0; i < size; i++ ) {
                Particle obj = particles.get( i );
                for( int j = i + 1; j < size; j++ ) {
                    obj.interact( particles.get( j ) );
                }
                obj.update( timeStep );
            }
        }
    };
    
    //implementation which prevents singularities from small distances between objects from occuring
    public static final Simulator ANTI_SINGLE = new Simulator() {
        
        @Override
        protected void simulate( double timeStep ) {
            List<Particle> particles = environment.particles;
            int size = particles.size();
            double threshHold = environment.getRatioThresh();
            double localTime = timeStep;
            while( localTime > 0 ) {
                double maxStepSq = localTime * localTime;
                for( int i = 0; i < size; i++ ) {
                    Particle p1 = particles.get( i );
                    for( int j = i + 1; j < size; j++ ) {
                        Particle p2 = particles.get( j );
                        double stepSq = threshHold * p1.interact( p2 ) / p1.velocDiff( p2 );
                        if( maxStepSq > stepSq ) {
                            maxStepSq = stepSq;
                        }
                    }
                }
                double maxStep = Math.sqrt( maxStepSq );
                for( int i = 0; i < size; i++ ) {
                    Particle p = particles.get( i );
                    p.update( maxStep );
                }
                localTime -= maxStep;
            }
        }
    };
    
    //implementation that uses segments simulation into trees to reduce computation times
    public static final Simulator TREE_OPTIMIZER = new Simulator() {
        
        //private Particle[] vertical, horizontal, summary = null;
        
        @Override
        protected void simulate( double timeStep ) { }
        
        /*@Override
        protected void acceptEnv() {
            List<Particle> particles = environment.particles;
            int size = particles.size();
            vertical = particles.toArray( new Particle[ size ] );
            Arrays.sort( vertical, ( p1, p2 ) -> p1.getXPosition() < p2.getXPosition() ? -1 : 1 );
            horizontal = vertical.clone();
            Arrays.sort( horizontal, ( p1, p2 ) -> p1.getYPosition() < p2.getYPosition() ? -1 : 1 );
            double segsExact = Math.sqrt( Math.sqrt( size + 0.1 ) ) + 0.5;
            int numSegs = (int)segsExact;
            int segSize = (int)( size / segsExact );
            summary = new Particle[ numSegs * numSegs ];
        }*/
    };
    
    //implementation that uses multiple threads
    public static final Simulator MULTI_THREAD = new Simulator() {
        
        private final Object workLock = new Object();
        private final int threadCount = Runtime.getRuntime().availableProcessors();
        private volatile int interactors = 0;
        private volatile int updators = 0;
        private volatile int numActive = 1;
        
        
        @Override
        protected void simulate( double timeStep ) {
            if( numActive < threadCount ) {
                for( int i = numActive; i < threadCount; i++ ) {
                    Thread thread = new Thread( () -> {
                        synchronized( workLock ) {
                            numActive++;
                        }
                        while( environment.getActive() ) {
                            if( interactors > 0 ) {
                                workInteract();
                            }
                            if( updators > 0 ) {
                                workSimulate( environment.getTimeStep() );
                            }
                        }
                        synchronized( workLock ) {
                            numActive--;
                        }
                    }, "Simulation-Worker-" + i );
                    thread.setDaemon( true );
                    thread.setPriority( Thread.NORM_PRIORITY );
                    thread.start();
                }
                while( numActive < threadCount ) {  }
            }
            int size = environment.particles.size();
            interactors = size * size / 4;
            workInteract();
            updators = size;
            workSimulate( timeStep );
        }
        
        @Override
        protected void disposeEnv() {
            boolean active = environment.getActive();
            environment.setActive( false );
            while( numActive > 1 ) { }
            environment.setActive( active );
        }
        
        private void workInteract() {
            int size = environment.particles.size();
            int takeTime = size / threadCount / 2;
            while( true ) {
                int low, high;
                synchronized( workLock ) {
                    high = interactors;
                    if( high == 0 ) {
                        return;
                    }
                    low = high - takeTime;
                    low = low < 0 ? 0 : low;
                    interactors = low;
                }
                for( int i = low; i < high; i++ ) {
                    int id = i;
                    int width = (int)( Math.sqrt( id++ + 0.75 ) + 0.5 );
                    int x = width * width - id;
                    int y = width - 1;
                    y = id - y * y;
                    if( x < 0 ) {
                        x += width;
                        y += 1 - width;
                    }
                    interact( x, y );
                    if( id <= size * size / 4 - size / 2 ) {
                        interact( size - y - 1, size - x - 1 );
                    }
                }
            }
        }
        
        private void workSimulate( double timeStep ) {
            List<Particle> particles = environment.particles;
            while( true ) {
                int id;
                synchronized( workLock ) {
                    id = updators - 1;
                    if( id == -1 ) {
                        return;
                    }
                    updators = id;
                }
                Particle particle = particles.get( id );
                particle.update( timeStep );
            }
        }
        
        private void interact( int x, int y ) {
            List<Particle> particles = environment.particles;
            Particle p1 = particles.get( x );
            Particle p2 = particles.get( y );
            //double synchronization is ok because x < y always
            synchronized( p1 ) {
                synchronized( p2 ) {
                    p1.interact( p2 );
                }
            }
        }
    };
    
    
    //stores environment class to simulate on
    protected Environment environment;
    
    
    //protected constructor - can only use once per class
    protected Simulator() {
        Class<?> thisClass = getClass();
        if( SIMULATORS.get( thisClass ) != null ) {
            throw new IllegalStateException();
        }
        environment = null;
        Simulator self = this;
        SIMULATORS.put( thisClass, self );
    }
    
    
    //methods for simulation to implement
    protected abstract void simulate( double timeStep );//simulate environment
    protected void acceptEnv() { }//accept a new environment instance
    protected void disposeEnv() { }//dispose a environment
    
    
    //returns the simulator instance for a given class
    public static final Simulator getSimulator( Class<?> cls ) {
        Simulator sim = SIMULATORS.get( cls );
        if( sim == null ) {
            try {
                sim = (Simulator)LOOKUP.findConstructor( cls, MethodType.methodType( void.class ) ).invoke();
            } catch( Throwable t ) {
                throw new IllegalArgumentException( t );
            }
        }
        return sim;
    }
}