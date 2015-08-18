package io.github.glenerrands.memorygame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Main class starting the memory game.
 */
public class SwingMemoryGame {

	private enum ScreenMode {
		FULLSCREEN,
		WINDOW
	}

	private ScreenMode screenMode = ScreenMode.WINDOW;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final SwingMemoryGame swingGame = new SwingMemoryGame();
		swingGame.configure(args);
		try {
			swingGame.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void configure(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--window") || args[i].equals("-w")) {
				setScreenMode(ScreenMode.WINDOW);
			} else if (args[i].equals("--fullscreen") || args[i].equals("-f")) {
				setScreenMode(ScreenMode.FULLSCREEN);
			}
		}
	}

	private void run() throws IOException {
		final GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final MemoryGameWindow main = new MemoryGameWindow();
		main.setBackground(Color.DARK_GRAY);
		if (getScreenMode().equals(ScreenMode.FULLSCREEN) && defaultScreenDevice.isFullScreenSupported()) {
			final JWindow jWindow = new JWindow();
			jWindow.setLayout(new BorderLayout());
			jWindow.add(main, BorderLayout.CENTER);
			defaultScreenDevice.setFullScreenWindow(jWindow);
		} else {
			final JFrame jFrame = new JFrame("SwingGame");
			jFrame.setLayout(new BorderLayout());
			jFrame.add(main, BorderLayout.CENTER);
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.pack();
			jFrame.setSize(1000, 800);
			jFrame.setVisible(true);
		}

		final Thread thread = new Thread(main);
		SwingUtilities.invokeLater(thread);
	}

	public void setScreenMode(ScreenMode screenMode) {
		this.screenMode = screenMode;
	}

	public ScreenMode getScreenMode() {
		return screenMode;
	}

}
