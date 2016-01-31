package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.imgutils.Animation;
import in.tamchow.fractal.imgutils.Transition;
import in.tamchow.fractal.imgutils.TransitionTypes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
/**
 * Swing app to display images & complex number fractals
 */
public class ImageDisplay extends JPanel implements Runnable, KeyListener, MouseListener, Publisher {
    BufferedImage[] img;
    Image[] rimg;
    Image todraw;
    JFrame parent;
    int width, height, ctr, subctr;
    ImageConfig imgconf;
    ComplexFractalConfig fracconf;
    ComplexFractalGenerator current;
    private boolean running, fractal_mode, zoomedin;
    private double zoomin;
    public ImageDisplay(Config config, int width, int height) {
        initDisplay(config, width, height);
    }
    private void initDisplay(Config config, int width, int height) {
        if (config instanceof ImageConfig) {try {
                ImageConfig imageConfig = (ImageConfig) config;
            if (width == -1) {width = ImageIO.read(new File(imageConfig.getParams()[0].image.getPath())).getWidth();}
            if (height == -1) {height = ImageIO.read(new File(imageConfig.getParams()[0].image.getPath())).getHeight();}
            this.width = width; this.height = height;
            todraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); imgconf = imageConfig;
            img = new BufferedImage[imageConfig.getParams().length]; rimg = new Image[imageConfig.getParams().length];
            ctr = 0; subctr = 0;
                for (int i = 0; i < img.length; i++) {
                    if (imageConfig.getParams()[i].image.getPixdata() == null) {
                        img[i] = ImageIO.read(new File(imageConfig.getParams()[i].image.getPath()));
                        rimg[i] = img[i].getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
                    } else {
                        img[i] = ImageConverter.toImage(imageConfig.getParams()[i].image);
                        rimg[i] = img[i].getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
                    }
                } fractal_mode = false;
        } catch (Exception e) {
            System.err.print("Image read error: " + e.getMessage());
        }
        } else if (config instanceof ComplexFractalConfig) {try {
                ComplexFractalConfig complexFractalConfig = (ComplexFractalConfig) config;
                this.width = complexFractalConfig.getParams()[0].initParams.width;
                this.height = complexFractalConfig.getParams()[0].initParams.height;
                todraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); fracconf = complexFractalConfig;
                img = new BufferedImage[complexFractalConfig.getParams().length];
                rimg = new Image[complexFractalConfig.getParams().length]; ctr = 0; subctr = 0; zoomin = 1.0;
            fractal_mode = true;
        } catch (Exception e) {
            System.err.print("Complex Fractal Configuration read error: " + e.getMessage());
        }
        }
    }
    public ImageDisplay(Config config) {
        if (config instanceof ImageConfig) {
            int width, height; if (((ImageConfig) config).customDimensions()) {
                width = ((ImageConfig) config).getWidth(); height = ((ImageConfig) config).getHeight();
            } else {
                if (((ImageConfig) config).getParams()[0].image.getPath() == null) {
                    width = ((ImageConfig) config).getParams()[0].image.getWidth();
                    height = ((ImageConfig) config).getParams()[0].image.getHeight();
                } else {width = -1; height = -1;}
            } initDisplay(config, width, height);
        } else if (config instanceof ComplexFractalConfig) {
            initDisplay(config, ((ComplexFractalConfig) config).getParams()[0].initParams.width, ((ComplexFractalConfig) config).getParams()[0].initParams.height);}}
    public static void show(Config config, String title) {
        ImageDisplay id = new ImageDisplay(config); JScrollPane scrollPane = new JScrollPane(id);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); id.parent = new JFrame(title);
        id.parent.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        }); id.parent.addKeyListener(id); id.parent.add(scrollPane); id.parent.pack(); id.running = true;
        id.parent.setVisible(true); Thread thread = new Thread(id); thread.start();
    }
    @Override
    public synchronized void publish(String message, double progress) {
        parent.setTitle("Generating Fractal: " + message);
    }
    @Override
    public void run() {
        for (int i = ctr; i < rimg.length; ) {
            if (!fractal_mode) {
                if (imgconf.getParams()[i].transition == TransitionTypes.NONE) {
                    todraw = rimg[i]; paint(this.getGraphics()); if (!running) {ctr = i - 1; break;}
                } else {
                    int k = i + 1; if (i == rimg.length - 1) {k = 0;}
                    Transition transition = new Transition(imgconf.getParams()[i].transition, ImageConverter.toImageData(rimg[i]), ImageConverter.toImageData(rimg[k]), imgconf.getParams()[i].getFps(), imgconf.getParams()[i].getTranstime());
                    transition.doTransition(); Animation anim = transition.getFrames();
                    for (int j = subctr; j < anim.getNumFrames(); j++) {
                        todraw = ImageConverter.toImage(anim.getFrame(j)); paint(this.getGraphics());
                        if (!running) {ctr = i - 1; subctr = j; break;} try {
                            Thread.sleep(1000 / imgconf.getParams()[i].getFps());
                        } catch (InterruptedException ignored) {}
                    }
                } try {Thread.sleep(1000 * imgconf.getParams()[i].getWait());} catch (InterruptedException ignored) {}
            } else {
                if (!zoomedin) {current = new ComplexFractalGenerator(fracconf.getParams()[i], this);}
                if (fracconf.getParams()[i].useThreadedGenerator()) {
                    ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(current, fracconf.getParams()[0]);
                    threaded.generate();
                } else {
                    current.generate(fracconf.getParams()[i]);
                } todraw = ImageConverter.toImage(current.getArgand());
                zoomedin = false; paint(this.getGraphics());
                try {Thread.sleep(1000 * fracconf.getWait());} catch (InterruptedException ignored) {}
            } if (i == img.length - 1 && running) {ctr = 0; i = ctr; continue;}i++;}}
    public void paint(Graphics g) {
        g.drawImage(todraw, 0, 0, null);
    }
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed:" + e.getKeyChar()); if (e.getKeyChar() == ' ') {
            running = (!running);
        } else if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
            JFileChooser fileChooser = new JFileChooser(); fileChooser.showSaveDialog(this);try {
                BufferedImage buf = new BufferedImage(todraw.getWidth(null), todraw.getHeight(null), BufferedImage.TYPE_INT_RGB);
                buf.getGraphics().drawImage(todraw, 0, 0, null);
                ImageIO.write(buf, "jpg", fileChooser.getSelectedFile());
            } catch (Exception ex) {ex.printStackTrace();}
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {
        if (fractal_mode) {
            try {
                running = false; zoomedin = true; current.zoom(e.getX(), e.getY(), zoomin);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    zoomin++;
                } else if (e.getButton() == MouseEvent.BUTTON3 || (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1)) {
                    zoomin--;
                } running = true;
            } catch (Exception ie) {ie.printStackTrace();}
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
}