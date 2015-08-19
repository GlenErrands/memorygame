package io.github.glenerrands.memorygame;

import static java.lang.Math.*;
import static javax.swing.JFileChooser.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main panel for the memory game.
 */
public class MemoryGameWindow extends JPanel implements Runnable {

	private static final long serialVersionUID = 3288768687427099265L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryGameWindow.class);

	private static final Random RANDOM = new Random();

	private static final Set<String> ACCEPTED_EXTENSIONS = new HashSet<String>(
			Arrays.asList("jpg", "jpeg", "gif", "png"));

	private int numberOfImages;

	private File currentDirectory = null;

	/**
	 * Width in number of tiles.
	 */
	private int horizontalNumberOfTiles;

	/**
	 * Height in number of tiles.
	 */
	private int verticalNumberOfTiles;

	private List<File> imageFiles;

	private final List<MemoryPlayer> _players = new ArrayList<MemoryPlayer>();

	private int _currentPlayerIndex;

	/**
	 * Width of a tile in pixels.
	 */
	private int tileWidth;

	/**
	 * Height of a tile in pixels.
	 */
	private int tileHeight;

	/**
	 * Horizontal space between tiles in pixels.
	 */
	private final int horizontalTileSpacing;

	/**
	 * Vertical space between tiles in pixels.
	 */
	private final int verticalTileSpacing;

	private final List<Tile> tiles;

	private final List<Tile> uncoveredTiles;

	private JPanel imagePanel = null;

	private MemoryLogFile _memoryLogFile;

	private JFileChooser fileChooser;

	private GameModeAccessory _gameModeAccessory;

	public MemoryGameWindow() {
		setLayout(null);

		this.tiles = new ArrayList<Tile>();
		this.uncoveredTiles = new ArrayList<Tile>();
		this.imageFiles = new ArrayList<File>();
		this.horizontalTileSpacing = 5;
		this.verticalTileSpacing = 5;

		addHierarchyBoundsListener((AncestorResizedListener) e -> handleResize());

		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Directories";
			}

			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		_gameModeAccessory = new GameModeAccessory(fileChooser);
		fileChooser.setAccessory(_gameModeAccessory);
	}

	protected void createGui() throws FileNotFoundException, MemoryGameException {
		_gameModeAccessory.updateCheckboxLabels();
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			setupMemoryGameGui(fileChooser.getSelectedFile(), _gameModeAccessory.getNumberOfTileGroups(),
					_gameModeAccessory.getNumberOfPlayers());
		}
	}

	private void setupMemoryGameGui(File imageDirectory, int numberOfTileGroups, int numberOfPlayers)
			throws FileNotFoundException, MemoryGameException {
		setupPlayers(numberOfPlayers);

		currentDirectory = imageDirectory;
		LOGGER.debug("Using directory: " + currentDirectory.getName());

		final MemoryLogFile memoryLogFile = new MemoryLogFile(currentDirectory);
		setMemoryLogFile(memoryLogFile);

		numberOfImages = numberOfTileGroups;
		int correction = 0;
		boolean done = false;
		do {
			final int numberOfTiles = (numberOfImages + correction) * 2;
			int firstDivider = Double.valueOf(Math.floor(Math.sqrt(numberOfTiles))).intValue();
			while ((numberOfTiles % firstDivider) != 0) {
				firstDivider--;
			}
			this.verticalNumberOfTiles = firstDivider;
			this.horizontalNumberOfTiles = numberOfTiles / firstDivider;

			if (((double) this.horizontalNumberOfTiles / (double) this.verticalNumberOfTiles) > (16d / 9d)) {
				// bad side ratio
				correction++;
				done = false;
			} else {
				done = true;
			}
		} while (!done);

		this.tileHeight = (getHeight() - ((this.verticalNumberOfTiles - 1) * this.verticalTileSpacing))
				/ this.verticalNumberOfTiles;
		this.tileWidth = (getWidth() - ((this.horizontalNumberOfTiles - 1) * this.horizontalTileSpacing))
				/ this.horizontalNumberOfTiles;

		this.imageFiles = selectRandomImages(currentDirectory, getNumberOfImages());
		tiles.clear();
		final List<TileGroup> tilePairs = new ArrayList<TileGroup>();
		for (File imageFile : imageFiles) {
			try {
				final TileGroup tileGroup = new TileGroup(this, imageFile, ImageIO.read(imageFile.toURI().toURL()), 2);
				tilePairs.add(tileGroup);
				tiles.addAll(tileGroup.getTiles());
			} catch (IOException e) {
				// wrap in MemoryGameException for multi-catch demonstration
				throw new MemoryGameException("Exception reading image file", e);
			}
		}
		Collections.shuffle(tiles);

		final Iterator<Tile> tilesIterator = tiles.iterator();
		for (int x = 0; x < this.horizontalNumberOfTiles; x++) {
			for (int y = 0; y < this.verticalNumberOfTiles; y++) {
				final int yPos = y * (this.tileHeight + this.verticalTileSpacing);
				final int xPos = x * (this.tileWidth + this.horizontalTileSpacing);
				if (tilesIterator.hasNext()) {
					final Tile tile = tilesIterator.next();
					tile.setBounds(xPos, yPos, tileWidth, tileHeight);
					add(tile);
				}
			}
		}
		repaint();
	}

	private void setupPlayers(int numberOfPlayers) {
		_players.clear();
		_currentPlayerIndex = 0;
		for (int playerNumber = 1; playerNumber <= numberOfPlayers; playerNumber++) {
			_players.add(new MemoryPlayer(String.format("Spieler Nr.%d", playerNumber)));
		}
	}

	private void nextPlayer() {
		_currentPlayerIndex = (++_currentPlayerIndex % _players.size());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Current player is now: %s", getCurrentPlayer()));
		}
	}

	private MemoryPlayer getCurrentPlayer() {
		return _players.get(_currentPlayerIndex);
	}

	public void handleResize() {
		final int width = getWidth();
		final int height = getHeight();
		LOGGER.debug("resizing to " + width + "x" + height);
		super.setSize(width, height);

		if (!tiles.isEmpty()) {
			this.tileHeight = (height - ((this.verticalNumberOfTiles - 1) * this.verticalTileSpacing))
					/ this.verticalNumberOfTiles;
			this.tileWidth = (width - ((this.horizontalNumberOfTiles - 1) * this.horizontalTileSpacing))
					/ this.horizontalNumberOfTiles;

			final Iterator<Tile> tilesIterator = tiles.iterator();
			for (int x = 0; tilesIterator.hasNext() && x < this.horizontalNumberOfTiles; x++) {
				for (int y = 0; tilesIterator.hasNext() && y < this.verticalNumberOfTiles; y++) {
					final int yPos = y * (this.tileHeight + this.verticalTileSpacing);
					final int xPos = x * (this.tileWidth + this.horizontalTileSpacing);
					final Tile tile = tilesIterator.next();
					tile.setBounds(xPos, yPos, tileWidth, tileHeight);
				}
			}
		}

		if (imagePanel != null) {
			// TODO resize image panel
		}
	}

	protected synchronized void tileClicked(Tile tile) {
		LOGGER.debug("Tile clicked");
		uncoverTile(tile);
	}

	private void uncoverTile(final Tile tile) {
		Runnable updateRunnable = null;
		if (uncoveredTiles.size() < 2 && !tile.isFlipping() && !tile.isUncovered()) {
			LOGGER.debug("less than two tiles uncovered");
			uncoveredTiles.add(tile);
			tile.flip();
			if (uncoveredTiles.size() >= 2) {
				LOGGER.debug("two tiles uncovered");
				final TileGroup tileGroup = tile.getTileGroup();
				if (tileGroup.isCompletelyUncovered()) {
					LOGGER.debug("matching tiles uncovered");
					updateRunnable = () -> {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.error("interrupted", e);
						}
						uncoveredTiles.clear();
						getCurrentPlayer().addUncoveredTileGroup(tileGroup);
						for (Tile tileToRemove : tileGroup.getTiles()) {
							remove(tileToRemove);
						}
						try {
							imagePanel = createImagePanel(tileGroup);
							add(imagePanel, 0);
						} catch (IOException e) {
							LOGGER.error("cannot display image", e);
						}
						validate();
						repaint();
					};
				} else {
					LOGGER.debug("unmatching tiles uncovered");
					nextPlayer();
					updateRunnable = () -> {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.error("interrupted", e);
						}
						for (Tile uncoveredTile : uncoveredTiles) {
							uncoveredTile.flip();
						}
						uncoveredTiles.clear();
					};
				}
			}
		}
		if (updateRunnable != null) {
			new Thread(updateRunnable).start();
		}
	}

	private JPanel createImagePanel(final TileGroup tileGroup) throws IOException {
		final JPanel imagePanel = new JPanel(new BorderLayout());
		final JLabel displayImageLabel = new JLabel(
				getScaledImage(tileGroup.getImageFile(), getWidth() - 10, getHeight() - 10));
		displayImageLabel.setPreferredSize(new Dimension(getWidth() - 10, getHeight() - 10));
		displayImageLabel.setMaximumSize(new Dimension(getWidth() - 10, getHeight() - 10));
		imagePanel.add(displayImageLabel, BorderLayout.CENTER);
		final JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		final String fileName = tileGroup.getImageFile().getName();
		buttonPanel.add(new JButton(new AbstractAction("Don't show again") {
			private static final long serialVersionUID = -1773394206139269292L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemoryLogFile().setRating(fileName, Rating.DO_NOT_SHOW_AGAIN);
				getMemoryLogFile().save();
				MemoryGameWindow.this.remove(imagePanel);
				MemoryGameWindow.this.imagePanel = null;
				MemoryGameWindow.this.repaint();
				checkGameEnding();
			}
		}));
		buttonPanel.add(new JButton(new AbstractAction("Show again") {
			private static final long serialVersionUID = -576276898438007779L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemoryLogFile().setRating(fileName, Rating.SHOW_AGAIN);
				getMemoryLogFile().save();
				MemoryGameWindow.this.remove(imagePanel);
				MemoryGameWindow.this.imagePanel = null;
				MemoryGameWindow.this.repaint();
				checkGameEnding();
			}
		}));
		buttonPanel.add(new JButton(new AbstractAction("Favourite!") {
			private static final long serialVersionUID = 8846481145551819072L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemoryLogFile().setRating(fileName, Rating.FAVOURITE);
				getMemoryLogFile().save();
				MemoryGameWindow.this.remove(imagePanel);
				MemoryGameWindow.this.imagePanel = null;
				MemoryGameWindow.this.repaint();
				checkGameEnding();
			}
		}));
		imagePanel.add(buttonPanel, BorderLayout.SOUTH);
		imagePanel.setBounds(0, 0, getWidth(), getHeight());
		return imagePanel;
	}

	public void checkGameEnding() {
		if (getComponents().length == 0) {
			LOGGER.info("Game has ended! Results:");
			for (MemoryPlayer player : _players) {
				LOGGER.info(String.format("  %s", player));
			}
			SwingUtilities.invokeLater(() -> {
				try {
					createGui();
				} catch (FileNotFoundException|MemoryGameException e) {
					// multi-catch demonstration
					LOGGER.error("could not create GUI", e);
				}
			});
		}
	}

	public ImageIcon getScaledImage(File imageFile, int maxWidth, int maxHeight) throws IOException {
		LOGGER.debug("Scaling image - maximum dimensions: " + maxWidth + "x" + maxHeight);
		final BufferedImage bufferedImage = ImageIO.read(imageFile);
		LOGGER.debug("Scaling image - original size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
		if (bufferedImage.getHeight() > maxHeight || bufferedImage.getWidth() > maxWidth) {
			final double scaleX = ((double) maxWidth) / ((double) bufferedImage.getWidth());
			final double scaleY = ((double) maxHeight) / ((double) bufferedImage.getHeight());
			final double scale = Math.min(scaleX, scaleY);
			LOGGER.debug("Scaling image - scale: " + scale);
			final int width = (int) (bufferedImage.getWidth() * scale);
			final int height = (int) (bufferedImage.getHeight() * scale);
			final Image scaledInstance = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			LOGGER.debug("Scaling image - scaled to: " + width + "x" + height);
			return new ImageIcon(scaledInstance);
		} else {
			LOGGER.debug("Scaling image - using original size: " + bufferedImage.getWidth() + "x"
					+ bufferedImage.getHeight());
			return new ImageIcon(imageFile.toURI().toURL());
		}
	}

	protected List<File> selectRandomImages(File imageDir, int numberOfImages) {
		final File[] imageFiles = imageDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				final Rating rating = getMemoryLogFile().getRating(name);
				if (_gameModeAccessory.isRatingSelected(rating)) {
					return isFilenameAccepted(name);
				} else {
					return false;
				}
			}
		});
		if (imageFiles.length < numberOfImages) {
			LOGGER.warn(imageDir.getAbsolutePath() + " contains less than " + numberOfImages + " files");
		}
		final Set<Integer> selectedIndexes = new HashSet<Integer>();
		while (selectedIndexes.size() < min(numberOfImages, imageFiles.length)) {
			selectedIndexes.add(RANDOM.nextInt(imageFiles.length));
		}

		final List<File> selectedImageFiles = new ArrayList<File>();
		for (Integer index : selectedIndexes) {
			selectedImageFiles.add(imageFiles[index.intValue()]);
		}

		return selectedImageFiles;
	}

	@Override
	public void run() {
		try {
			createGui();
		} catch (FileNotFoundException | MemoryGameException e) {
			// multi-catch demonstration
			LOGGER.error("could not create GUI", e);
		}
	}

	static boolean isFilenameAccepted(String name) {
		final int indexOfLastDot = name.lastIndexOf('.');
		if (indexOfLastDot >= 0 && indexOfLastDot < name.length()) {
			final String extension = name.substring(indexOfLastDot + 1);
			return ACCEPTED_EXTENSIONS.contains(extension.toLowerCase());
		} else {
			return false;
		}
	}

	public List<Tile> getUncoveredTiles() {
		return uncoveredTiles;
	}

	public int getNumberOfImages() {
		return numberOfImages;
	}

	public MemoryLogFile getMemoryLogFile() {
		return _memoryLogFile;
	}

	public void setMemoryLogFile(MemoryLogFile memoryLogFile) {
		_memoryLogFile = memoryLogFile;
	}

}
