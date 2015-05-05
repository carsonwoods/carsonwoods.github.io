/**
 * @(#)Bullet.java
 *
 *
 * @author Carson Woods
 * @version 1.00 2015/4/30
 */

import java.awt.*;
import java.awt.Rectangle;
public class Bullet extends BaseVectorShape
{
    //bounding rectangle
    public Rectangle getBounds()
    {
        Rectangle r;
        r = new Rectangle((int)getX(), (int)getY(), 1, 1);
        return r;
    }
    
    Bullet() 
    {
    	//create bullet shape
    	setShape(new Rectangle(0, 0, 1, 1));
    	setAlive(false);
    }
}