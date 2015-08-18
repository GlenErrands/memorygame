package io.github.glenerrands.memorygame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A group of tiles (usually a pair) with the same image.
 */
public class TileGroup {

	private final MemoryGameWindow memoryGameWindow;

	private final File _imageFile;

	private final BufferedImage image;

	private final List<Tile> tiles;

	public TileGroup(MemoryGameWindow memoryGameWindow, File imageFile, BufferedImage image, int numberOfTiles) {
		this.memoryGameWindow = memoryGameWindow;
		this.image = image;
		_imageFile = imageFile;
		this.tiles = new ArrayList<Tile>(numberOfTiles);
		for (int i = 0; i < numberOfTiles; i++) {
			final Tile tile = new Tile(this);
			this.tiles.add(tile);
			tile.addMouseListener((MouseClickedListener) e -> TileGroup.this.memoryGameWindow.tileClicked(tile));
		}
	}

	public boolean isCompletelyUncovered() {
		for (Tile tile : getTiles()) {
			if (!tile.isUncovered()) {
				return false;
			}
		}
		return true;
	}

	public List<Tile> getTiles() {
		return tiles;
	}

	public BufferedImage getImage() {
		return image;
	}

	public MemoryGameWindow getMemoryGameWindow() {
		return memoryGameWindow;
	}

	public File getImageFile() {
		return _imageFile;
	}
}
