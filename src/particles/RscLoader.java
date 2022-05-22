package particles;

/* Author: Kent F.
 * Description: class for loading system resources and external config items
 * Created: 5-15-2022
 * Status: singleton class, finished
 * Dependencies: Environment, SimulationGUI, Simulator
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public final class RscLoader extends ClassLoader {
    
    //constants
    public static final int PSOBJ_MAGIC = 0x5E65BEAD;
    public static final int CURRENT_PSOBJ_VERSION = 0x01-0000-00;
    public static final String PSOBJ_EXTENSION = ".psobj";
    
    
    //singleton holder
    private static final RscLoader RSCLOADER = new RscLoader();
    
    //manages the various filepaths of external script items
    private final MethodHandles.Lookup lookup;
    private final ArrayList<String> filePaths;
    private final HashMap<String,Class<?>> starterClasses;
    
    
    //private constructor for creating the instance
    private RscLoader() {
        if( RSCLOADER != null ) {
            throw new AssertionError();
        }
        lookup = MethodHandles.lookup();
        filePaths = new ArrayList<>();
        starterClasses = new HashMap<>();
    }
    
    
    //retrieves loader instance
    public static RscLoader rsc() {
        return RSCLOADER;
    }
    
    //loads a simulation engine
    public void loadSimulator( Environment env, String filePath ) throws IOException {
        Class<?> cls = loadExternal( filePath );
        if( !Simulator.class.isAssignableFrom( cls ) ) {
            throw new IllegalArgumentException();
        }
        env.setSimManager( Simulator.getSimulator( cls ) );
    }
    
    //loads an experiment
    public void loadExperiment( Environment env, SimulationGUI.CreationTemplate temp, String filePath ) throws Throwable {
        Class<?> cls = loadExternal( filePath );
        lookup.findStatic( cls, "experimentMain", MethodType.methodType( void.class,
                Environment.class, SimulationGUI.CreationTemplate.class ) ).invoke( env, temp );
    }
    
    //loads a generic script
    public void loadScript( Environment env, String filePath ) 
            throws IOException, NoSuchMethodException, IllegalAccessException {
        Class<?> cls = loadExternal( filePath );
        MethodHandle main = lookup.findStatic( cls, "main", MethodType.methodType( void.class, String[].class ) );
        Thread thread = new Thread( () -> {
            try {
                main.invoke( new String[0] );
            } catch( Throwable t ) { 
                throw new RuntimeException( t );
            }
        }, "Script-Main-" + cls.getName() );
        thread.setPriority( Thread.NORM_PRIORITY );
        thread.start();
    }
    
    //load an image from internal resources
    public BufferedImage loadImage( String name ) throws IOException {
        BufferedImage image;
        try( InputStream stream = super.getResourceAsStream( name ) ) {
            image = ImageIO.read( stream );
        }
        return image;
    }
    
    //read particles from a file
    public Particle[] readParticles( String fileName ) throws IOException {
        DataInputStream stream = new DataInputStream( new FileInputStream( fileName ) );
        while( stream.readInt() != PSOBJ_MAGIC ) { }
        switch( stream.readInt() ) {
            case CURRENT_PSOBJ_VERSION :
                int len = stream.readInt();
                Particle[] particles = new Particle[len];
                for( int i = 0; i < len; i++ ) {
                    Particle particle = new Particle();
                    particle.read( stream );
                    particles[i] = particle;
                }
                stream.close();
                return particles;
            default :
                stream.close();
                throw new IllegalArgumentException( "Invalid version" );
        }
    }
    
    //write particles to a file
    public void writeParticles( Particle[] particles, String fileName ) throws IOException {
        DataOutputStream stream = new DataOutputStream( new FileOutputStream( fileName ) );
        stream.writeInt( PSOBJ_MAGIC );
        stream.writeInt( CURRENT_PSOBJ_VERSION );
        stream.writeInt( particles.length );
        for( Particle particle : particles ) {
            particle.write( stream );
        }
    }
    
    
    //overridden find class method
    @Override
    protected Class<?> findClass( String name ) throws ClassNotFoundException {
        name = name.replace( '.', '/' );
        for( String path : filePaths ) {
            path += name;
            if( new File( path ).exists() ) {
                try {
                    FileInputStream stream = new FileInputStream( path );
                    byte[] bytes = new byte[ stream.available() ];
                    stream.read( bytes );
                    return super.defineClass( null, bytes, 0, bytes.length );
                } catch( IOException|ArrayIndexOutOfBoundsException|SecurityException e ) {
                    throw new ClassNotFoundException();
                }
            }
        }
        throw new ClassNotFoundException();
    }
    
    //private utility method for loading classes and recording its directory
    private Class<?> loadExternal( String path ) throws IOException {
        Class<?> load = starterClasses.get( path );
        if( load == null ) {
            byte[] bytes;
            try( FileInputStream stream = new FileInputStream( path ) ) {
                bytes = new byte[ stream.available() ];
                stream.read( bytes );
            }
            load = super.defineClass( null, bytes, 0, bytes.length );
            path = path.substring( 0, path.lastIndexOf( load.getName().replace( '.', '/' ) ) );
            filePaths.add( path );
        }
        return load;
    }
}
