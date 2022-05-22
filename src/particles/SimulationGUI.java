package particles;

/* Author: Kent F.
 * Description: gui and display frame manager
 * Created: 5-11-2022
 * Status: factory class, finished
 * Dependencies: Environment, Particle, RscLoader, Simulator
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public final class SimulationGUI extends Container implements LayoutManager,
        WindowListener, MouseListener, MouseMotionListener, MouseWheelListener {
    
    //default fields for experiment creation templates
    public static final CreationTemplate RING_FORMATION = new CreationTemplate( 
            300, new Color( 240, 240, 255 ), 0.2, 1.15, 0.5, 0.036, 0.65, 0, 5, 0 );
    public static final CreationTemplate BLACK_HOLE = new CreationTemplate( 
            300, new Color( 240, 240, 255, 120 ), 1, 5, 0.25, 0.036, 4, 0, 1.5, 0 );
    public static final CreationTemplate DIRECT_COLLISION = new CreationTemplate( 
            346, null, 2.4, 5, 0.75, 0.036, 4, -1, 0, 0 );
    public static final CreationTemplate PENETRATION_COLLISION = new CreationTemplate( 
            320, new Color( 255, 0, 0, 128 ), 2.4, 5, 0.75, 0.0036, 4, 6, 0, 0 );
    public static final CreationTemplate HIT_AND_RUN_COLLISION = new CreationTemplate( 
            520, null, 2.4, 5, 1.5, 0.04, 4, 4.3, 0, 0.002 );
    public static final CreationTemplate COSMOLOGICAL_SPONGE = new CreationTemplate( 
            600, new Color( 50, 50, 255, 160 ), 1, 1.15, 2, 0.01, 50, 0, 0, 0 );
    public static final CreationTemplate MOON_CREATING_COLLISION = new CreationTemplate( 
            400, new Color( 128, 128, 128, 128 ), 2.4, 5, 0.6, 0.04, 4, -1.8, 0, 0.03 );
    public static final CreationTemplate MANTLE_DIFFERENTIATION = new CreationTemplate( 
            300, null, 1, 5, 0.75, 0.036, 4, 0, 0, 0 );
    public static final CreationTemplate ANGULAR_MOMENTUM = new CreationTemplate( 
            300, new Color( 255, 255, 255, 50 ), 1, 5, 0.05, 0.036, 5, 0, 0, 0.01 );
    public static final CreationTemplate ACCRETION_DISK = new CreationTemplate( 
            300, new Color( 255, 0, 0, 75 ), 1, 1.15, 2, 0.1, 250, 0, 0, 0.00441 );
    public static final CreationTemplate PROTOPLANETARY_DISK = new CreationTemplate( 
            300, new Color( 255, 0, 0, 75 ), 1, 1.15, 2, 0.1, 250, 0, 0, 0 );
    
    
    //height of each editpane component
    private static final int COMP_HEIGHT = 20;
    private static final double CIRCLE = 2 * Math.PI;
    
    //fields to store components managed by the simulation gui
    private final Environment environment;
    private final TextField resolutionField;
    private final TextField massField;
    private final TextField radiusField;
    private final TextField springField;
    private final TextField dragField;
    private final TextField compactField;
    private final TextField xVelocityField;
    private final TextField yVelocityField;
    private final TextField angVelocityField;
    //fields for storing internal states
    private final CreationTemplate fieldStates;
    private MouseEvent lastDrag;
    private boolean reflectExp;
    
    //constructor
    private SimulationGUI( Environment env ) {
        environment = env;
        fieldStates = new CreationTemplate( 0, new Color( 255, 255, 255, 50 ), 0, 0, 0, 0, 0, 0, 0, 0 );
        lastDrag = null;
        reflectExp = false;
        super.add( new Label( "Object Creation Options" ) );
        Button colorSelection = new Button( "Choose Color" );
        colorSelection.setBackground( fieldStates.color );
        colorSelection.addActionListener( a -> {
            Color color = JColorChooser.showDialog( null, "Color Selector", fieldStates.color );
            if( color != null ) {
                fieldStates.color = color;
                colorSelection.setBackground( color );
            }
        } );
        super.add( colorSelection );
        resolutionField = addField( "Resolution", "100" );
        massField = addField( "Mass", "240" );
        radiusField = addField( "Radius", "50" );
        springField = addField( "Spring", "0.075" );
        dragField = addField( "Drag", "0.0036" );
        compactField = addField( "Compactness", "1" );
        xVelocityField = addField( "Velocity X-Component", "0" );
        yVelocityField = addField( "Velocity Y-Component", "0" );
        angVelocityField = addField( "Angular Velocity","0" );
    }
    
    
    //layout for the editpane gui
    @Override
    public void doLayout() {
        int w = super.getWidth();
        int len = super.getComponentCount();
        for( int i = 0; i < len; i++ ) {
            super.getComponent( i ).setBounds( 0, i * COMP_HEIGHT, w, COMP_HEIGHT );
        }
    }
    
    //layout for the editpane gui
    @Override
    public Dimension getMinimumSize() {
        return new Dimension( 0, COMP_HEIGHT * super.getComponentCount() );
    }
    
    //listener for clicking exit button
    @Override
    public void windowClosing( WindowEvent w ) {
        if( JOptionPane.showConfirmDialog( null, "Do you want to destroy changes?" ) == JOptionPane.OK_OPTION ) {
            System.exit( 0 );
        }
    }
    
    //listener for when the environment component is clicked
    @Override
    public void mouseClicked( MouseEvent m ) {
        try {
            updateFields();
            double zoom = environment.getZoom();
            double x = environment.getPosX() + ( m.getX() - environment.getWidth() / 2 ) / zoom;
            double y = environment.getPosY() + ( environment.getHeight() / 2 - m.getY() ) / zoom;
            environment.queueOperation( list -> addParticles( list, fieldStates, x, y ) );
            environment.requestFocus();
        } catch( NumberFormatException e ) {
            errorMessage( "Invalid options fields format", e );
        }
    }

    //listener for when mouse releases environment component
    @Override
    public void mouseReleased( MouseEvent m ) {
        lastDrag = null;
    }
    
    //listener for mouse drag events in environment component to move camera
    @Override
    public void mouseDragged( MouseEvent m ) {
        if( lastDrag != null ) {
            double zoom = environment.getZoom();
            environment.setPosX( environment.getPosX() - ( m.getX() - lastDrag.getX() ) / zoom );
            environment.setPosY( environment.getPosY() + ( m.getY() - lastDrag.getY() ) / zoom );
        }
        lastDrag = m;
    }

    //listener for mouse wheel events to in environment component to adjust the zoom of the camera
    @Override
    public void mouseWheelMoved( MouseWheelEvent m ) {
        double mult = Math.pow( 1.1, -m.getPreciseWheelRotation() * m.getScrollAmount() );
        double zoom = environment.getZoom();
        double posX = environment.getPosX();
        double posY = environment.getPosY();
        double x = posX - ( m.getX() - environment.getWidth() / 2 ) / zoom;
        double y = posY - ( environment.getHeight() / 2 - m.getY() ) / zoom;
        environment.setPosX( x + ( posX - x ) * mult );
        environment.setPosY( y + ( posY - y ) * mult );
        environment.setZoom( zoom * mult );
    }
    
    //layouts the components in the frame, not the simulation gui
    @Override
    public void layoutContainer( Container c ) {
        int minWidth = 200;
        int w = c.getWidth();
        int h = c.getHeight();
        Component c1 = c.getComponent( 0 );
        Component c2 = c.getComponent( 1 );
        if( c2.isVisible() ) {
            c2.setBounds( w -= minWidth, 0, minWidth, h );
        }
        c1.setBounds( 0, 0, w, h );
    }
    
    //implemented methods
    @Override
    public Dimension preferredLayoutSize( Container c ) {
        return new Dimension();
    }

    @Override
    public Dimension minimumLayoutSize( Container c ) { 
        return new Dimension();
    }
    
    @Override
    public void windowOpened( WindowEvent w ) { }
    @Override
    public void windowClosed( WindowEvent w ) { }
    @Override
    public void windowIconified( WindowEvent w ) { }
    @Override
    public void windowDeiconified( WindowEvent w ) { }
    @Override
    public void windowActivated( WindowEvent w ) { }
    @Override
    public void windowDeactivated( WindowEvent w ) { }
    @Override
    public void mousePressed( MouseEvent m ) { }
    @Override
    public void mouseEntered( MouseEvent m ) { }
    @Override
    public void mouseExited( MouseEvent m ) { }
    @Override
    public void mouseMoved( MouseEvent m ) { }
    @Override
    public void addLayoutComponent( String s, Component c ) { }
    @Override
    public void removeLayoutComponent( Component c ) { }
    
    
    //private utility methods
    //adds field to the options component
    private TextField addField( String fieldName, String defVal ) {
        TextField field = new TextField( defVal );
        super.add( new Label( fieldName ) );
        super.add( field );
        return field;
    }
    
    //updates default creation template with fields from the options component
    private void updateFields() {
        int res = Integer.parseInt( resolutionField.getText() );
        double resSq = Math.sqrt( res );
        fieldStates.count = res;
        fieldStates.mass = Double.parseDouble( massField.getText() ) / res;
        fieldStates.radius = Double.parseDouble( radiusField.getText() ) / resSq;
        fieldStates.distances = fieldStates.radius / Double.parseDouble( compactField.getText() );
        fieldStates.spring = Double.parseDouble( springField.getText() ) * resSq;
        fieldStates.drag = Double.parseDouble( dragField.getText() ) * resSq;
        fieldStates.xVelocity = Double.parseDouble( xVelocityField.getText() );
        fieldStates.yVelocity = Double.parseDouble( yVelocityField.getText() );
        fieldStates.angVelocity = Double.parseDouble( angVelocityField.getText() );
    }
    
    //invoke an experiment from the experiment menu
    private void applyExperiment( CreationTemplate temp, double timeStep, 
            BiConsumer<CreationTemplate,List<Particle>> toAct ) {
        if( reflectExp ) {
            try {
                updateFields();
            } catch( NumberFormatException e ) {
                errorMessage( "Invalid options fields format", e );
                return;
            }
            temp = fieldStates;
        } else {
            environment.setTimeStep( timeStep );
            environment.setTimePassed( 0 );
            environment.setZoom( 1 );
        }
        environment.setPosX( 0 );
        environment.setPosY( 0 );
        CreationTemplate toUse = temp;
        environment.queueOperation( list -> {
            list.clear();
            toAct.accept( toUse, list );
        } );
    }
    
    //display a error message with exception data
    private static void errorMessage( String message, Throwable t ) {
        JOptionPane.showMessageDialog( null, "Error: " + message + '\n' + t, "Error", JOptionPane.ERROR_MESSAGE );
    }
    
    //parses the response of a dialog message and checks its format and bounds
    private static double parseCheck( String msg, double defVal, double min, double max ) {
        String answer = JOptionPane.showInputDialog( msg, defVal );
        if( answer == null ) {
            return defVal;
        } else {
            double val = Double.parseDouble( answer );
            if( val < min || val > max ) {
                throw new IllegalArgumentException( "value must be in bounds of " + min + " and " + max );
            }
            return val;
        }
    }
    
    //updates the engine menu with the correct display
    private static void updateEngineMenu( Object obj ) {
        MenuItem item = (MenuItem)obj;
        Menu parent = (Menu)item.getParent();
        int len = parent.getItemCount();
        for( int i = 0; i < len; i++ ) {
            MenuItem current = parent.getItem( i );
            String label = current.getLabel();
            if( current == item ) {
                current.setLabel( 'X' + label.substring( 1 ) );
            } else if( label.charAt( 0 ) == 'X' ) {
                current.setLabel( '-' + label.substring( 1 ) );
            }
        }
    }
    
    //adds a planet with specific velocity, size, and modification parameters
    private static void addPlanet( List<Particle> toAdd, CreationTemplate temp, int count,
            double x, double y, double vx, double vy, BiConsumer<Particle,Double> actor ) {
        double currRadii = 0;
        double currAng = 0;
        double angDiv = CIRCLE + 0.1;
        vx += temp.xVelocity;
        vy += temp.yVelocity;
        for( int i = 0; i < count; i++ ) {
            double partX = Math.sin( currAng ) * currRadii;
            double partY = Math.cos( currAng ) * currRadii;
            Particle part = new Particle( temp.mass, temp.radius, temp.spring, temp.drag, temp.color, 
                    partX + x, partY + y, -partY * temp.angVelocity + vx, partX * temp.angVelocity + vy );
            toAdd.add( part );
            actor.accept( part, currRadii );
            if( ( currAng += angDiv ) > CIRCLE - angDiv ) {
                currRadii += temp.distances * 2;
                currAng = 0;
                angDiv = CIRCLE * temp.distances / ( currRadii * Math.PI );
            }
        }
    }
    
    
    //miscellaneous particle factory methods
    public static void addParticles( List<Particle> toAdd, CreationTemplate temp, double x, double y ) {
        addPlanet( toAdd, temp, temp.count, x, y, 0, 0, ( p, d ) -> {} );
    }
    
    public static void ringFormation( CreationTemplate temp, List<Particle> toAdd ) {
        double mainMass = temp.mass * temp.count * 50;
        double radius = temp.distances * Math.sqrt( temp.count ) * 4;
        addParticles( toAdd, temp, radius * 2.5, 0 );
        toAdd.add( new Particle( mainMass, radius, temp.spring, temp.drag, Color.YELLOW, 0, 0, 
                temp.xVelocity * -0.02, temp.yVelocity * -0.02 ) );
    }
    
    public static void blackHole( CreationTemplate temp, List<Particle> toAdd ) {
        double mainMass = temp.mass * temp.count * 2;
        double radius = temp.distances * Math.sqrt( temp.count ) * 0.1;
        addParticles( toAdd, temp, radius * 30, 0 );
        toAdd.add( new Particle( mainMass, radius, temp.spring, temp.drag, Color.DARK_GRAY, 0, 0, 
                temp.xVelocity * -0.5, temp.yVelocity * -0.5 ) );
    }
    
    public static void directCollision( CreationTemplate temp, List<Particle> toAdd ) {
        int indCount = temp.count / 2;
        double max = temp.distances * Math.sqrt( indCount );
        Color c1i = new Color( 255, 255, 0, 128 ), c1m = new Color( 255, 0, 0, 128 ), c1c = new Color( 144, 144, 144, 192 );
        addPlanet( toAdd, temp, indCount, max * 2, 0, 0, 0, 
                ( p, d ) -> p.setColor( d / max < 0.5 ? c1i : d / max < 0.95 ? c1m : c1c  ) );
        Color c2i = new Color( 128, 255, 0, 128 ), c2m = new Color( 128, 128, 0, 128 ), c2c = new Color( 112, 112, 112, 192 );
        addPlanet( toAdd, temp, indCount, -max * 2, 0, -2 * temp.xVelocity, -2 * temp.yVelocity, 
                ( p, d ) -> p.setColor(  d / max < 0.5 ? c2i : d / max < 0.95 ? c2m : c2c ) );
    }
    
    public static void penetrationCollision( CreationTemplate temp, List<Particle> toAdd ) {
        int side = (int)Math.sqrt( temp.count * 0.75 ) * 5 + 5;
        Color cloud = new Color( 255, 255, 255, 50 );
        for( int x = -side; x < side; x += 10 ) {
            for( int y = -side; y < side; y += 10 ) {
                toAdd.add( new Particle( 0.2, 5, 0.2, 0.001, cloud, x, y, 0, 0 ) );
            }
        }
        addPlanet( toAdd, temp, temp.count / 4, -side * 2, 0, 0, 0, ( p, d ) -> {} );
    }
    
    public static void hitAndRunCollision( CreationTemplate temp, List<Particle> toAdd ) {
        int indCount1 = temp.count * 2 / 3, indCount2 = temp.count / 3;
        double max1 = temp.distances * Math.sqrt( indCount1 ), max2 = temp.distances * Math.sqrt( indCount2 );
        Color c1i = new Color( 255, 255, 0, 128 ), c1m = new Color( 255, 0, 0, 128 ), c1c = new Color( 144, 144, 144, 192 );
        addPlanet( toAdd, temp, indCount1, 0, 0, -temp.xVelocity, 0, ( p, d ) -> {
            double rat = d / max1;
            double mass = p.getMass();
            if( rat < 0.5 ) {
                p.setColor( c1i );
                p.setMass( mass * 1.2 );
            } else if( rat < 0.95 ) {
                p.setColor( c1m );
            } else {
                p.setColor( c1c );
                p.setMass( mass * 0.8 );
            }
        } );
        Color c2i = new Color( 128, 255, 0, 128 ), c2m = new Color( 128, 128, 0, 128 ), c2c = new Color( 112, 112, 112, 192 );
        addPlanet( toAdd, temp, indCount2, -max1 * 3, max1 * 1.8, 0, 0, ( p, d ) -> {
            double rat = d / max2;
             double mass = p.getMass();
            if( rat < 0.5 ) {
                p.setColor( c2i );
                p.setMass( mass * 1.2 );
            } else if( rat < 0.95 ) {
                p.setColor( c2m );
            } else {
                p.setColor( c2c );
                p.setMass( mass * 0.8 );
            }
        } );
    }
    
    public static void cosmologicalSponge( CreationTemplate temp, List<Particle> toAdd ) {
        double darkSize = temp.distances / Math.sqrt( temp.count / 4 );
        Color darkColor = new Color( 255, 255, 255, 0 );
        for( double x = -temp.distances; x < temp.distances; x += darkSize ) {
            for( double y = -temp.distances; y < temp.distances; y += darkSize ) {
                toAdd.add( new Particle( temp.mass * 4, darkSize / 2, 0, 0, darkColor, x, y, 
                        temp.mass * 0.2 * x, temp.mass * 0.2 * y ) );
            }
        }
        for( int i = 0; i < temp.count * 3 / 4; i++ ) {
            double x = ( Math.random() * 2 - 1 ) * temp.distances;
            double y = ( Math.random() * 2 - 1 ) * temp.distances;
            toAdd.add( new Particle( temp.mass, temp.radius, temp.spring, temp.drag, temp.color, 
                    x, y, temp.mass * 0.2 * x, temp.mass * 0.2 * y ) );
        }
    }
    
    public static void moonCreatingCollision( CreationTemplate temp, List<Particle> toAdd ) {
        int indCount = temp.count * 2 / 3;
        double max = temp.distances * Math.sqrt( indCount );
        double vaddX = temp.xVelocity / 3;
        double vaddY = temp.yVelocity / 3;
        addPlanet( toAdd, temp, indCount, max * 3, max * 0.7, -vaddX, -vaddY, ( p, d ) -> {} );
        addPlanet( toAdd, temp, temp.count / 3, -max * 3, -max * 0.7, 
                -2 * temp.xVelocity - vaddX, -2 * temp.yVelocity - vaddY, ( p, d ) -> {} );
    }
    
    public static void mantleDifferentiation( CreationTemplate temp, List<Particle> toAdd ) {
        Color colors[] = { new Color( 128, 128, 128, 128 ), new Color( 255, 0, 0, 128 ), 
            new Color( 255, 255, 0, 128 ), new Color( 255, 255, 255, 128 ) };
        double weights[] = { 0.8, 1.6, 2.4, 3.2 };
        addPlanet( toAdd, temp, temp.count, 0, 0, 0, 0, 
            ( p, d ) -> {
                int num = (int)( Math.random() * 4 );
                p.setMass( p.getMass() * weights[num] );
                p.setColor( colors[num] );
            } );
    }
    
    public static void angularMomentum( CreationTemplate temp, List<Particle> toAdd ) {
        addParticles( toAdd, temp, 0, 0 );
    }
    
    public static void accretionDisk( CreationTemplate temp, List<Particle> toAdd ) {
        for( int i = 0; i < temp.count; i++ ) {
            double angle = Math.random() * CIRCLE;
            double dist = Math.random();
            double posMult = temp.distances * dist;
            double velMult = temp.angVelocity * posMult;
            double x = Math.sin( angle );
            double y = Math.cos( angle );
            toAdd.add( new Particle( temp.mass, temp.radius, temp.spring, temp.drag, temp.color, 
                    x * posMult, y * posMult, y * velMult, -x * velMult ) );
        }
    }
    
    public static void protoplanetaryDisk( CreationTemplate temp, List<Particle> toAdd ) {
        double mainMass = temp.mass * temp.count * 10;
        toAdd.add( new Particle( mainMass, temp.distances * 0.03, temp.spring, temp.drag, 
                new Color( temp.color.getRed(), temp.color.getBlue(), temp.color.getGreen(), 255 ), 0, 0, 0, 0 ) );
        for( int i = 0; i < temp.count; i++ ) {
            double angle = Math.random() * CIRCLE;
            double dist = Math.random() * 0.8 + 0.2;
            double posMult = temp.distances * dist;
            double velMult = Math.sqrt( mainMass / posMult );
            double x = Math.sin( angle );
            double y = Math.cos( angle );
            toAdd.add( new Particle( temp.mass, temp.radius, temp.spring, temp.drag, temp.color, 
                    x * posMult, y * posMult, y * velMult, -x * velMult ) );
        }
    }
    
    
    //gui factory that returns a frame
    public static JFrame createGUI( Environment env, String version ) {
        //initialize variables
        JFrame frame = new JFrame( "Particle Simulator - v" + version );
        SimulationGUI gui = new SimulationGUI( env );
        Dimension bounds = Toolkit.getDefaultToolkit().getScreenSize();
        ScrollPane pane = new ScrollPane();
        //add components to each other
        pane.add( gui );
        env.addMouseListener( gui );
        env.addMouseMotionListener( gui );
        env.addMouseMotionListener( env );
        env.addMouseWheelListener( gui );
        frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        frame.setBounds( bounds.width /= 10, bounds.height /= 10, bounds.width * 7, bounds.height * 7 );
        frame.setBackground( Color.DARK_GRAY );
	frame.add( env );
        frame.add( pane );
        frame.setLayout( gui );
        frame.addWindowListener( gui );
        
        //set frame icon image
        try {
            frame.setIconImage( RscLoader.rsc().loadImage( "particles/icon.png" ) );
        } catch( IOException|NullPointerException|IllegalArgumentException|SecurityException e ) { }
        
        //initiate file dialog
        FileDialog selector = new FileDialog( frame, "Select File" );
        selector.setFilenameFilter( ( file, name ) -> {
            int i = name.lastIndexOf( '.' );
            return i == -1 ? false : name.substring( i + 1 ).toLowerCase().equals( "psobj" );
        } );
        
        //initiate title and label string array for menu bar
        String[] menuNames = { "File", "Simulation", "View", "Simulators", "Experiments", "About" };
        String[][] itemNames = { { "New", "Open", "Save", "Import", "- Reflect Experiments", "Scripts", "Exit" },
                { "Start", "Stop", "Step", "Time Step", "Tick Length", "Reset Simulation Counter" },
                { "Zoom In", "Zoom Out", "Default Zoom", "X Edit Pane", "Frame Length" },
                { "X Simple", "- Anti-Singularity", "- Tree Optimizer", "- Multi-Thread", "- Other..." },
                { "Ring Formation", "Black Hole", "Direct Collision", "Penetration Collision", "Hit and Run Collision", 
                "Cosmological Sponge", "Moon-Creating Collision", "Mantle Differentiation", "Angular Momentum", 
                "Accretion Disk", "Protoplanetary Disk", "Other..." },
                { "Information", "Help", "GitHub" } };
        //listeners for menu bar
        ActionListener[][] listeners = { { 
                a -> env.queueOperation( list -> list.clear() ), 
                a -> {
                    selector.setMode( FileDialog.LOAD );
                    selector.setVisible( true );
                    String fileName = selector.getFile();
                    if( fileName != null ) {
                        try {
                            Particle[] particles = RscLoader.rsc().readParticles( fileName );
                            env.queueOperation( list -> {
                                list.clear();
                                list.addAll( Arrays.asList( particles ) );
                            } );
                        } catch( IOException|IllegalArgumentException e ) {
                            errorMessage( "Failed to open file", e );
                        }
                    }
                }, 
                a -> env.queueOperation( list -> {
                    Particle[] particles = list.toArray( new Particle[ list.size() ] );
                    EventQueue.invokeLater( () -> {
                        selector.setMode( FileDialog.SAVE );
                        selector.setVisible( true );
                        String fileName = selector.getFile();
                        if( fileName != null ) {
                            if( !fileName.endsWith( RscLoader.PSOBJ_EXTENSION ) ) {
                                fileName += RscLoader.PSOBJ_EXTENSION;
                            }
                            try {
                                RscLoader.rsc().writeParticles( particles, fileName );
                            } catch( IOException e ) {
                                errorMessage( "Failed to save file", e );
                            }
                        }
                    } );
                } ), 
                a -> {
                    selector.setMode( FileDialog.LOAD );
                    selector.setVisible( true );
                    String fileName = selector.getFile();
                    if( fileName != null ) {
                        try {
                            Particle[] particles = RscLoader.rsc().readParticles( fileName );
                            env.queueOperation( list -> list.addAll( Arrays.asList( particles ) ) );
                        } catch( IOException|IllegalArgumentException e ) {
                            errorMessage( "Failed to import file", e );
                        }
                    }
                },
                a -> {
                    gui.reflectExp = !gui.reflectExp;
                    MenuItem item = (MenuItem)a.getSource();
                    item.setLabel( ( gui.reflectExp ? 'X' : '-' ) + item.getLabel().substring( 1 ) );
                },
                a -> {
                    selector.setMode( FileDialog.LOAD );
                    selector.setVisible( true );
                    String fileName = selector.getFile();
                    if( fileName != null ) {
                        try {
                            RscLoader.rsc().loadScript( env, version );
                        } catch( IOException|IllegalAccessException|NoSuchMethodException|RuntimeException|Error e ) {
                            errorMessage( "Failed to load simulation script", e );
                        }
                    }
                },
                a -> frame.dispose()
            }, { 
                a -> env.setActive( true ), 
                a -> env.setActive( false ), 
                a -> {
                    if( !env.getActive() ) {
                        env.setActive( true );
                        env.queueOperation( list -> env.setActive( false ) );
                    }
                }, 
                a -> {
                    try {
                        env.setTimeStep( parseCheck( "Time Step", env.getTimeStep(), 0, Double.POSITIVE_INFINITY ) );
                    } catch( IllegalArgumentException e ) {
                        errorMessage( "Invalid text format", e );
                    }
                }, 
                a -> {
                    try {
                        env.setTickLength( (int)parseCheck( "Tick Length", env.getTickLength(), -1.5, 1000.5 ) );
                    } catch( IllegalArgumentException e ) {
                        errorMessage( "Invalid text format", e );
                    }
                }, 
                a -> env.setTimePassed( 0 )
            }, { 
                a -> env.setZoom( env.getZoom() * 1.1 ), 
                a -> env.setZoom( env.getZoom() * 0.9 ), 
                a -> env.setZoom( 1 ), 
                a -> {
                    boolean curr = !pane.isVisible();
                    pane.setVisible( curr );
                    MenuItem item = (MenuItem)a.getSource();
                    item.setLabel( ( curr ? 'X' : '-' ) + item.getLabel().substring( 1 ) );
                    frame.revalidate();
                }, 
                a -> {
                    try {
                        env.setFrameLength( (int)parseCheck( "Frame Length", env.getFrameLength(), 0, 1000.5 ) );
                    } catch( IllegalArgumentException e ) {
                        errorMessage( "Invalid text format", e );
                    }
                }
            }, {
                a -> {
                    env.setSimManager( Simulator.DEFAULT );
                    updateEngineMenu( a.getSource() );
                }, 
                a -> {
                    try {
                        double acc = 1 - parseCheck( "Accuracy Threshold", 1 - Math.sqrt( env.getRatioThresh() ), 0, 0.99999 );
                        env.setRatioThresh( acc * acc );
                        env.setSimManager( Simulator.ANTI_SINGLE );
                        updateEngineMenu( a.getSource() );
                    } catch( NullPointerException|NumberFormatException e ) {
                        errorMessage( "Invalid number format", e );
                    }
                }, 
                a -> {
                    errorMessage( "Simulation engine not available", new NullPointerException() );//-------------------
                    //env.setSimManager( Simulator.TREE_OPTIMIZER );
                    //updateEngineMenu( a.getSource() );
                }, 
                a -> {
                    env.setSimManager( Simulator.MULTI_THREAD );
                    updateEngineMenu( a.getSource() );
                },
                a -> {
                    selector.setMode( FileDialog.LOAD );
                    selector.setVisible( true );
                    String fileName = selector.getFile();
                    if( fileName != null ) {
                        try {
                            RscLoader.rsc().loadSimulator( env, fileName );
                            updateEngineMenu( a.getSource() );
                        } catch( IOException|RuntimeException|Error e ) {
                            errorMessage( "Failed to load simulation engine", e );
                        }
                    }
                }
            }, { 
                a -> gui.applyExperiment( RING_FORMATION, 0.05, SimulationGUI::ringFormation ), 
                a -> gui.applyExperiment( BLACK_HOLE, 1, SimulationGUI::blackHole ), 
                a -> gui.applyExperiment( DIRECT_COLLISION, 1, SimulationGUI::directCollision ), 
                a -> gui.applyExperiment( PENETRATION_COLLISION, 0.1, SimulationGUI::penetrationCollision ), 
                a -> gui.applyExperiment( HIT_AND_RUN_COLLISION, 1, SimulationGUI::hitAndRunCollision ), 
                a -> gui.applyExperiment( COSMOLOGICAL_SPONGE, 0.05, SimulationGUI::cosmologicalSponge ), 
                a -> gui.applyExperiment( MOON_CREATING_COLLISION, 0.3, SimulationGUI::moonCreatingCollision ), 
                a -> gui.applyExperiment( MANTLE_DIFFERENTIATION, 0.3, SimulationGUI::mantleDifferentiation ), 
                a -> gui.applyExperiment( ANGULAR_MOMENTUM, 1, SimulationGUI::angularMomentum ),
                a -> gui.applyExperiment( ACCRETION_DISK, 0.05, SimulationGUI::accretionDisk ),
                a -> gui.applyExperiment( PROTOPLANETARY_DISK, 0.05, SimulationGUI::protoplanetaryDisk ),
                a -> {
                    selector.setMode( FileDialog.LOAD );
                    selector.setVisible( true );
                    String fileName = selector.getFile();
                    if( fileName != null ) {
                        try {
                            gui.updateFields();
                            env.setPosX( 0 );
                            env.setPosY( 0 );
                            RscLoader.rsc().loadExperiment( env, gui.fieldStates, fileName );
                            updateEngineMenu( a.getSource() );
                        } catch( Throwable t ) {
                            errorMessage( "Failed to load simulation experiment", t );
                        }
                    }
                }
            }, { 
                a -> JOptionPane.showMessageDialog( null, 
                        "Program: Particle Simulator\n" + 
                        "Author:  Kent Fukuda\n" + 
                        "Version: " + version + "\n" +
                        "Created: 3-25-2021\n" +
                        "License: GNU Affero General Public License", 
                        "Information", JOptionPane.INFORMATION_MESSAGE ), 
                a -> JOptionPane.showMessageDialog( null, "work in progress" ), 
                a -> {
                    String url = "https://github.com/klark888/space-simulator";
                    try {
                        Desktop.getDesktop().browse( new URI( url ) );
                    } catch( UnsupportedOperationException|IOException|URISyntaxException e ) {
                        errorMessage( "Could not open browser. Please copy link:\n" + url, e );
                    }
                }
        } };
        
        //create menu bar
        MenuBar bar = new MenuBar();
        for( int i = 0; i < menuNames.length; i++ ) {
            Menu menu = new Menu( menuNames[i] );
            for( int j = 0; j < itemNames[i].length; j++ ) {
                MenuItem item = new MenuItem( itemNames[i][j] );
                item.addActionListener( listeners[i][j] );
                menu.add( item ); 
            }
            bar.add( menu );
        }
        frame.setMenuBar( bar );
        
        return frame;
    }
    
    
    public static final class CreationTemplate implements Cloneable {
        
        private Color color;
        private int count;
        private double mass;
        private double radius;
        private double distances;
        private double spring;
        private double drag;
        private double xVelocity;
        private double yVelocity;
        private double angVelocity;
        
        
        public CreationTemplate( int count, Color color, double mass, double radius, 
                double spring, double drag, double distances,
                double xVelocity, double yVelocity, double angVelocity ) {
            this.color = color;
            this.count = count;
            this.mass = mass;
            this.radius = radius;
            this.spring = spring;
            this.drag = drag;
            this.distances = distances;
            this.xVelocity = xVelocity;
            this.yVelocity = yVelocity;
            this.angVelocity = angVelocity;
        }
    }
}