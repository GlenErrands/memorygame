package io.github.glenerrands.memorygame;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Helper interface to enable lambda expressions for mouseClicked events.
 * All methods of {@link MouseListener} got a default implementation
 * except {@link #mouseClicked(MouseEvent)}.
 * <p>
 * Usage:
 * </p>
 * <code>
 * component.addMouseListener((MouseClickedListener)(e)->System.out.println("Mouse clicked"));
 * </code>
 */
interface MouseClickedListener extends MouseListener
{
    @Override
    public default void mouseEntered(MouseEvent e) {}

    @Override
    public default void mouseExited(MouseEvent e) {}

    @Override
    public default void mousePressed(MouseEvent e) {}

    @Override
    public default void mouseReleased(MouseEvent e) {}
}