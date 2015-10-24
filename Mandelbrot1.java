/*  Draw a Mandelbrot set, maximum magnification 10000000 times;
 *  @author Qian Xie (C) Digest Java, 1999. All rights reserved.
 *  @author qianxie@hotmail.com
 *  @author http://melting.fortunecity.com/trinity/660
 *  @version JDK1.0, AWT Version 
 */
 /**
  * I thought I would use this for reference, but its just an artifact now.
  * My fractal generator operates nothing like the one depicted here.
  * Its here, with due credit to the original authors, for anyone else who may want references from it.  * 
  * /
import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;

public class Mandelbrot1 extends Applet implements Runnable{

  private int AppletWidth, AppletHeight;
  private boolean firstTime=true;
  private double amin = -2.0;
  private double amax =  1.0;
  private double bmin = -1.5;
  private double bmax =  1.5;
  private double alen, blen;
  private double a=0.0;
  private double b=0.0;
  private double x=0.0;
  private double y=0.0;
  private double acenter,bcenter;
  private int zoomin=10;
  private int scaleda,scaledb;
  Thread t = null;
  private Graphics offGraphics, onGraphics;
  Image offImage=null;
  private int delay=0;
  private int Index=0;
  int[] pixels;
  private MemoryImageSource source;
  int alpha = 0xff;
  int red = 0xff;
  int green = 0xff;
  int blue = 0xff;
  int times = 255;

  public void init() {

    AppletWidth = size().width;
    AppletHeight = size().height;
    alen=amax-amin;
    blen=bmax-bmin;

    pixels = new int[AppletWidth*AppletHeight];

    onGraphics=getGraphics();
    offImage=this.createImage(AppletWidth,AppletHeight);
    offGraphics=offImage.getGraphics();

    Font font = new Font("TimesRoman",Font.BOLD,30);
    onGraphics.setFont(font);
    onGraphics.setColor(Color.red);

  }

  public void start() {
    if(t==null) {
      t = new Thread(this);
      t.start();
    }
  }

  public void stop () {
    t = null;
  }

  public void run () {

    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    while (t!=null) {

      if(Index==1) {
        int incr=0;
        for(int iy=0;iy<AppletHeight;iy++){
          for(int ix=0;ix<AppletWidth;ix++){
            pixels[incr++]=alpha<<24 | 0x00<<16 | 0x00<< 8 | 0xff;
          }
        }
        if(firstTime) { 
          source=new MemoryImageSource
                 (AppletWidth,AppletHeight,pixels,0,AppletWidth);
          firstTime=false;
        } else {
          source.newPixels(0,0,AppletWidth,AppletHeight);
        }
        offImage=createImage(source);
        paint(onGraphics);
        red   = (int)(Math.random()*255);
        green = (int)(Math.random()*255);
        blue  = (int)(Math.random()*255);
      }

      if(Index==2) {

        double x1,y1;
        for(a=amin;a<amax;a+=((amax-amin)/AppletWidth)) {
          for(b=bmin;b<bmax;b+=((bmax-bmin)/AppletHeight)) {
            x=0.0;
            y=0.0;
            int iteration=0;
            while((x*x+y*y<=4.0) && (iteration!=times)) {
              x1 = xmapMandelbrot(x,y,a);
              y1 = ymapMandelbrot(x,y,b);
              x = x1;
              y = y1;
              iteration ++;
            }
            if(iteration<=times && iteration>0) {
              scaleda=scaleX(a,amin,amax);
              scaledb=scaleY(b,bmin,bmax);
              pixels[scaledb*AppletWidth+scaleda]=
                  alpha<<24 | red<<16 | iteration<<8 | blue;
            }
          }
        }
        source.newPixels(0,0,AppletWidth,AppletHeight);
        offImage=createImage(source);
      }

      Index++;
      paint(onGraphics);

      try {
        Thread.currentThread().sleep(delay);
      }
      catch (InterruptedException e) {
      }
    }

  }

  public void paint(Graphics g) {
    if(offImage!=null) {
      g.drawImage(offImage,0,0,AppletWidth,AppletHeight,null);
      if(Index<=2) g.drawString("WAIT",AppletWidth/2-70,AppletHeight/2);
    }
  }

  private double xmapMandelbrot(double x, double y, double a) {
    return x*x-y*y+a;
  }

  private double ymapMandelbrot(double x, double y, double b) {
    return 2.0*x*y+b;
  }

  private int scaleX(double x, double xmin, double xmax)  {
    int ivalue;
    if( x >= xmin && x < xmax ) {
      ivalue = (int) ((x - xmin)*AppletWidth/(xmax - xmin));
    }
    else {
      ivalue=0;
      System.out.println("arg "+x+" out of range:["+xmin+","+xmax+"]");
      System.exit(1);
    }
    return ivalue;
  }

  private int scaleY(double y, double ymin, double ymax)  {
    int jvalue;
    if( y >= ymin && y < ymax ) {
      jvalue = (int) ((y - ymin)*AppletHeight/(ymax - ymin));
    }
    else {
      jvalue=0;
      System.out.println("arg "+y+" out of range:["+ymin+","+ymax+"]");
      System.exit(2);
    }
    return jvalue;
  }

  public boolean mouseDown (Event e, int x, int y) {
    if(x>AppletWidth) {x=AppletWidth;}
    if(x<0) {x=0;}
    if(y>AppletHeight) {y=AppletHeight;}
    if(y<0) {y=0;}
    acenter=((double) (x)/(double) (AppletWidth))*alen+amin;
    bcenter=((double) (y)/(double) (AppletHeight))*blen+bmin;
    amin=acenter-alen/zoomin;
    amax=acenter+alen/zoomin;
    bmin=bcenter-blen/zoomin;
    bmax=bcenter+blen/zoomin;
    alen=amax-amin;
    blen=bmax-bmin;
    if(Math.max(alen,blen)<0.00000003) {
      amax=1.0;amin=-2.0;bmin=-1.5;bmax=1.5;
      alen=amax-amin;blen=bmax-bmin;
    }
    Index=1;
    return true;
  }

}









