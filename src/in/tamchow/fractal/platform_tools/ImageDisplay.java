package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.transition.Transition;
import in.tamchow.fractal.graphicsutilities.transition.TransitionTypes;
import in.tamchow.fractal.helpers.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
/**
 * Swing app to display images &amp; complex number fractals
 */
public class ImageDisplay extends JPanel implements Runnable, KeyListener, MouseListener, Publisher {
    BufferedImage[] img;
    BufferedImage todraw;
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
    public ImageDisplay(Config config) {
        if (config instanceof ImageConfig) {
            int width, height;
            if (((ImageConfig) config).customDimensions()) {
                width = ((ImageConfig) config).getWidth();
                height = ((ImageConfig) config).getHeight();
            } else {
                if (((ImageConfig) config).getParams()[0].image.getPath() == null) {
                    width = ((ImageConfig) config).getParams()[0].image.getWidth();
                    height = ((ImageConfig) config).getParams()[0].image.getHeight();
                } else {
                    width = -1;
                    height = -1;
                }
            }
            initDisplay(config, width, height);
        } else if (config instanceof ComplexFractalConfig) {
            initDisplay(config,
                    ((ComplexFractalConfig) config).getParams()[0].initParams.width,
                    ((ComplexFractalConfig) config).getParams()[0].initParams.height);
        }
    }
    public static void show(final Config config, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                @NotNull ImageDisplay id = new ImageDisplay(config);
                @NotNull JScrollPane scrollPane = new JScrollPane(id);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                id.parent = new JFrame(title);
                id.parent.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
                id.parent.addKeyListener(id);
                id.parent.add(scrollPane);
                id.parent.pack();
                id.running = true;
                id.parent.setVisible(true);
                @NotNull Thread thread = new Thread(id);
                thread.start();
            }
        });
    }
    private void initDisplay(Config config, int width, int height) {
        if (config instanceof ImageConfig) {
            try {
                @NotNull ImageConfig imageConfig = (ImageConfig) config;
                BufferedImage first = ImageIO.read(new File(imageConfig.getParams()[0].image.getPath()));
                if (width == -1) {
                    width = first.getWidth();
                }
                if (height == -1) {
                    height = first.getHeight();
                }
                this.width = width;
                this.height = height;
                imgconf = imageConfig;
                img = new BufferedImage[imageConfig.getParams().length];
                ctr = 0;
                subctr = 0;
                for (int i = 1; i < img.length; ++i) {
                    if (imageConfig.getParams()[i].image.getPixdata() == null) {
                        img[i] = ImageIO.read(new File(imageConfig.getParams()[i].image.getPath()));
                    } else {
                        img[i] = ImageConverter.toImage(imageConfig.getParams()[i].image);
                    }
                    BufferedImage tmp = getGraphicsConfiguration().createCompatibleImage(
                            img[i].getWidth(), img[i].getHeight(), img[i].getTransparency());
                    Graphics2D graphics2D = tmp.createGraphics();
                    Rectangle clip = graphics2D.getClipBounds();
                    graphics2D.drawImage(img[i], clip.x, clip.y, clip.width, clip.height, null);
                    graphics2D.dispose();
                    img[i] = scale(tmp, this.width, this.height);
                }
                todraw = getGraphicsConfiguration().createCompatibleImage(
                        first.getWidth(), first.getHeight(), first.getTransparency());
                fractal_mode = false;
            } catch (Exception e) {
                System.err.print("Image read error: " + e.getMessage());
            }
        } else if (config instanceof ComplexFractalConfig) {
            try {
                @NotNull ComplexFractalConfig complexFractalConfig = (ComplexFractalConfig) config;
                this.width = complexFractalConfig.getParams()[0].initParams.width;
                this.height = complexFractalConfig.getParams()[0].initParams.height;
                todraw = getGraphicsConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
                fracconf = complexFractalConfig;
                img = new BufferedImage[complexFractalConfig.getParams().length];
                ctr = 0;
                subctr = 0;
                zoomin = 1.0;
                fractal_mode = true;
            } catch (Exception e) {
                System.err.print("Complex Fractal Configuration read error: " + e.getMessage());
            }
        }
    }
    private BufferedImage scale(BufferedImage bufferedImage, int width, int height) {
        /*Graphics2D graphics2d=bufferedImage.createGraphics();
        Map<RenderingHints.Key,Object> renderingHints=new HashMap<>();
        renderingHints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2d.addRenderingHints(renderingHints);
        graphics2d.drawImage(bufferedImage,0,0,width,height,null);
        graphics2d.dispose();*/
        return progressiveScale(bufferedImage, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
    }
    public BufferedImage progressiveScale(BufferedImage img,
                                          int targetWidth, int targetHeight, Object hint,
                                          boolean progressiveBilinear) {
        int transparency = img.getTransparency();
        //int type = img.getType();
        //(img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        @NotNull BufferedImage ret = img, scratchImage = null;
        @NotNull Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();
        if (progressiveBilinear) {
            /**
             * Use multi-step technique: start with original size,
             * then scale down in multiple passes with an
             * {@link Graphics#drawImage(Image, int, int, int, int, int, int, int, int, ImageObserver)}
             * until the target size is reached.
             */
            w = img.getWidth();
            h = img.getHeight();
        } else {
            /**
             * Use one-step technique: scale directly from original
             * size to target size with a single
             * {@link Graphics#drawImage(Image, int, int, int, int, int, int, int, int, ImageObserver)} call.
             */
            w = targetWidth;
            h = targetHeight;
        }
        do {
            if (progressiveBilinear && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }
            if (progressiveBilinear && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }
            if (scratchImage == null) {
                /**
                 * Use a single scratch buffer for all iterations
                 * and then copy to the final, correctly sized image
                 * before returning.
                 */
                scratchImage = getGraphicsConfiguration().createCompatibleImage(w, h, transparency);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    hint);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;
            ret = scratchImage;
            //g2.dispose();
        } while (w != targetWidth || h != targetHeight);
        if (g2 != null) {
            g2.dispose();
        }
        /**
         * If we used a scratch buffer that is larger than our
         * target size, create an image of the right size and copy
         *  the results into it.
         */
        if (targetWidth != ret.getWidth() ||
                targetHeight != ret.getHeight()) {
            scratchImage = getGraphicsConfiguration().createCompatibleImage(targetWidth, targetHeight, transparency);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }
        return ret;
    }
    @Override
    public synchronized void publish(String message, double progress, int data) {
        parent.setTitle("Generating Fractal: " + message);
    }
    @Override
    public synchronized void publish(@NotNull String message, double progress, int data, Object... args) {
        parent.setTitle("Generating Fractal: " + String.format(message, args));
    }
    @Override
    public void run() {
        for (int i = ctr; i < img.length; ) {
            if (!fractal_mode) {
                if (imgconf.getParams()[i].transition == TransitionTypes.NONE) {
                    todraw = img[i];
                    paint(this.getGraphics());
                    if (!running) {
                        ctr = i - 1;
                        break;
                    }
                } else {
                    int k = i + 1;
                    if (i == img.length - 1) {
                        k = 0;
                    }
                    @NotNull Transition transition = new Transition(imgconf.getParams()[i].transition,
                            ImageConverter.toPixelContainer(img[i]), ImageConverter.toPixelContainer(img[k]),
                            imgconf.getParams()[i].getFps(), imgconf.getParams()[i].getTranstime());
                    transition.doTransition();
                    @NotNull Animation anim = transition.getFrames();
                    for (int j = subctr; j < anim.getNumFrames(); j++) {
                        todraw = ImageConverter.toImage(anim.getFrame(j));
                        revalidate();
                        repaint(getGraphics().getClipBounds());
                        //paint(getGraphics());
                        if (!running) {
                            ctr = i - 1;
                            subctr = j;
                            break;
                        }
                        try {
                            Thread.sleep(1000 / imgconf.getParams()[i].getFps());
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                try {
                    Thread.sleep(1000 * imgconf.getParams()[i].getWait());
                } catch (InterruptedException ignored) {
                }
            } else {
                if (!zoomedin) {
                    current = new ComplexFractalGenerator(fracconf.getParams()[i], this);
                }
                if (fracconf.getParams()[i].useThreadedGenerator()) {
                    @NotNull ThreadedComplexFractalGenerator threaded =
                            new ThreadedComplexFractalGenerator(current, fracconf.getParams()[0]);
                    threaded.generate();
                } else {
                    current.generate();
                }
                todraw = ImageConverter.toImage(current.getPlane());
                zoomedin = false;
                revalidate();
                repaint(getGraphics().getClipBounds());
                //paint(getGraphics());
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
            ++i;
        }
    }
    @Override
    public void paintComponent(@NotNull Graphics g) {
        super.paintComponent(g);
        Rectangle clip = g.getClipBounds();
        g.drawImage(todraw, clip.x, clip.y, clip.width, clip.height, null);
    }
    @NotNull
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(@NotNull KeyEvent e) {
        System.out.println("Key pressed:" + e.getKeyChar());
        if (e.getKeyChar() == ' ') {
            running = (!running);
        } else if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
            @NotNull final JComponent parent = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    @NotNull final JFileChooser fileChooser = new JFileChooser();
                    fileChooser.showSaveDialog(parent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                @NotNull BufferedImage buf = getGraphicsConfiguration().createCompatibleImage(
                                        todraw.getWidth(), todraw.getHeight(), todraw.getTransparency());
                                @NotNull Graphics2D graphics2D = buf.createGraphics();
                                @NotNull Rectangle clip = graphics2D.getClipBounds();
                                graphics2D.drawImage(buf, clip.x, clip.y, clip.width, clip.height, null);
                                ImageIO.write(buf, "jpg", fileChooser.getSelectedFile());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
    }
    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
        if (fractal_mode) {
            try {
                running = false;
                zoomedin = true;
                current.zoom(e.getX(), e.getY(), zoomin);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    zoomin++;
                } else if (e.getButton() == MouseEvent.BUTTON3 ||
                        (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1)) {
                    zoomin--;
                }
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