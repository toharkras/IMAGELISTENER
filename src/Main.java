import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends JFrame {

    private static final int THRESHOLD = 100;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    public Main() {
        File file = new File("C:\\Users\\tohar\\Downloads\\website.jpg");
        if (file.exists()) {
            try {
                BufferedImage originalImage = ImageIO.read(file);

                if (originalImage != null) {
                    BufferedImage negativeImage = createNegativeImage(originalImage);
                    BufferedImage borderedImage = drawBorders(negativeImage);

                    DisplayImage displayImage = new DisplayImage(borderedImage);
                    this.add(displayImage);

                    this.setSize(borderedImage.getWidth(), borderedImage.getHeight());
                    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
                    this.setVisible(true);
                } else {
                    throw new IOException("Failed to read the image");
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading the image:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Image file not found:\n" + file.getAbsolutePath(), "File Not Found", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static class DisplayImage extends JPanel {
        private BufferedImage image;
        private ArrayList<Point> points;

        private void rerender(){
            new Thread(()->{
                while (true){
                    repaint();
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        throw new RuntimeException(e);
                    }
                }
            }
                    ).start();

        }

        public DisplayImage(BufferedImage image) {
            this.image = image;
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            this.points = new ArrayList<>();
            rerender();

            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Point point = new Point(e.getX(), e.getY());
                    points.add(point);
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    Point point= new Point(e.getX(), e.getY());
                    points.add(point);
                    repaint();
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
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
            g.setColor(Color.BLUE);
            System.out.println(this.points.size());

            // Draw all marked points
            for (Point point : points) {
                g.setColor(Color.BLUE); // Set color for points
                g.fillOval(point.getX() - 5, point.getY() - 5, 5, 5); // Draw a small oval centered at the point
            }
        }
    }

    private static BufferedImage createNegativeImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage negativeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = originalImage.getRGB(x, y);
                int red = 255 - ((rgb >> 16) & 0xFF);
                int green = 255 - ((rgb >> 8) & 0xFF);
                int blue = 255 - (rgb & 0xFF);
                int alpha = (rgb >> 24) & 0xFF;

                int negativeRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                negativeImage.setRGB(x, y, negativeRGB);
            }
        }

        return negativeImage;
    }

    private static BufferedImage drawBorders(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage borderedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width - 1; x++) {
            for (int y = 0; y < height - 1; y++) {
                Color currentPixel = new Color(image.getRGB(x, y));
                Color rightNeighbor = new Color(image.getRGB(x + 1, y));
                Color downNeighbor = new Color(image.getRGB(x, y + 1));

                // Calculate color differences
                int redDiff1 = Math.abs(currentPixel.getRed() - rightNeighbor.getRed());
                int greenDiff1 = Math.abs(currentPixel.getGreen() - rightNeighbor.getGreen());
                int blueDiff1 = Math.abs(currentPixel.getBlue() - rightNeighbor.getBlue());
                int totalDiff1 = redDiff1 + greenDiff1 + blueDiff1;

                int redDiff2 = Math.abs(currentPixel.getRed() - downNeighbor.getRed());
                int greenDiff2 = Math.abs(currentPixel.getGreen() - downNeighbor.getGreen());
                int blueDiff2 = Math.abs(currentPixel.getBlue() - downNeighbor.getBlue());
                int totalDiff2 = redDiff2 + greenDiff2 + blueDiff2;

                // Check if either neighbor has a significant color difference
                if (totalDiff1 > THRESHOLD || totalDiff2 > THRESHOLD) {
                    borderedImage.setRGB(x, y, Color.RED.getRGB());
                } else {
                    borderedImage.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }

        return borderedImage;
    }
}