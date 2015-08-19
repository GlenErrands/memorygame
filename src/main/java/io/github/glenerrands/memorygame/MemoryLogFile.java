package io.github.glenerrands.memorygame;

import static io.github.glenerrands.memorygame.Rating.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File to store and manage ratings for the other files in the same directory.
 */
public class MemoryLogFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLogFile.class);

	private final File _workingDirectory;

	private final File _file;

	private final Map<String, Rating> _ratings;

	private final Map<Rating, Integer> _ratingCounts;

	public MemoryLogFile(File workingDirectory) throws FileNotFoundException, MemoryGameException {
		_workingDirectory = workingDirectory;
		_file = new File(workingDirectory, ".memoryLog");
		_ratings = new HashMap<String, Rating>();
		_ratingCounts = new TreeMap<Rating, Integer>();
		load();
	}

	public Rating getRating(String fileName) {
		final Rating rating = _ratings.get(fileName);
		if (rating != null) {
			LOGGER.debug("rating of {} is {}", fileName, rating);
			return rating;
		} else {
			LOGGER.trace("not rated yet: {}", fileName);
			return Rating.NOT_SHOWN_YET;
		}
	}

	public void setRating(String fileName, Rating rating) {
		final Rating oldRating = _ratings.get(fileName);
		if (oldRating == null || oldRating != rating) {
			if (rating != null) {
				LOGGER.debug("setting rating of {} to {}", fileName, rating);
				_ratings.put(fileName, rating);
			} else {
				LOGGER.debug("removing rating of {}", fileName);
				_ratings.remove(fileName);
			}
		}
	}

	public void save() {
		final File file = getFile();
		if (file.exists()) {
			final File backupFile = new File(_workingDirectory, ".memoryLog.bak");
			LOGGER.debug("backing up {} to {}", file.getAbsolutePath(), backupFile.getAbsolutePath());
			if (backupFile.exists()) {
				LOGGER.debug("backup file exists, deleting {}", backupFile.getAbsolutePath());
				backupFile.delete();
			}
			if (file.renameTo(backupFile)) {
				LOGGER.debug("backup of {} complete", file.getAbsolutePath());
			} else {
				LOGGER.warn("backup did not work");
			}
		}
		LOGGER.debug("saving to {}", file.getAbsolutePath());
		try (PrintWriter saveFile = new PrintWriter(new FileOutputStream(file))) {
			for (Map.Entry<String, Rating> entry : _ratings.entrySet()) {
				saveFile.print(entry.getKey());
				saveFile.print(':');
				saveFile.print(entry.getValue());
				saveFile.println();
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("exception saving to " + file.getAbsoluteFile(), e);
		}
	}

	public void load() throws FileNotFoundException, MemoryGameException {
		_ratings.clear();
		resetRatingCounts();
		final File file = getFile();
		try (LineNumberReader loadFile = new LineNumberReader(new FileReader(file))) {
			if (file.exists()) {
				LOGGER.debug("loading from {}", file.getAbsolutePath());
				for (String line = loadFile.readLine(); line != null; line = loadFile.readLine()) {
					final String[] splitLine = line.split(":");
					final String fileName = splitLine[0];
					final String ratingString = splitLine[1];
					final Rating rating = Rating.valueOf(ratingString);

					if (new File(_workingDirectory, fileName).exists()) {
						setRating(fileName, rating);
						increaseRatingCount(rating);
					}
				}

			} else {
				LOGGER.debug("file does not exist: {}", file.getAbsolutePath());
			}

			final String[] allFilenames = _workingDirectory
					.list((dir, name) -> MemoryGameWindow.isFilenameAccepted(name));
			final int unratedCount = allFilenames.length - _ratings.size() + _ratingCounts.get(NOT_SHOWN_YET);
			_ratingCounts.put(NOT_SHOWN_YET, unratedCount);
		} catch (IOException e) {
			// wrap IOException in MemoryGameException to demonstrate multi-catch
			throw new MemoryGameException("exception accessing memory log file", e);
		}
	}

	public File getFile() {
		return _file;
	}

	public Map<String, Rating> getRatings() {
		return Collections.unmodifiableMap(_ratings);
	}

	public Map<Rating, Integer> getRatingCounts() {
		return Collections.unmodifiableMap(_ratingCounts);
	}

	private void increaseRatingCount(Rating rating) {
		_ratingCounts.put(rating, _ratingCounts.get(rating) + 1);
	}

	private void resetRatingCounts() {
		for (Rating rating : Rating.values()) {
			_ratingCounts.put(rating, 0);
		}
	}

}
