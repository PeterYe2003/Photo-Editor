// Written by Peter. I talked with Seth on this project.

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

public class PhotoEditor {
    private BufferedImage currentImage;
    private JFrame myJFrame;
    private JPanel myJPanel;
    private JSlider penSizeSlider;
    private PhotoCanvas myPhotoCanvas;
    private boolean scribbleToggle = false;
    private Color filterColor = Color.WHITE;
    private Color penColor = Color.BLACK;
    private double sumofRed;
    private double sumofGreen;
    private double sumofBlue;
    private int newRed;
    private int newGreen;
    private int newBlue;

    public static void main(String[] args) {
        new PhotoEditor();
    }

    PhotoEditor() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        }
        myJPanel = new JPanel();
        myJPanel.setLayout(new BorderLayout());
        penSizeSlider = new JSlider(1, 100);

        penSizeSlider.setBorder(new TitledBorder("Pen Size"));
        penSizeSlider.setPaintTicks(true);
        penSizeSlider.setPaintLabels(true);
        Hashtable labelsOnSlider = new Hashtable();
        labelsOnSlider.put(5, new JLabel("1"));
        labelsOnSlider.put(95, new JLabel("100"));
        penSizeSlider.setLabelTable(labelsOnSlider);
        penSizeSlider.setVisible(false);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu draw = new JMenu("Draw");
        JMenu filter = new JMenu("Filter");

        file.add(new ImageSave());
        file.add(new ImageOpen());
        file.add(new ImageClear());

        filter.add(new GreyscaleFilter());
        filter.add(new ColorFilter());
        filter.add(new FilterColorPicker());
        filter.add(new PixelateFilter());
        filter.add(new GaussianBlur());
        filter.add(new SharpenFilter());

        draw.add(new ScribbleOn());

        menuBar.add(file);
        menuBar.add(draw);
        menuBar.add(filter);

        myPhotoCanvas = new PhotoCanvas(500, 500);
        myJPanel.add(myPhotoCanvas, BorderLayout.CENTER);
        myJPanel.add(penSizeSlider, BorderLayout.SOUTH);

        myJFrame = new JFrame("PhotoEditor");
        myJFrame.add(myJPanel);
        myJFrame.setJMenuBar(menuBar);
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // So the Photocanvas is blank and drawable when first created.
        currentImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = currentImage.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());

        myJFrame.pack();

        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);

    }


    class PhotoCanvas extends ImageCanvas implements MouseMotionListener, MouseListener {
        private double scale = 1;
        private double heightRatio = 1;
        private double widthRatio = 1;
        double recentX;
        double recentY;

        public PhotoCanvas(int width, int height) {
            super(width, height);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void draw() {
            Graphics2D pen = myPhotoCanvas.getPen();
            pen.drawImage(currentImage, 0, 0, (int) (currentImage.getWidth() * scale), (int) (currentImage.getHeight() * scale), myPhotoCanvas);
            myPhotoCanvas.display();
            pen.dispose();
        }

        public void resized() {
            double currentImageHeight = currentImage.getHeight();
            double currentImageWidth = currentImage.getWidth();
            heightRatio = myPhotoCanvas.getHeight() / currentImageHeight;
            widthRatio = myPhotoCanvas.getWidth() / currentImageWidth;
            scale = Math.min(heightRatio, widthRatio);
            draw();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (scribbleToggle) {
                if (e.getX() < myPhotoCanvas.getWidth() && e.getY() < myPhotoCanvas.getHeight() && currentImage != null) {
                    Graphics2D pen = (Graphics2D) currentImage.getGraphics();
                    pen.setColor(penColor);
                    pen.setStroke(new BasicStroke(penSizeSlider.getValue() / 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    recentX = e.getX();
                    recentY = e.getY();
                    pen.drawLine((int) (recentX / scale), (int) (recentY / scale), (int) (e.getX() / scale), (int) (e.getY() / scale));
                    pen.dispose();
                    myPhotoCanvas.draw();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseDragged(MouseEvent e) {
            if (scribbleToggle) {
                if (e.getX() < myPhotoCanvas.getWidth() && e.getY() < myPhotoCanvas.getHeight() && e.getX() >= 0 && e.getY() >= 0 && currentImage != null) {
                    Graphics2D pen = (Graphics2D) currentImage.getGraphics();
                    pen.setColor(penColor);
                    pen.setStroke(new BasicStroke(penSizeSlider.getValue() / 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    pen.drawLine((int) (recentX / scale), (int) (recentY / scale), (int) (e.getX() / scale), (int) (e.getY() / scale));
                    recentX = e.getX();
                    recentY = e.getY();
                    pen.dispose();
                    myPhotoCanvas.draw();
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    class ScribbleOn extends JMenuItem implements ActionListener {

        ScribbleOn() {
            super("Scribble");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            scribbleToggle = !scribbleToggle;
            if (scribbleToggle) {
                penSizeSlider.setVisible(true);
                penColor = JColorChooser.showDialog(myPhotoCanvas,
                        "Choose Pen Color", penColor);
                if (penColor == null) {
                    penColor = Color.BLACK;
                }
            }
        }
    }

    class ImageOpen extends JMenuItem implements ActionListener {
        ImageOpen() {
            super("Open");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser myJFileChooser = new JFileChooser();
            int state = myJFileChooser.showOpenDialog(myJFrame);
            if (state == JFileChooser.APPROVE_OPTION) {
                try {
                    currentImage = ImageIO.read(myJFileChooser.getSelectedFile());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            myPhotoCanvas.resized();
            myPhotoCanvas.draw();
        }
    }

    class ImageSave extends JMenuItem implements ActionListener {
        ImageSave() {
            super("Save");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser myJFileChooser = new JFileChooser();
            int state = myJFileChooser.showSaveDialog(myJFrame);
            if (state == JFileChooser.APPROVE_OPTION) {
                try {
                    ImageIO.write(currentImage, "png", myJFileChooser.getSelectedFile());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    class ImageClear extends JMenuItem implements ActionListener {
        ImageClear() {
            super("Clear");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            Graphics2D graphics = currentImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, currentImage.getWidth(), currentImage.getHeight());
            myPhotoCanvas.draw();
        }
    }

    class GreyscaleFilter extends JMenuItem implements ActionListener {
        GreyscaleFilter() {
            super("Greyscale");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            applyFilter((r, g, b) -> new Color((int) (0.3 * r + 0.59 * g + 0.11 * b), (int) (0.3 * r + 0.59 * g + 0.11 * b), (int) (0.3 * r + 0.59 * g + 0.11 * b)));
        }
    }

    class ColorFilter extends JMenuItem implements ActionListener {
        ColorFilter() {
            super("ColorFilter");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            applyFilter((r, g, b) ->
                    new Color(r > filterColor.getRed() ? Math.max(r - 20, filterColor.getRed()) : Math.min(r + 20, filterColor.getRed()),
                            g > filterColor.getGreen() ? Math.max(g - 20, filterColor.getGreen()) : Math.min(g + 20, filterColor.getGreen()),
                            b > filterColor.getBlue() ? Math.max(b - 20, filterColor.getBlue()) : Math.min(b + 20, filterColor.getBlue())));
        }
    }

    class FilterColorPicker extends JMenuItem implements ActionListener {
        FilterColorPicker() {
            super("Pick Color For Filter");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            filterColor = JColorChooser.showDialog(myPhotoCanvas,
                    "Choose Filter Color", filterColor);
            if (filterColor == null) {
                filterColor = Color.WHITE;
            }
        }
    }


    class PixelateFilter extends JMenuItem implements ActionListener {
        PixelateFilter() {
            super("Pixelate");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            if (currentImage != null) {
                for (int x = 0; x < currentImage.getWidth(); x = x + 12) {
                    for (int y = 0; y < currentImage.getHeight(); y = y + 12) {
                        sumofRed = 0;
                        sumofGreen = 0;
                        sumofBlue = 0;
                        int numberOfPixelsInGrid = 0;
                        for (int a = x; a < x + 12 && a < currentImage.getWidth(); a++) {
                            for (int b = y; b < y + 12 && b < currentImage.getHeight(); b++) {
                                Color pixelColor = new Color(currentImage.getRGB(x, y));
                                sumofRed += pixelColor.getRed();
                                sumofGreen += pixelColor.getGreen();
                                sumofBlue += pixelColor.getBlue();
                                numberOfPixelsInGrid++;
                            }
                        }
                        newRed = (int) sumofRed / numberOfPixelsInGrid;
                        newGreen = (int) sumofGreen / numberOfPixelsInGrid;
                        newBlue = (int) sumofBlue / numberOfPixelsInGrid;
                        int pixelatedColor = new Color(newRed, newGreen, newBlue).getRGB();
                        for (int a = x; a < x + 12 && a < currentImage.getWidth(); a++) {
                            for (int b = y; b < y + 12 && b < currentImage.getHeight(); b++) {
                                currentImage.setRGB(a, b, pixelatedColor);
                            }
                        }
                    }
                }
                myPhotoCanvas.draw();
            }
        }
    }

    // Kind of hard to tell if the image is actually sharpened so I'm not sure if this totally works on not. Seems like it just makes the photo a bit lighter.
    class SharpenFilter extends JMenuItem implements ActionListener {
        SharpenFilter() {
            super("Sharpen Image");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            if (currentImage != null) {
                BufferedImage copyImage = currentImage;
                for (int x = 0; x < currentImage.getWidth(); x++) {
                    for (int y = 0; y < currentImage.getHeight(); y++) {
                        sumofRed = 0;
                        sumofGreen = 0;
                        sumofBlue = 0;
                        int numberOfPixelsInGrid = (Math.min(x + 1, currentImage.getWidth())-Math.max(x - 1, 0)+1)*(Math.min(y + 1, currentImage.getHeight())-Math.max(y - 1, 0)+1);
                        for (int a = Math.max(x - 1, 0); a <= Math.min(x + 1, currentImage.getWidth()); a++) {
                            for (int b = Math.max(y - 1, 0); b <= Math.min(y + 1, currentImage.getHeight()); b++) {
                                Color pixelColor = new Color(currentImage.getRGB(x, y));
                                if (a != x && b != y) {
                                    sumofRed += -pixelColor.getRed() / (double) (numberOfPixelsInGrid-1);
                                    sumofGreen += -pixelColor.getGreen() / (double) ((numberOfPixelsInGrid-1));
                                    sumofBlue += -pixelColor.getBlue() / (double) ((numberOfPixelsInGrid-1));
                                } else {
                                    sumofRed += 2 * pixelColor.getRed();
                                    sumofGreen += 2 * pixelColor.getGreen();
                                    sumofBlue += 2 * pixelColor.getBlue();
                                }
                            }
                        }
                        // This ensures we don't get negative RGB values.
                        newRed = Math.min((int) (sumofRed / numberOfPixelsInGrid), 255);
                        newGreen = Math.min((int) (sumofGreen / numberOfPixelsInGrid), 255);
                        newBlue = Math.min((int) (sumofBlue / numberOfPixelsInGrid), 255);
                        newRed = Math.max(newRed, 0);
                        newGreen = Math.max(newGreen, 0);
                        newBlue = Math.max(newBlue, 0);
                        int pixelatedColor = new Color(newRed, newGreen, newBlue).getRGB();
                        copyImage.setRGB(x, y, pixelatedColor);
                    }
                }
                currentImage = copyImage;
                myPhotoCanvas.draw();
            }
        }
    }

    class GaussianBlur extends JMenuItem implements ActionListener {
        GaussianBlur() {
            super("Gaussian Blur");
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            if (currentImage != null) {
                int standardDeviation = 3;
                BufferedImage currentImageCopy = currentImage;
                double[] kernel = new double[1 + 6 * standardDeviation];
                for (int x = 0; x < 1 + 6 * standardDeviation; x++) {
                    kernel[x] = calculateGaussianFunction(standardDeviation, x - 3 * standardDeviation);
                }

                double sumOfKernalValuesUsed;
                for (int x = 0; x < currentImage.getWidth(); x++) {
                    for (int y = 0; y < currentImage.getHeight(); y++) {
                        sumofRed = 0;
                        sumofGreen = 0;
                        sumofBlue = 0;
                        sumOfKernalValuesUsed = 0;
                        for (int a = Math.max((x - 3 * standardDeviation), 0); a < Math.min(x + 3 * standardDeviation, currentImage.getWidth() - 1); a++) {
                            // I use the Max to make sure that the pixels to the left do not fall off the screen. I use Min to make sure they don't use pixels to far right.
                            Color pixelColor = new Color(currentImage.getRGB(a, y));
                            sumOfKernalValuesUsed += kernel[a - x + 3 * standardDeviation];
                            sumofRed = sumofRed + pixelColor.getRed() * kernel[a - x + 3 * standardDeviation];
                            sumofGreen = sumofGreen + pixelColor.getGreen() * kernel[a - x + 3 * standardDeviation];
                            sumofBlue = sumofBlue + pixelColor.getBlue() * kernel[a - x + 3 * standardDeviation];
                        }

                        currentImageCopy.setRGB(x, y, new Color((int) (sumofRed / sumOfKernalValuesUsed), (int) (sumofGreen / sumOfKernalValuesUsed), (int) (sumofBlue / sumOfKernalValuesUsed)).getRGB());
                    }
                }
                for (int x = 0; x < currentImage.getWidth(); x++) {
                    for (int y = 0; y < currentImage.getHeight(); y++) {
                        sumOfKernalValuesUsed = 0;
                        sumofRed = 0;
                        sumofGreen = 0;
                        sumofBlue = 0;
                        for (int b = Math.max((y - 3 * standardDeviation), 0); b < Math.min(y + 3 * standardDeviation, currentImage.getHeight() - 1); b++) {
                            Color pixelColor = new Color(currentImageCopy.getRGB(x, b));
                            sumOfKernalValuesUsed += kernel[b - y + 3 * standardDeviation];
                            sumofRed = sumofRed + pixelColor.getRed() * kernel[b - y + 3 * standardDeviation];
                            sumofGreen = sumofGreen + pixelColor.getGreen() * kernel[b - y + 3 * standardDeviation];
                            sumofBlue = sumofBlue + pixelColor.getBlue() * kernel[b - y + 3 * standardDeviation];
                        }
                        currentImageCopy.setRGB(x, y, new Color((int) (sumofRed / sumOfKernalValuesUsed), (int) (sumofGreen / sumOfKernalValuesUsed), (int) (sumofBlue / sumOfKernalValuesUsed)).getRGB());
                    }
                }
                currentImage = currentImageCopy;
                myPhotoCanvas.draw();
            }
        }

        private double calculateGaussianFunction(double standardDevation, int distanceFromOrigin) {
            return 1 / (Math.sqrt(2 * Math.PI * standardDevation * standardDevation)) * Math.exp(-distanceFromOrigin * distanceFromOrigin / (2 * standardDevation * standardDevation));
        }
    }

    private void applyFilter(ColorTransformer transformer) {
        if (currentImage != null) {
            for (int x = 0; x < currentImage.getWidth(); x++) {
                for (int y = 0; y < currentImage.getHeight(); y++) {
                    Color currentColor = new Color(currentImage.getRGB(x, y));
                    Color newColor = transformer.transformColor(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
                    currentImage.setRGB(x, y, newColor.getRGB());
                }
            }
            myPhotoCanvas.draw();
        }
    }
}