package com.hydra.overlay;

import java.awt.*;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.hydra.AlchemicalHydraConfig;
import com.hydra.AlchemicalHydraPlugin;
import com.hydra.entity.Hydra;
import com.hydra.entity.HydraPhase;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.getCanvasTileAreaPoly;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Singleton
public class SceneOverlay extends Overlay {
	private static final int LIGHTNING_ID = 1666;

	private static final Area POISON_AREA = new Area();

	private static final int POISON_AOE_AREA_SIZE = 3;

	private static final int HYDRA_HULL_OUTLINE_STROKE_SIZE = 1;

	private final Client client;
	private final AlchemicalHydraPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private Hydra hydra;

	@Inject
	private AlchemicalHydraConfig config;

	@Inject
	public SceneOverlay(final Client client, final AlchemicalHydraPlugin plugin,
						final ModelOutlineRenderer modelOutlineRenderer) {
		this.client = client;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(final Graphics2D graphics2D) {
		hydra = plugin.getHydra();

		if (hydra == null) {
			return null;
		}

		renderHpUntilPhaseChange(graphics2D);
		renderHydraImmunityOutline();
		renderPoisonProjectileAreaTiles(graphics2D);
		renderLightning(graphics2D);
		renderFountainOutline(graphics2D);
		renderFountainTicks(graphics2D);

		return null;
	}

	/**
	 * Renders the area affected by the Hydra's poison attacks in the first and final phases.
	 * @param graphics2D
	 */
	private void renderPoisonProjectileAreaTiles(final Graphics2D graphics2D) {
		final Map<LocalPoint, Projectile> poisonProjectiles = plugin.getPoisonProjectiles();

		if (!config.poisonOutline() || poisonProjectiles.isEmpty()) {
			return;
		}

		POISON_AREA.reset();

		for (final Map.Entry<LocalPoint, Projectile> entry : poisonProjectiles.entrySet()) {
			if (entry.getValue().getEndCycle() < client.getGameCycle()) {
				continue;
			}

			final LocalPoint localPoint = entry.getKey();

			final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, POISON_AOE_AREA_SIZE);

			if (polygon != null) {
				POISON_AREA.add(new Area(polygon));
			}
		}

		drawOutlineAndFill(graphics2D, config.poisonOutlineColor(), config.poisonFillColor(), config.poisonStroke(), POISON_AREA);
	}

	/**
	 * Renders an outline and fill on the tile where the lightning is currently.
	 * @param graphics2D
	 */
	private void renderLightning(final Graphics2D graphics2D) {
		final Deque<GraphicsObject> graphicsObjects = client.getTopLevelWorldView().getGraphicsObjects();

		if (!config.lightningOutline()) {
			return;
		}

		if (hydra.getPhase() != HydraPhase.LIGHTNING) {
			return;
		}

		for (final GraphicsObject graphicsObject : graphicsObjects) {
			if (graphicsObject.getId() != LIGHTNING_ID) {
				continue;
			}

			final LocalPoint localPoint = graphicsObject.getLocation();

			if (localPoint == null) {
				return;
			}

			final Polygon polygon = Perspective.getCanvasTilePoly(client, localPoint);

			if (polygon == null) {
				return;
			}

			drawOutlineAndFill(graphics2D, config.lightningOutlineColor(), config.lightningFillColor(),
				config.lightningStroke(), polygon);
		}
	}

	/**
	 * Renders an outline around the Hydra when it is immune to damage and must be lured underneath a fountain.
	 */
	private void renderHydraImmunityOutline()
	{
		final NPC npc = hydra.getNpc();

		if (!config.hydraImmunityOutline() || !hydra.isImmunity() || npc == null || npc.isDead())
		{
			return;
		}

		final WorldPoint fountainWorldPoint = hydra.getPhase().getFountainWorldPoint();

		if (fountainWorldPoint != null) {
			final Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(client.getTopLevelWorldView(), fountainWorldPoint);

			if (fountainWorldPoints.size() == 1) {
				WorldPoint worldPoint = null;

				for (final WorldPoint wp : fountainWorldPoints) {
					worldPoint = wp;
				}

				final LocalPoint localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), worldPoint);

				if (localPoint != null) {
					final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, 3);

					if (polygon != null) {
						int stroke = HYDRA_HULL_OUTLINE_STROKE_SIZE;

						if (npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1))) {
							stroke++;
						}

