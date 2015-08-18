package io.github.glenerrands.memorygame;

import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

/**
 * Helper interface to enable lambda expressions for ancestorResized events.
 * All methods of {@link HierarchyBoundsListener} got a default implementation
 * except {@link #ancestorResized(HierarchyEvent)}.
 * <p>
 * Usage:
 * </p>
 * <code>
 * component.addHierarchyBoundsListener((AncestorResizedListener)(e)->System.out.println("Ancestor resized"));
 * </code>
 */
public interface AncestorResizedListener extends HierarchyBoundsListener {
	@Override
	public default void ancestorMoved(HierarchyEvent e) {}
}
