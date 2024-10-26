package com.hydra.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import com.hydra.AlchemicalHydraPlugin;
import com.hydra.entity.Hydra;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class PrayerOverlay extends Overlay {
	private final Client client;
	private final AlchemicalHydraPlugin plugin;

    @Inject
	private PrayerOverlay(final Client client, final AlchemicalHydraPlugin plugin) {
		this.client = client;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(final Graphics2D graphics2D) {
        Hydra hydra = plugin.getHydra();

		if (hydra == null) {
			return null;
		}

		final Prayer prayer = hydra.getNextAttack().getPrayer();
		OverlayUtil.renderPrayerOverlay(graphics2D, client, prayer, prayer == Prayer.PROTECT_FROM_MAGIC ? Color.CYAN : Color.GREEN);

		return null;
	}
}
