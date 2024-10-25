
package com.hydra.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.hydra.overlay.AttackOverlay;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.SpriteID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

import static com.hydra.AlchemicalHydraPlugin.*;


@Getter
@RequiredArgsConstructor
public enum HydraPhase
{

	POISON(3, HYDRA_1_1, HYDRA_1_2, 1644, 0, 825, SpriteID.SPELL_CLAWS_OF_GUTHIX, new WorldPoint(1371, 10263, 0), Color.GREEN, Color.RED),
	LIGHTNING(3, HYDRA_2_1, HYDRA_2_2, 0, HYDRA_LIGHTNING, 550, SpriteID.SPELL_ENERGY_TRANSFER, new WorldPoint(1371, 10272, 0), Color.CYAN, Color.GREEN),
	FLAME(3, HYDRA_3_1, HYDRA_3_2, 0, HYDRA_FIRE, 275, SpriteID.SPELL_SUPERHEAT_ITEM, new WorldPoint(1362, 10272, 0), Color.RED, Color.CYAN),
	ENRAGED(1, HYDRA_4_1, HYDRA_4_2, 1644, 0, 0, SpriteID.SPELL_CLAWS_OF_GUTHIX, null, null, null);

	private final int attacksPerSwitch;
	private final int deathAnimation1;
	private final int deathAnimation2;
	private final int specialProjectileId;
	private final int specialAnimationId;
	private final int hpThreshold;

	@Getter(AccessLevel.NONE)
	private final int spriteId;

	private final WorldPoint fountainWorldPoint;

	private final Color phaseColor;
	private final Color fountainColor;

	private BufferedImage specialImage;

	public BufferedImage getSpecialImage(final SpriteManager spriteManager) {
		if (specialImage == null) {
			final BufferedImage tmp = spriteManager.getSprite(spriteId, 0);
			specialImage = tmp == null ? null : ImageUtil.resizeImage(tmp, AttackOverlay.IMAGE_SIZE, AttackOverlay.IMAGE_SIZE);
		}

		return specialImage;
	}
}
