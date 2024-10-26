package com.hydra.overlay;

import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;

import static net.runelite.client.ui.overlay.OverlayUtil.renderPolygon;

public class OverlayUtil {

	/**
	 * Renders an Overlay which shows which prayer to click.
	 * @param graphics
	 * @param client
	 * @param prayer
	 * @param color
	 */
	public static void renderPrayerOverlay(Graphics2D graphics, Client client, Prayer prayer, Color color) {
		Widget widget;

		if(prayer.name().equals("PROTECT_FROM_MAGIC")) {
			widget = client.getWidget(35454997);
		} else {
			widget = client.getWidget(35454998);
		}

		// Only render this overlay if the player is currently looking at their prayer tab.
		if (widget != null && client.getVarbitValue(VarClientInt.INVENTORY_TAB) != InterfaceID.PRAYER) {
			Rectangle bounds = widget.getBounds();
			renderPolygon(graphics, rectangleToPolygon(bounds), color);
		}
	}

	/**
	 * Converts a rectangle to a Polygon object.
	 * @param rect Rectangle object
	 * @return Polygon
	 */
	private static Polygon rectangleToPolygon(Rectangle rect) {
		int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
		int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};

		return new Polygon(xpoints, ypoints, 4);
	}

	/**
	 * Renders text at a given location on the canvas.
	 * @param graphics
	 * @param txtString
	 * @param fontSize
	 * @param fontStyle
	 * @param fontColor
	 * @param canvasPoint
	 * @param shadows
	 * @param yOffset
	 */
	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset) {
		graphics.setFont(new Font("Arial", fontStyle, fontSize));
		if (canvasPoint != null) {
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX(),
				canvasPoint.getY() + yOffset);
			final Point canvasCenterPoint_shadow = new Point(
				canvasPoint.getX() + 1,
				canvasPoint.getY() + 1 + yOffset);
			if (shadows) {
				renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
			}
			renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
		}
	}

	public static void renderTextLocation(Graphics2D graphics, Point txtLoc, String text, Color color) {
		if (Strings.isNullOrEmpty(text)) {
			return;
		}

		int x = txtLoc.getX();
		int y = txtLoc.getY();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);

		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
