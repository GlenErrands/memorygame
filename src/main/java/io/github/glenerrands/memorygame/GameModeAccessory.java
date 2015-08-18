package io.github.glenerrands.memorygame;

import static io.github.glenerrands.memorygame.Rating.*;
import static java.awt.event.ItemEvent.*;
import static javax.swing.BoxLayout.*;
import static javax.swing.JFileChooser.*;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel in which the user can choose the desired gaming mode and parameters.
 */
public class GameModeAccessory extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -4835145445717580557L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GameModeAccessory.class);

	private final JFileChooser _fileChooser;

	private final Set<Rating> _selectedRatings = new HashSet<Rating>();

	private int _numberOfPlayers = 1;

	private final Map<Rating, JCheckBox> _checkboxes = new HashMap<Rating, JCheckBox>();

	private final JSlider _numberOfTileGroupsControl;

	private final JLabel _numberOfTileGroupsDisplay;

	private final JSpinner _numberOfPlayersControl;

	public GameModeAccessory(JFileChooser fileChooser) {
		_fileChooser = fileChooser;
		_selectedRatings.add(NOT_SHOWN_YET);

		setLayout(new BoxLayout(this, Y_AXIS));

		_numberOfTileGroupsDisplay = new JLabel();
		add(_numberOfTileGroupsDisplay);

		_numberOfTileGroupsControl = new JSlider(10, 50, 10);
		_numberOfTileGroupsControl.setMajorTickSpacing(10);
		_numberOfTileGroupsControl.setMinorTickSpacing(1);
		_numberOfTileGroupsControl.setPaintLabels(true);
		_numberOfTileGroupsControl.setPaintTicks(true);
		_numberOfTileGroupsControl.setPaintTrack(true);
		_numberOfTileGroupsControl.setSnapToTicks(true);
		_numberOfTileGroupsControl.addChangeListener(e -> updateNumberOfTileGroupsDisplay());
		add(_numberOfTileGroupsControl);
		updateNumberOfTileGroupsDisplay();

		final JPanel numberOfPlayersPanel = new JPanel(new FlowLayout());
		numberOfPlayersPanel.add(new JLabel("Number of players:"));
		_numberOfPlayersControl = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		_numberOfPlayersControl.getModel().addChangeListener(e -> setNumberOfPlayers((Integer) _numberOfPlayersControl.getValue()));
		numberOfPlayersPanel.add(_numberOfPlayersControl);
		add(numberOfPlayersPanel);

		for (final Rating rating : Rating.values()) {
			final JCheckBox checkbox = new JCheckBox(rating.name(), isRatingSelected(rating));
			checkbox.addItemListener(e -> setRatingSelected(rating, e.getStateChange() == SELECTED));
			_checkboxes.put(rating, checkbox);
			add(checkbox);
		}

		_fileChooser.addPropertyChangeListener(this);
	}

	public synchronized boolean isRatingSelected(Rating rating) {
		return _selectedRatings.contains(rating);
	}

	public synchronized void setRatingSelected(Rating rating, boolean state) {
		if (state) {
			_selectedRatings.add(rating);
		} else {
			_selectedRatings.remove(rating);
		}
	}

	private void updateNumberOfTileGroupsDisplay() {
		_numberOfTileGroupsDisplay.setText(MessageFormat.format("Number of tile groups: {0}", getNumberOfTileGroups()));
	}

	public int getNumberOfTileGroups() {
		return _numberOfTileGroupsControl.getModel().getValue();
	}

	@Override
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		final String propertyName = propertyChangeEvent.getPropertyName();
		if (DIRECTORY_CHANGED_PROPERTY.equals(propertyName)) {
			final File currentDirectory = _fileChooser.getCurrentDirectory();
			if (currentDirectory != null && currentDirectory.isDirectory()) {
				updateCheckboxLabels(currentDirectory);
			}
		} else if (SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName)) {
			final File selectedFile = _fileChooser.getSelectedFile();
			if (selectedFile != null && selectedFile.isDirectory()) {
				updateCheckboxLabels(selectedFile);
			}
		}
	}

	public void updateCheckboxLabels() {
		updateCheckboxLabels(_fileChooser.getCurrentDirectory());
	}

	private void updateCheckboxLabels(final File directory) {
		try {
			final MemoryLogFile memoryLogFile = new MemoryLogFile(directory);
			for (final Rating rating : Rating.values()) {
				final JCheckBox checkbox = _checkboxes.get(rating);
				checkbox.setText(
						MessageFormat.format("{0} ({1} files)", rating, memoryLogFile.getRatingCounts().get(rating)));
			}
		} catch (IOException e) {
			LOGGER.error("could not update rating counts", e);
		}
	}

	public int getNumberOfPlayers() {
		return _numberOfPlayers;
	}

	public void setNumberOfPlayers(int numberOfPlayers) {
		LOGGER.debug(String.format("Number of players is now: %d", numberOfPlayers));
		_numberOfPlayers = numberOfPlayers;
	}

}
