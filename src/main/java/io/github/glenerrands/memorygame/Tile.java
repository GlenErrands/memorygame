package io.github.glenerrands.memorygame;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single, flippable tile in the GUI of the memory game.
 */
public class Tile extends JComponent {

	private static final long serialVersionUID = 3832294699261778944L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Tile.class);

	private final TileGroup _tileGroup;

	private float turnFactor = -1.0f;

	private boolean flipping = false;

	private boolean uncovered = false;

	public Tile(TileGroup tileGroup) {
		_tileGroup = tileGroup;
	}

	@Override
	public void paint(Graphics g) {
		LOGGER.trace("drawing..." + getX() + "/" + getY() + " " + getWidth() + "x" + getHeight());
		final Graphics2D graphics2d = (Graphics2D) g;
		final int y = 0;
		if (turnFactor > 0) {
			final BufferedImage image = getTileGroup().getImage();
			final float scale = Math.min(((float) getWidth()) / image.getWidth(),
					((float) getHeight()) / image.getHeight());
			final int x = Math.round((getWidth() / 2.0f) - (image.getWidth() * scale * turnFactor * 0.5f));
			final AffineTransform translateInstance = AffineTransform.getTranslateInstance(x, y);
			final AffineTransform scaleInstance = AffineTransform.getScaleInstance(turnFactor * scale, scale);
			translateInstance.concatenate(scaleInstance);
			graphics2d.drawImage(image, translateInstance, null);
			// graphics2d.drawImage(getTileGroup().getImage(), x, y, width,
			// height, null);
		} else {
			final int x = Math.round((getWidth() / 2.0f) * (1.0f - Math.abs(turnFactor)));
			final int width = Math.round(getWidth() * Math.abs(turnFactor)) - 1;
			final int height = getHeight() - 1;
			graphics2d.setPaint(new GradientPaint(0, 0, Color.GRAY, 0, height * 2, Color.BLUE));
			graphics2d.fillRect(x, y, width, height);
		}
	}

	public synchronized void flip() {
		if (!isFlipping()) {
			setFlipping(true);
			final boolean uncovered = isUncovered();
			setUncovered(!uncovered);
			Thread t = new Thread((Runnable) () -> {
				if (uncovered) {
					LOGGER.debug("covering tile");
					for (turnFactor = 1.0f; turnFactor > -1.0; turnFactor -= 0.1) {
						try {
							Thread.sleep(15);
						} catch (InterruptedException e) {
							LOGGER.error("interrupted", e);
						}
						repaint();
					}
				} else {
					LOGGER.debug("uncovering tile");
					for (turnFactor = -1.0f; turnFactor < 1.0; turnFactor += 0.1) {
						try {
							Thread.sleep(15);
						} catch (InterruptedException e) {
							LOGGER.error("interrupted", e);
						}
						repaint();
					}
				}
				setFlipping(false);
			});
			t.start();
		}
	}

	public TileGroup getTileGroup() {
		return _tileGroup;
	}

	public boolean isFlipping() {
		return flipping;
	}

	public void setFlipping(boolean flipping) {
		this.flipping = flipping;
	}

	public boolean isUncovered() {
		return uncovered;
	}

	public void setUncovered(boolean uncovered) {
		this.uncovered = uncovered;
	}

}