
package com.bernardini.vrphysioplatform;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Marker extends Rectangle{

    private double offset;

    public Marker(double width, double height, double offset) {
        super(width,height);
        this.offset = offset;
    }
    
    
    public double getOffset(){
        return offset;
    }
    
}
