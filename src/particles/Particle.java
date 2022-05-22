package particles;

/* Author: Kent F.
 * Description: class for holding the coordinates and state for a single simulation particle
 * Created: 3-27-2022
 * Status: entity class, finished
 * Dependencies: none
 * Licensed under GNU v3, see src/particles/Particles.java for more details
 */

import java.awt.Color;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public final class Particle implements Cloneable, Externalizable {
    
    //serial version uid
    private static final long serialVersionUID = 5348502945034859632L;
    
    
    //private fields for holding data abount the particle
    private double mass, radius, invSpring, drag;
    private Color color;
    private double xPosition, yPosition, xVelocity, yVelocity;
    private transient double xAccel, yAccel;
    
    
    //constructor  0.00066
    public Particle() {
        this( 1, 1, 1, 1, Color.BLACK, 0, 0, 0, 0 );
    }
    
    //constructor
    public Particle( double mass, double radius, double spring, double drag, Color color ) {
        this( mass, radius, spring, drag, color, 0, 0, 0, 0 );
    }
    
    //constructor
    public Particle( double mass, double radius, double spring, double drag, Color color, 
            double xPosition, double yPosition, double xVelocity, double yVelocity ) {
        this.mass = mass;
        this.radius = radius;
        this.invSpring = 1 / spring;
        this.drag = drag;
        this.color = color;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }
    
    
    //calculates interaction between two particles and returns the distance squared
    public double interact( Particle p ) {
        double xDiff = xPosition - p.xPosition;
	double yDiff = yPosition - p.yPosition;
        double distSq = xDiff * xDiff + yDiff * yDiff;
        double dist = Math.sqrt( distSq );
        double totRad = radius + p.radius;
        double force, forceX, forceY;
        //test for contact between particles
        if( dist <= totRad ) {
            //linear restoring spring force and decreasign gravity
            force = ( totRad / dist - 1 ) / ( mass * p.mass * ( invSpring + p.invSpring ) ) - 1 / ( totRad * totRad * totRad );
            /*implemented simple drag calculations - drag calculation which takes into account contact area and rVelocity
            is too computationally heavy, so it was not added to this simulation. this simpler implementation is slightly
            less accurate, which results in the simulation system losing any angular velocity due to this drag calcuation*/
            double totDrag = drag * p.drag;
            forceX = ( force * xDiff + totDrag * ( p.xVelocity - xVelocity ) );
            forceY = ( force * yDiff + totDrag * ( p.yVelocity - yVelocity ) );
        } else {
            //normal gravity calculations
            force = -1 / ( distSq * dist );
            forceX = force * xDiff;
            forceY = force * yDiff;
        }
        xAccel += forceX * p.mass;
        yAccel += forceY * p.mass;
        p.xAccel -= forceX * mass;
        p.yAccel -= forceY * mass;
        return dist;
    }
    
    //returns the difference in velocity
    public double velocDiff( Particle p ) {
        double xvDiff = p.xVelocity - xVelocity;
        double yvDiff = p.yVelocity - yVelocity;
        return xvDiff * xvDiff + yvDiff * yvDiff;
    }
    
    //simulations the movement of the particle without resetting delta t variables
    public void update( double timeStep ) {
        xPosition += ( xVelocity += xAccel * timeStep ) * timeStep;
        yPosition += ( yVelocity += yAccel * timeStep ) * timeStep;
        xAccel = yAccel = 0;
    }
    
    //serializes particle
    public void write( DataOutput out ) throws IOException {
        out.writeDouble( mass );
        out.writeDouble( radius );
        out.writeDouble( invSpring );
        out.writeDouble( drag );
        out.writeInt( color.getRGB() );
        out.writeDouble( xPosition );
        out.writeDouble( yPosition );
        out.writeDouble( xVelocity );
        out.writeDouble( yVelocity );
    }
    
    //deserialized particle
    public void read( DataInput in ) throws IOException {
        mass = in.readDouble();
        radius = in.readDouble();
        invSpring = in.readDouble();
        drag = in.readDouble();
        color = new Color( in.readInt(), true );
        xPosition = in.readDouble();
        yPosition = in.readDouble();
        xVelocity = in.readDouble();
        yVelocity = in.readDouble();
    }
    
    
    //mutator methods
    public void setMass( double mass ) {
        this.mass = mass;
    }
    
    public void setRadius( double radius ) {
        this.radius = radius;
    }
    
    public void setSpring( double spring ) {
        this.invSpring = 1 / spring;
    }
    
    public void setDrag( double drag ) {
        this.drag = drag;
    }

    public void setColor( Color color ) {
        this.color = color;
    }
    
    public void setXPosition( double xPosition ) {
        this.xPosition = xPosition;
    }

    public void setYPosition( double yPosition ) {
        this.yPosition = yPosition;
    }

    public void setXVelocity( double xVelocity ) {
        this.xVelocity = xVelocity;
    }

    public void setYVelocity( double yVelocity ) {
        this.yVelocity = yVelocity;
    }

    //accessor methods
    public double getMass() {
        return mass;
    }
    
    public double getRadius() {
        return radius;
    }

    public double getSpring() {
        return invSpring;
    }
    
    public double getDrag() {
        return drag;
    }

    public Color getColor() {
        return color;
    }
    
    public double getXPosition() {
        return xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

    public double getXVelocity() {
        return xVelocity;
    }

    public double getYVelocity() {
        return yVelocity;
    }
    
    
    //overridden externalizable, cloneable, and object methods
    @Override
    public final String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append( super.toString() ).append( '[' );
        bldr.append( "mass=" ).append( mass ).append( ',' );
        bldr.append( "radius=" ).append( radius ).append( ',' );
        bldr.append( "spring=" ).append( 1 / invSpring ).append( ',' );
        bldr.append( "drag=" ).append( drag ).append( ',' );
        bldr.append( "color=" ).append( color.getRGB() ).append( ',' );
        bldr.append( "xPosition=" ).append( xPosition ).append( ',' );
        bldr.append( "yPosition=" ).append( yPosition ).append( ',' );
        bldr.append( "xVelocity=" ).append( xVelocity ).append( ',' );
        bldr.append( "yVelocity=" ).append( yVelocity ).append( ']' );
        return bldr.toString();
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch( CloneNotSupportedException e ) { 
            throw new UnsupportedOperationException( e );
        }
    }
    
    @Override
    public void writeExternal( ObjectOutput out ) throws IOException {
        write( out );
    }

    @Override
    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
        read( in );
    }
}
/*
    public static final double VOLCIRCLE = 4 * Math.PI / 3 ;
    public static final double GRAVITY = -1 *         0.6;
    public static final double SPRING = VOLCIRCLE *   3;
    public static final double DRAG = VOLCIRCLE / 2 * 0.0005;
        double xDiff = xPosition - other.xPosition;
	double yDiff = yPosition - other.yPosition;
        double distSq = xDiff * xDiff + yDiff * yDiff;
        double dist = Math.sqrt( distSq );
        double force, forceX, forceY;
        double totRad = radius + other.radius;
        if( dist <= totRad ) {
            double density = ( mass + other.mass ) / ( mass * other.mass * ( radi3 + other.radi3 ) );
            force = GRAVITY / ( totRad * totRad * totRad ) + SPRING * density * ( totRad / dist - 1 );
            forceX = force * xDiff + DRAG * ( other.xVelocity - xVelocity );
            forceY = force * yDiff + DRAG * ( other.yVelocity - yVelocity );
        } else {
            force = GRAVITY / ( distSq * dist );
            forceX = force * xDiff;
            forceY = force * yDiff;
        }
        other.xVelocity -= forceX * mass;
        other.yVelocity -= forceY * mass;
        xVelocity += forceX * other.mass;
        yVelocity += forceY * other.mass;*//*
    public void interact( double timeStep, Particle other ) {
        double xDiff = xPosition - other.xPosition;
	double yDiff = yPosition - other.yPosition;
        double distSq = xDiff * xDiff + yDiff * yDiff;
        double dist = Math.sqrt( distSq );
        double force, forceX, forceY;
        double totRad = radius + other.radius;
        //test for contact between particles
        if( dist <= totRad ) {
            //linear restoring spring force and decreasign gravity
            force = ( totRad / dist - 1 ) / ( mass * other.mass * ( invSpring + other.invSpring ) ) - 1 / ( totRad * totRad * totRad );
            //implemented simple drag calculations - drag calculation which takes into account contact area and rVelocity
            //is too computationally heavy, so it was not added to this simulation. this simpler implementation is slightly
            //less accurate, which results in the simulation system losing any angular velocity due to this drag calcuation
            forceX = timeStep * ( force * xDiff + 0.00131 * ( other.xVelocity - xVelocity ) );
            forceY = timeStep * ( force * yDiff + 0.00131 * ( other.yVelocity - yVelocity ) );
        } else {
            //normal gravity alculations
            force = -timeStep / ( distSq * dist );
            forceX = force * xDiff;
            forceY = force * yDiff;
        }
        //update velocity with calculations
        other.xVelocity -= forceX * mass;
        other.yVelocity -= forceY * mass;
        xVelocity += forceX * other.mass;
        yVelocity += forceY * other.mass;
    }*/