						modelOutlineRenderer.drawOutline(npc, stroke, hydra.getPhase().getPhaseColor(), 0);
						return;
					}
				}
			}

		}

		modelOutlineRenderer.drawOutline(npc, HYDRA_HULL_OUTLINE_STROKE_SIZE, hydra.getPhase().getPhaseColor(), 0);
	}

	/**
	 * Renders an outline around the fountains which weaken or strengthen hydra.
	 * @param graphics2D
	 */
	private void renderFountainOutline(final Graphics2D graphics2D) {
		final NPC npc = hydra.getNpc();
		final WorldPoint fountainWorldPoint = hydra.getPhase().getFountainWorldPoint();

		if (!config.fountainOutline() || !hydra.isImmunity() || fountainWorldPoint == null || npc == null || npc.isDead()) {
			return;
		}

		final Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(client.getTopLevelWorldView(), fountainWorldPoint);

		if (fountainWorldPoints.size() != 1) {
			return;
		}

		WorldPoint worldPoint = null;

		for (final WorldPoint wp : fountainWorldPoints) {
			worldPoint = wp;
		}

		final LocalPoint localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), worldPoint);

		if (localPoint == null) {
			return;
		}

		final Polygon polygon = getCanvasTileAreaPoly(client, localPoint, 3);

		if (polygon == null) {
			return;
		}

		Color color = hydra.getPhase().getFountainColor();

		if (!npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1))) {
			color = color.darker();
		}

		drawOutlineAndFill(graphics2D, color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 30), 1, polygon);
	}

	/**
	 * Renders text on the canvas indicating the number of ticks remaining until the fountain will spurt next.
	 * @param graphics2D
	 */
	private void renderFountainTicks(final Graphics2D graphics2D) {
		if (!config.fountainTicks()) {
			return;
		}

		final Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(client.getTopLevelWorldView(), HydraPhase.POISON.getFountainWorldPoint());
		fountainWorldPoints.addAll(WorldPoint.toLocalInstance(client.getTopLevelWorldView(), HydraPhase.LIGHTNING.getFountainWorldPoint()));
		fountainWorldPoints.addAll(WorldPoint.toLocalInstance(client.getTopLevelWorldView(), HydraPhase.FLAME.getFountainWorldPoint()));

		if (fountainWorldPoints.isEmpty()) {
			return;
		}

		WorldPoint worldPoint;

		for (final WorldPoint wp : fountainWorldPoints) {
			worldPoint = wp;
			final LocalPoint localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), worldPoint);

			if (localPoint == null)
			{
				return;
			}


			final String text = String.valueOf(plugin.getFountainTicks());
			Point timeLoc = Perspective.getCanvasTextLocation(client, graphics2D, localPoint, text, graphics2D.getFontMetrics().getHeight());


			OverlayUtil.renderTextLocation(
				graphics2D,
				text,
				16,
				Font.BOLD,
				new Color(255, 255, 255, 255),
				timeLoc,
				true,
				0
			);
		}


	}

	/**
	 * Renders text on the hydra which indicates the amount of HP remaining until it changes phases.
	 * @param graphics2D
	 */
	private void renderHpUntilPhaseChange(final Graphics2D graphics2D) {
		final NPC npc = hydra.getNpc();

		if (!config.showHpUntilPhaseChange() || npc == null || npc.isDead()) {
			return;
		}

		final int hpUntilPhaseChange = hydra.getHpUntilPhaseChange();

		if (hpUntilPhaseChange == 0) {
			return;
		}

		final String text = String.valueOf(hpUntilPhaseChange);

		final Point point = npc.getCanvasTextLocation(graphics2D, text, 0);

		if (point == null) {
			return;
		}

		OverlayUtil.renderTextLocation(
			graphics2D,
			text,
			16,
			Font.BOLD,
			new Color(255, 255, 255, 255),
			point,
			true,
			0
		);
	}

	private static void drawOutlineAndFill(final Graphics2D graphics2D, final Color outlineColor, final Color fillColor, final float strokeWidth, final Shape shape) {
		final Color originalColor = graphics2D.getColor();
		final Stroke originalStroke = graphics2D.getStroke();

		graphics2D.setStroke(new BasicStroke(strokeWidth));
		graphics2D.setColor(outlineColor);
		graphics2D.draw(shape);

		graphics2D.setColor(fillColor);
		graphics2D.fill(shape);

		graphics2D.setColor(originalColor);
		graphics2D.setStroke(originalStroke);
	}
}
