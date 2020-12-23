package com.example.loadouts;

import com.example.GearSlot;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public final class LoadoutPanel extends JPanel {

  private static final Dimension BACKGROUND_SIZE = new Dimension(148, 194);
  private static final Dimension PANEL_SIZE =
      new Dimension(PluginPanel.PANEL_WIDTH, BACKGROUND_SIZE.height);
  private static final Point BACKGROUND_POSITION = createBackgroundPosition();

  private final HashMap<GearSlot, Component> drawnImages = new HashMap<>();
  private final BufferedImage backgroundImage;
  private final BufferedImage slotBackgroundImage;

  public LoadoutPanel() {
    setLayout(null);
    setSize(PANEL_SIZE);
    setPreferredSize(PANEL_SIZE);

    backgroundImage = ImageUtil.getResourceStreamFromClass(getClass(), "loadout_background.png");
    slotBackgroundImage =
        ImageUtil.getResourceStreamFromClass(getClass(), "slot_blank_background.png");
  }

  public void drawImageInSlot(BufferedImage image, GearSlot slot) {
    SwingUtilities.invokeLater(
        () -> {
          if (drawnImages.containsKey(slot)) {
            remove(drawnImages.remove(slot));
          }

          Rectangle bounds = slot.getDrawableArea();
          bounds.translate(BACKGROUND_POSITION.x, BACKGROUND_POSITION.y);

          ImagePanel imagePanel = new ImagePanel(image, slotBackgroundImage, bounds);
          drawnImages.put(slot, add(imagePanel));

          revalidate();
          repaint();
        });
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(
        backgroundImage, BACKGROUND_POSITION.x, BACKGROUND_POSITION.y, /* observer= */ null);
  }

  private static Point createBackgroundPosition() {
    int x = (PANEL_SIZE.width - BACKGROUND_SIZE.width) / 2;
    return new Point(x, 0);
  }

  private static final class ImagePanel extends JPanel {

    final BufferedImage image;
    final BufferedImage bgImage;
    final Rectangle imageBounds;

    ImagePanel(BufferedImage image, BufferedImage bgImage, Rectangle bounds) {
      this.image = image;
      this.bgImage = bgImage;
      this.imageBounds = calculateImageBounds(image, bounds);

      setBounds(bounds);
      setLayout(null);
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(bgImage, /* x= */ 0, /* y= */ 0, /* observer= */ null);
      g.drawImage(
          image,
          imageBounds.x,
          imageBounds.y,
          imageBounds.width,
          imageBounds.height,
          /* observer= */ null);

      // TODO: Remove debug code.
      g.setColor(Color.RED);
      g.drawRect(imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);
    }

    private static Rectangle calculateImageBounds(BufferedImage image, Rectangle bounds) {
      if (image.getWidth() <= bounds.width && image.getHeight() <= bounds.height) {
        int x = (bounds.width - image.getWidth()) / 2;
        int y = (bounds.height - image.getHeight()) / 2;
        return new Rectangle(x, y, image.getWidth(), image.getHeight());
      }

      double aspectRatio = (double) image.getWidth() / image.getHeight();
      if (aspectRatio <= 1) {
        int newHeight = (int) (image.getHeight() * aspectRatio);
        int y = (bounds.height - newHeight) / 2;
        return new Rectangle(0, y, bounds.width, newHeight);
      }

      aspectRatio = (double) image.getHeight() / image.getWidth();
      int newWidth = (int) (image.getWidth() * aspectRatio);
      int x = (bounds.width - newWidth) / 2;
      return new Rectangle(x, 0, newWidth, bounds.height);
    }
  }
}
