package io.github.glenerrands.memorygame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A player playing the memory game.
 */
public class MemoryPlayer {

	private String _name;

	private final List<TileGroup> _uncoveredTileGroups = new ArrayList<TileGroup>();

	public MemoryPlayer(String name) {
		setName(name);
	}

	public List<TileGroup> getUncoveredTileGroups() {
		return Collections.unmodifiableList(_uncoveredTileGroups);
	}

	public void addUncoveredTileGroup(TileGroup tileGroup) {
		_uncoveredTileGroups.add(tileGroup);
	}

	public void clearUncoveredTileGroups() {
		_uncoveredTileGroups.clear();
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Player[");
		stringBuilder.append(getName());
		stringBuilder.append(", ");
		stringBuilder.append(getNumberOfUncoveredTileGroups());
		stringBuilder.append("]");
		return stringBuilder.toString();
	}

	public int getNumberOfUncoveredTileGroups() {
		return _uncoveredTileGroups.size();
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

}
