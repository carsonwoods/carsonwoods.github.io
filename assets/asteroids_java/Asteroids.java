/**
 * @(#)Asteroids.java
 *
 *
 * @author Carson Woods
 * @version 1.00 2015/4/30
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

public class Asteroids extends Applet implements Runnable, KeyListener 
{
	//the main thread becomes the game loop 
	Thread gameloop;
	
	//use this a double buffer 
	BufferedImage backbuffer;
	
	//the main drawing object for the back buffer
	Graphics2D g2d;
	
	//toggle for drawing bounds boxes
	boolean showBounds = false;
	
	//create the asteroid array
	int ASTEROIDS = 20;
	Asteroid[] ast = new Asteroid[ASTEROIDS];
	
	//create bullet array
	int BULLETS = 10000;
	Bullet[] bullet = new Bullet[BULLETS];
	int currentBullet = 0;
	
	//the player's ship
	Ship ship = new Ship();
	
	//create the identity transform(0.0)
	AffineTransform identity = new AffineTransform();
	
	//create a random number generator
	Random rand = new Random();
	
	public void init()
	{
		//create the buffer for smooth graphics
		backbuffer = new BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
		g2d = backbuffer.createGraphics();
		
		//set up the ship
		ship.setX(320);
		ship.setY(240);
		
		//create the asteroids
		for (int n = 0; n < BULLETS; n++)
		{
			bullet[n] = new Bullet();
		}
		
		//create the asteroids
		for (int n = 0; n < ASTEROIDS; n++)
		{
			ast[n] = new Asteroid();
			ast[n].setRotationVelocity(rand.nextInt(3) + 1);
			ast[n].setX((double)rand.nextInt(600) + 20);
			ast[n].setY((double)rand.nextInt(440) + 20);
			ast[n].setMoveAngle(rand.nextInt(360));
			double ang = ast[n].getMoveAngle() - 90;
			ast[n].setVelX(calcAngleMoveX(ang));
			ast[n].setVelY(calcAngleMoveY(ang));
		}
		//starts the user input listener
		addKeyListener(this);
	}
	
	public void update(Graphics g)
	{
		//start off transforms at identity
		g2d.setTransform(identity);
		
		//erase the background
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, getSize().width, getSize().height);
		
		//print some status information
		g2d.setColor(Color.WHITE);
		g2d.drawString("Ship: " + Math.round(ship.getX()) + "," + Math.round(ship.getY()), 5, 10);
		g2d.drawString("Move angle: " + Math.round(ship.getMoveAngle()) + 90, 5, 25);
		g2d.drawString("Face angle: " + Math.round(ship.getFaceAngle()), 5, 40);
		
		//draw the game graphics
		drawShip();
		drawBullets();
		drawAsteroids();
		
		//reapaint the applet window
		paint(g);
	}
	
	public void drawShip()
	{
		g2d.setTransform(identity);
		g2d.translate(ship.getX(), ship.getY());
		g2d.rotate(Math.toRadians(ship.getFaceAngle()));
		g2d.setColor(Color.ORANGE);
		g2d.fill(ship.getShape());
	}
	
	public void drawBullets()
	{
		//iterate throught the array of bullets
		for (int n = 0; n < BULLETS; n++)
		{
			if (bullet[n].isAlive())
			{
				g2d.setTransform(identity);
				g2d.translate(bullet[n].getX(), bullet[n].getY());
				g2d.setColor(Color.MAGENTA);
				g2d.draw(bullet[n].getShape());
			}
		}
	}
	
	public void drawAsteroids()
	{
		for (int n = 0; n < ASTEROIDS; n++)
		{
			if (ast[n].isAlive())
			{
				g2d.setTransform(identity);
				g2d.translate(ast[n].getX(), ast[n].getY());
				g2d.rotate(Math.toRadians(ast[n].getMoveAngle()));
				g2d.setColor(Color.DARK_GRAY);
				g2d.fill(ast[n].getShape());
			}
		}
	}
	
	public void paint(Graphics g)
	{
		//draw the back buffer onto the applet window
		g.drawImage(backbuffer, 0, 0, this);
	}
	
	public void start()
	{
		gameloop = new Thread(this);
		gameloop.start();
	}
	
	public void run()
	{
		//aquire the current thread
		Thread t = Thread.currentThread();
		
		//keep going as long as teh thread is alive
		while (t == gameloop)
		{
			try
			{
				//update the game loop 
				gameUpdate();
				
				//target fps is 50
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			repaint();
			
		}
	}
	
	public void stop()
	{
		//kill the gameloop thread
		gameloop = null;
	}
	
	private void gameUpdate()
	{
		updateShip();
		updateBullets();
		updateAsteroids();
		checkCollisions();
	}
	
	public void updateShip()
	{
		//update ship's X position
		ship.incX(ship.getVelX());
		
		//wrap around left/right
		if (ship.getX() < -10)
			ship.setX(getSize().width + 10);
		else if (ship.getX() > getSize().width + 10)
			ship.setX(-10);
			
		//update ships Y postition
		ship.incY(ship.getVelY());
		
		//wrap around the top/bottom
		if (ship.getY() < -10)
			ship.setY(getSize().height + 10);
		else if (ship.getY() > getSize().height + 10)
			ship.setY(-10);
	}
	
	public void updateBullets()
	{
		for(int n=0; n < BULLETS; n++)
		{
			if (bullet[n].isAlive())
			{
				bullet[n].incX(bullet[n].getVelX());
					
				if(bullet[n].getX() < 0 || bullet[n].getX() > getSize().width)
				{
					bullet[n].setAlive(false);	
				}
				
				bullet[n].incY(bullet[n].getVelY());
				
				if (bullet[n].getY() < 0 || bullet[n].getY() > getSize().height)
				{
					bullet[n].setAlive(false);
				}
			}
		}
	}
	
	public void updateAsteroids()
	{
		//move and rotate asteroids
		for (int n = 0; n < ASTEROIDS; n++)
		{
			//is this asteroid being used
			if (ast[n].isAlive())
			{
					//update the asteroids x value
					ast[n].incX(ast[n].getVelX());
					
					//warp the asteroid's x value
					if (ast[n].getX() < -20)
						ast[n].setX(getSize().width + 20);
					else if (ast[n].getX() > getSize().width + 20)
						ast[n].setX(-20);
					
					//update the asteroid's Y value	
					ast[n].incY(ast[n].getVelY());
					
					//ward the asteroid at screen edges
					if(ast[n].getY() < -20)
						ast[n].setY(getSize().height + 20);
					else if (ast[n].getY() > getSize().height + 20)
						ast[n].setY(-20);
					
					//update the asteroid's rotation
					ast[n].incMoveAngle(ast[n].getRotationVelocity());
					
					//keep the angle within 0-359 degrees
					if (ast[n].getMoveAngle() < 0)
						ast[n].setMoveAngle(360 - ast[n].getRotationVelocity());
					else if (ast[n].getMoveAngle() > 360)
						ast[n].setMoveAngle(ast[n].getRotationVelocity());
			}
		}
	}
	
	public void checkCollisions()
	{
		for (int m = 0; m < ASTEROIDS; m++)
		{
			if (ast[m].isAlive())
			{
				for (int n = 0; n < BULLETS; n++)
				{
					if (bullet[n].isAlive())
					{
						if (ast[m].getBounds().contains(bullet[n].getX(), bullet[n].getY()))
						{
							bullet[n].setAlive(false);
							ast[m].setAlive(false);
							continue;
						}
					}
				}
				if (ast[m].getBounds().intersects(ship.getBounds()))
				{
					ast[m].setAlive(false);
					ship.setX(320);
					ship.setY(240);
					ship.setFaceAngle(0);
					ship.setVelX(0);
					ship.setVelY(0);
					continue;
				}
			}
		}
	}
	
	public void keyReleased(KeyEvent k) {}
	public void keyTyped(KeyEvent k) {}
	public void keyPressed(KeyEvent k)
	{
		int keyCode = k.getKeyCode();
		
		switch (keyCode)
		{
			case KeyEvent.VK_LEFT:
				//shifts the ship left 5 degrees
				ship.incFaceAngle(-5);
				if (ship.getFaceAngle() < 0) ship.setFaceAngle(360-5);
				break;
				
			case KeyEvent.VK_RIGHT:
				//right arrow rotates ship right 5 degrees
				ship.incFaceAngle(5);
				if (ship.getFaceAngle() > 360) ship.setFaceAngle(5);
				break;
				
			case KeyEvent.VK_UP:
				ship.setMoveAngle(ship.getFaceAngle() - 90);
				ship.incVelX(calcAngleMoveX(ship.getMoveAngle()) * 0.1);
				ship.incVelY(calcAngleMoveY(ship.getMoveAngle()) * 0.1);
				break;
			
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_SPACE:
				currentBullet++;
				if (currentBullet > BULLETS - 1) currentBullet = 0;
				bullet[currentBullet].setAlive(true);
				
				bullet[currentBullet].setX(ship.getX());
				bullet[currentBullet].setY(ship.getY());
				bullet[currentBullet].setMoveAngle(ship.getFaceAngle() - 90);
				
				double angle = bullet[currentBullet].getMoveAngle();
				double svx = ship.getVelX();
				double svy = ship.getVelY();
				bullet[currentBullet].setVelX(svx + calcAngleMoveX(angle) * 2);
				bullet[currentBullet].setVelY(svy + calcAngleMoveY(angle) * 2);
				break;
				
		}
	}
	
	public double calcAngleMoveX(double angle)
	{
		return(double) (Math.cos(angle * Math.PI / 180));
	}
	
	public double calcAngleMoveY(double angle)
	{
		return(double) (Math.sin(angle * Math.PI / 180));
	}
}