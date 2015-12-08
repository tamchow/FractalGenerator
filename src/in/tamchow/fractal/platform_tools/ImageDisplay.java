package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.FractalGenerator;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.fractalconfig.FractalConfig;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.imgutils.Animation;
import in.tamchow.fractal.imgutils.Transition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
/**
 * Swing app to display images
 */
public class ImageDisplay extends JPanel implements Runnable, KeyListener, MouseListener {
    BufferedImage[] img;
    Image[]         rimg;
    Image           todraw;
    int             width, height, ctr, subctr;
    ImageConfig      imgconf;
    FractalConfig    fracconf;
    FractalGenerator current;
    private boolean running, fractal_mode, zoomedin;
    private int zoomin;
    public ImageDisplay(Config config, int width, int height) {
        initDisplay(config, width, height);
    }
    private void initDisplay(Config config, int width, int height) {
        if (config instanceof ImageConfig) {
            try {
                ImageConfig imageConfig = (ImageConfig) config;
                this.width = width;
                this.height = height;
                todraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                imgconf = imageConfig;
                img = new BufferedImage[imageConfig.getImages().length];
                rimg = new Image[imageConfig.getImages().length];
                ctr = 0;
                subctr = 0;
                for (int i = 0; i < img.length; i++) {
                    if (imageConfig.getImages()[i].getPixdata() == null) {
                        img[i] = ImageIO.read(new File(imageConfig.getImages()[i].getPath()));
                        rimg[i] = img[i].getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
                    } else {
                        img[i] = ImageConverter.toImage(imageConfig.getImages()[i]);
                        rimg[i] = img[i].getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH);
                    }
                }
                fractal_mode = false;
            } catch (Exception e) {
                System.err.print("Image read error: " + e.getMessage());
            }
        } else if (config instanceof FractalConfig) {
            try {
                FractalConfig fractalConfig = (FractalConfig) config;
                this.width = fractalConfig.getParams()[0].initParams.width;
                this.height = fractalConfig.getParams()[0].initParams.height;
                todraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                fracconf = fractalConfig;
                img = new BufferedImage[fractalConfig.getParams().length];
                rimg = new Image[fractalConfig.getParams().length];
                ctr = 0;
                subctr = 0;
                zoomin = 1;
                fractal_mode = true;
            } catch (Exception e) {
                System.err.print("Image read error: " + e.getMessage());
            }
        }
    }
    public ImageDisplay(Config config) {
        if (config instanceof ImageConfig) {
            initDisplay(config, ((ImageConfig) config).getImages()[0].getWidth(), ((ImageConfig) config).getImages()[0].getHeight());
        } else if (config instanceof FractalConfig) {
            initDisplay(config, ((FractalConfig) config).getParams()[0].initParams.width, ((FractalConfig) config).getParams()[0].initParams.height);
        }
    }
    public static void show(Config config, String title) {
        ImageDisplay id         = new ImageDisplay(config);
        JScrollPane  scrollPane = new JScrollPane(id);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JFrame disp = new JFrame(title);
        disp.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        disp.addKeyListener(id);
        disp.add(scrollPane);
        disp.pack();
        id.running = true;
        disp.setVisible(true);
        Thread thread = new Thread(id);
        thread.start();
    }
    @Override
    public void run() {
        for (int i = ctr; i < rimg.length; ) {
            if (!fractal_mode) {
                if (imgconf.getTransitions()[i] == -1) {
                    todraw = rimg[i];
                    paint(this.getGraphics());
                    if (!running) {
                        ctr = i - 1;
                        break;
                    }
                } else {
                    int k = i + 1;
                    if (i == rimg.length - 1) {
                        k = 0;
                    }
                    Transition transition = new Transition(imgconf.getTransitions()[i], ImageConverter.toImageData(rimg[i]), ImageConverter.toImageData(rimg[k]), imgconf.getFps(), imgconf.getTranstime());
                    transition.doTransition();
                    Animation anim = transition.getFrames();
                    for (int j = subctr; j < anim.getNumFrames(); j++) {
                        todraw = ImageConverter.toImage(anim.getFrame(j));
                        paint(this.getGraphics());
                        if (!running) {
                            ctr = i - 1;
                            subctr = j;
                            break;
                        }
                        try {
                            Thread.sleep(1000 / imgconf.getFps());
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                try {
                    Thread.sleep(1000 * imgconf.getWait());
                } catch (InterruptedException ignored) {
                }
            } else {
                if (!zoomedin) {
                    current = new FractalGenerator(fracconf.getParams()[i]);
                }
                current.generate(fracconf.getParams()[i]);
                todraw = ImageConverter.toImage(current.getArgand());
                zoomedin = false;
                paint(this.getGraphics());
                try {
                    Thread.sleep(1000 * fracconf.getWait());
                } catch (InterruptedException ignored) {
                }
            }
            if (i == img.length - 1 && running) {
                ctr = 0;
                i = ctr;
                continue;
            }
            i++;
        }
    }
    public void paint(Graphics g) {
        g.drawImage(todraw, 0, 0, null);
    }
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed:" + e.getKeyChar());
        if (e.getKeyChar() == ' ') {
            running = (!running);
        } else if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(this);
            try {
                BufferedImage buf = new BufferedImage(todraw.getWidth(null), todraw.getHeight(null), BufferedImage.TYPE_INT_RGB);
                buf.getGraphics().drawImage(todraw, 0, 0, null);
                ImageIO.write(buf, "jpg", fileChooser.getSelectedFile());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if (fractal_mode) {
            try {
                running = false;
                zoomedin = true;
                current.zoom(e.getX(), e.getY(), zoomin);
                zoomin++;
                running = true;
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }
    }
    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
}