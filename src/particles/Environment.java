package particles;

/* Author: Kent F.
 * Description: class for holding and rendering simulation states of particles
 * Created: 3-25-2022
 * Status: generic class, finished
 * Dependencies: Particle, Simulator
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class Environment extends Component implements Runnable, MouseMotionListener {
    
    //serial version uid
    private static final long serialVersionUID = -1205743849784754339L;

    //lists of spaceobjects in the simulation
    final List<Particle> particles;
    //queue of external operations queued to the spaceObjects list
    private final List<Consumer<List<Particle>>> operationQueue;
    //main thread of the environment
    private final Thread mainThread;
    //simulation manager for optimizations
    private Simulator simulator;

    //environment variables
    private double posX;//camera x coordinate
    private double posY;//camer y coordinate
    private double zoom;//scale modifier
    private double timePassed;//indicates days passed in simulation
    private double timeStep;//indicates the time passed per tick of simulation
    private double ratioThresh;//required accuracy threshold modifier for when using the antisingularity simulator
    //mouseevent storage
    private MouseEvent lastPos;
    //status checkers
    private long tickLength;//minumum length of each tick
    private long frameLength;//minimum length of each frame repaint
    private boolean simActive;//if simulation is active
    
    //constructor
    public Environment() {
        //initializes final fields
        particles = new ArrayList<>();
        operationQueue = Collections.synchronizedList( new ArrayList<>() );
        mainThread = new Thread( this, "Simulation-Main" );

        //initializes other fields
        simulator = Simulator.DEFAULT;
        posX = 0;
        posY = 0;
        zoom = 1;
        timePassed = 0;
        timeStep = 1;
        ratioThresh = 0.49;
        lastPos = new MouseEvent( this, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 0, 0, 0, false );
        tickLength = 16;
        frameLength = 16;
        simActive = false;

        super.setFocusable( true );
        mainThread.setPriority( Thread.MAX_PRIORITY );
        mainThread.setDaemon( true );
        Environment env = this;
        simulator.environment = env;
    }

    //mutator methods
    public void setSimManager( Simulator sim ) {
        operationQueue.add( list -> {
            simulator.disposeEnv();
            simulator.environment = null;
            sim.environment = this;
            sim.acceptEnv();
            simulator = sim;
        } );
    }
    
    public void setActive( boolean simActive ) {
        this.simActive = simActive;
    }

    public void setTimePassed( double timePassed ) {
        operationQueue.add( list -> this.timePassed = timePassed );
    }

    public void setTimeStep( double timeStep ) {
        operationQueue.add( list -> this.timeStep = timeStep );
    }

    public void setRatioThresh( double ratioThresh ) {
        this.ratioThresh = ratioThresh;
    }
    
    public void setTickLength( long tickLength ) {
        this.tickLength = tickLength;
    }

    public void setFrameLength( long frameLength ) {
        this.frameLength = frameLength;
    }

    public void setPosX( double posX ) {
        this.posX = posX;
    }

    public void setPosY( double posY ) {
        this.posY = posY;
    }
    
    public void setZoom( double zoom ) {
        this.zoom = zoom;
    }
    

    //accessor methods
    public Thread getMainThread() {
        return mainThread;
    }
    
    public Simulator getSimulator() {
        return simulator;
    }

    public boolean getActive() {
        return simActive;
    }
    
    public double getTimePassed() {
        return timePassed;
    }
    
    public double getTimeStep() {
        return timeStep;
    }

    public double getRatioThresh() {
        return ratioThresh;
    }
    
    public long getTickLength() {
        return tickLength;
    }

    public long getFrameLength() {
        return frameLength;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getZoom() {
        return zoom;
    }

    //queues an operation to spaceObject list
    public void queueOperation( Consumer<List<Particle>> operation ) {
        operationQueue.add( operation );
    }

    
    //overridden methods
    @Override
    public void mouseDragged( MouseEvent e ) {
        lastPos = e;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        lastPos = e;
    }
    
    //renders simulation as image
    @Override
    public void paint( Graphics g ) {
        //paints particle objects
        g.setColor( Color.BLACK );
        g.fillRect( 0, 0, super.getWidth(), super.getHeight() );
        synchronized( particles ) {
            particles.forEach( obj -> {
                if( obj != null ) {
                    g.setColor( obj.getColor() );
                    double radius = obj.getRadius();
                    int s = Math.max( (int)( radius * 2 * zoom ), 2 );
                    g.fillOval( (int)( ( obj.getXPosition() - radius - posX ) * zoom ) + super.getWidth() / 2, 
                            (int)( ( posY - obj.getYPosition() - radius ) * zoom ) + super.getHeight() / 2, s, s );
                }
            } );
        }
        //paints environment status
        g.setColor( Color.WHITE );
        g.drawString( "Coordinates: ( " + ( posX + ( lastPos.getX() - super.getWidth() / 2 ) / zoom ) + ", " + 
                ( posY - ( lastPos.getY() - super.getHeight() / 2 ) / zoom ) +
                ") --- Simulation Time: " + timePassed +
                " --- Zoom Magnitude: " + (int)( zoom * 100 ) + "%", 0, 10 );
    }

    //implemented method for main simulation thread to run
    @Override
    public void run() {
        long repaintTime = 0;
        long simTime = 0;
        long currentTime;
        while( true ) {
            currentTime = System.currentTimeMillis();
            //repaints simulation and consumes queued operations
            if( currentTime > repaintTime + frameLength ) {
                super.repaint();
                if( !operationQueue.isEmpty() ) {
                    synchronized( particles ) {
                        do {
                            operationQueue.remove( 0 ).accept( particles );
                        } while( !operationQueue.isEmpty() );
                    }
                    simulator.acceptEnv();
                }
                repaintTime = currentTime;
            }
            //simulates a single tick of the simulation
            if( simActive && currentTime > simTime + tickLength ) {
                simulator.simulate( timeStep );
                timePassed += timeStep;
                simTime = currentTime;
            }
        }
    }
}
