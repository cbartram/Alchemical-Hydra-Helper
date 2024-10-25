
package com.hydra;

import com.google.inject.Provides;

import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.hydra.entity.Hydra;
import com.hydra.entity.HydraPhase;
import com.hydra.overlay.AttackOverlay;
import com.hydra.overlay.PrayerOverlay;
import com.hydra.overlay.SceneOverlay;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
@PluginDescriptor(
	name = "Alchemical Hydra",
	enabledByDefault = false,
	description = "Tracks Prayers and specials for the Alchemical Hydra.",
	tags = {"alchemical", "hydra", "alch", "boss", "pvm", "slayer"}
)
public class AlchemicalHydraPlugin extends Plugin
{
	private static final String MESSAGE_NEUTRALIZE = "The chemicals neutralise the Alchemical Hydra's defences!";
	private static final String MESSAGE_STUN = "The Alchemical Hydra temporarily stuns you.";
	private static final int[] HYDRA_REGIONS = {5279, 5280, 5535, 5536};

	@Inject
	private Client client;
	
	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AttackOverlay attackOverlay;

	@Inject
	private SceneOverlay sceneOverlay;

	@Inject
	private PrayerOverlay prayerOverlay;

	private boolean atHydra;

	@Getter
	private Hydra hydra;

	public static final int HYDRA_1_1 = 8232;
	public static final int HYDRA_1_2 = 8233;
	public static final int HYDRA_LIGHTNING = 8241;
	public static final int HYDRA_2_1 = 8239;
	public static final int HYDRA_2_2 = 8240;
	public static final int HYDRA_FIRE = 8248;
	public static final int HYDRA_3_1 = 8247;
	public static final int HYDRA_3_2 = 8246;
	public static final int HYDRA_4_1 = 8254;
	public static final int HYDRA_4_2 = 8253; // TODO could be wrong but shouldn't matter as hydra doesn't move in enrage

	@Getter
	int fountainTicks = -1;
	int lastFountainAnim = -1;
	private boolean inCombat = false;

	@Getter
	private final Map<LocalPoint, Projectile> poisonProjectiles = new HashMap<>();

	private int lastAttackTick = -1;

	@Getter
	private final Set<GameObject> vents = new HashSet<>();

	@Provides
	AlchemicalHydraConfig provideConfig(final ConfigManager configManager) {
		return configManager.getConfig(AlchemicalHydraConfig.class);
	}

	@Override
	protected void startUp() {
		if (client.getGameState() == GameState.LOGGED_IN && isInHydraRegion()) {
			init();
		}
	}

	private void init() {
		atHydra = true;

		overlayManager.add(sceneOverlay);
		overlayManager.add(attackOverlay);
		overlayManager.add(prayerOverlay);

		for (final NPC npc : client.getNpcs()) {
			onNpcSpawned(new NpcSpawned(npc));
		}
	}

	@Override
	protected void shutDown() {
		atHydra = false;

		overlayManager.remove(sceneOverlay);
		overlayManager.remove(attackOverlay);
		overlayManager.remove(prayerOverlay);

		hydra = null;
		poisonProjectiles.clear();
		lastAttackTick = -1;
		fountainTicks = -1;
		vents.clear();
		lastFountainAnim = -1;
	}

	@Subscribe
	private void onGameStateChanged(final GameStateChanged event) {
		final GameState gameState = event.getGameState();

		switch (gameState) {
			case LOGGED_IN:
				if (isInHydraRegion()) {
					if (!atHydra) {
						init();
					}
				} else {
					if (atHydra) {
						shutDown();
					}
				}
				break;
			case HOPPING:
			case LOGIN_SCREEN:
				if (atHydra)
				{
					shutDown();
				}
			default:
				break;
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event) {
		if (!isInHydraRegion()) {
			return;
		}
		GameObject gameobject = event.getGameObject();
		int id = gameobject.getId();
		if (id == ObjectID.CHEMICAL_VENT_RED || id == ObjectID.CHEMICAL_VENT_GREEN || id == ObjectID.CHEMICAL_VENT_BLUE) {
			vents.add(gameobject);
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event) {
		GameObject gameobject = event.getGameObject();
		vents.remove(gameobject);
	}


	@Subscribe
	private void onGameTick(final GameTick event) {
		attackOverlay.decrementStunTicks();
		updateVentTicks();
		if(hydra != null) {
			NPC npc = hydra.getNpc();
			if(npc != null) {
				int poseAnim = npc.getPoseAnimation();

				// If the phase is POISON and we see either of pose anims for lightning swap phases. Same goes for
				// the other phases as well.
				switch(hydra.getPhase()) {
					case POISON:
						if(poseAnim == HydraPhase.LIGHTNING.getPoseAnimationIdle() || poseAnim == HydraPhase.LIGHTNING.getPoseAnimationMoving()) {
							System.out.println("Changing phase to: LIGHTNING.");
							hydra.changePhase(HydraPhase.LIGHTNING);
						}
						break;
					case LIGHTNING:
						if(poseAnim == HydraPhase.FLAME.getPoseAnimationIdle() || poseAnim == HydraPhase.FLAME.getPoseAnimationMoving()) {
							System.out.println("Changing phase to: FLAME.");
							hydra.changePhase(HydraPhase.FLAME);
						}
						break;
					case FLAME:
						if(poseAnim == HydraPhase.ENRAGED.getPoseAnimationIdle() || poseAnim == HydraPhase.ENRAGED.getPoseAnimationMoving()) {
							System.out.println("Changing phase to: ENRAGE.");
							hydra.changePhase(HydraPhase.ENRAGED);
						}
						break;
					case ENRAGED:
//						hydra = null;
						if (!poisonProjectiles.isEmpty()) {
							poisonProjectiles.clear();
						}
						break;
				}
			}

			if (!poisonProjectiles.isEmpty()) {
				poisonProjectiles.values().removeIf(p -> p.getEndCycle() < client.getGameCycle());
			}
		}
	}

	/**
	 * Updates the ticks remaining until the fountain spurts water again weakening the Alchemical Hydra.
	 */
	private void updateVentTicks() {
		if (fountainTicks > 0) {
			fountainTicks--;
			if (fountainTicks == 0) {
				fountainTicks = 8;
			}
		}

		if (!vents.isEmpty()) {
			for (final GameObject vent : vents) {
				final DynamicObject dynamicObject = (DynamicObject) vent.getRenderable();
				int animation = dynamicObject.getAnimation().getId();
				if (animation == 8279 && lastFountainAnim == 8280) {
					fountainTicks = 2;
				}
				lastFountainAnim = animation;
				break;
			}
		}

	}


	@Subscribe
	private void onNpcSpawned(final NpcSpawned event) {
		final NPC npc = event.getNpc();

		if (npc.getId() == NpcID.ALCHEMICAL_HYDRA) {
			hydra = new Hydra(npc);
			if (client.isInInstancedRegion() && fountainTicks == -1) {
				fountainTicks = 11;
			}
		}
	}

	@Subscribe
	private void onNpcDespawned(final NpcDespawned event) {
		final NPC npc = event.getNpc();

		if(npc != null) {
			if (Objects.equals(npc.getName(), "Alchemical Hydra")) {
				hydra = null;
				poisonProjectiles.clear();
				lastAttackTick = -1;
				fountainTicks = -1;
				vents.clear();
				lastFountainAnim = -1;
				inCombat = false;
			}
		}
	}


	@Subscribe
	private void onProjectileMoved(final ProjectileMoved event)
	{
		final Projectile projectile = event.getProjectile();

		if (hydra == null || client.getGameCycle() >= projectile.getStartCycle()) {
			return;
		}

		final int projectileId = projectile.getId();

		if (hydra.getPhase().getSpecialProjectileId() == projectileId) {
			if (hydra.getAttackCount() >= hydra.getNextSpecial()) {
				hydra.setNextSpecial();
			}

			poisonProjectiles.put(event.getPosition(), projectile);
		} else if (client.getTickCount() != lastAttackTick && (projectileId == Hydra.AttackStyle.MAGIC.getProjectileID() || projectileId == Hydra.AttackStyle.RANGED.getProjectileID())) {
			hydra.handleProjectile(projectileId);

			lastAttackTick = client.getTickCount();
		}
	}

	@Subscribe
	private void onChatMessage(final ChatMessage event) {
		final ChatMessageType chatMessageType = event.getType();

		if (chatMessageType != ChatMessageType.SPAM && chatMessageType != ChatMessageType.GAMEMESSAGE) {
			return;
		}

		final String message = event.getMessage();

		if (message.equals(MESSAGE_NEUTRALIZE)) {
			clientThread.invokeLater(() -> {
				if(hydra != null) {
					hydra.setImmunity(false);
				}
			});
		} else if (message.equals(MESSAGE_STUN)) {
			attackOverlay.setStunTicks();
		}
	}

	/**
	 * Determines if the player is within the Alchemical Hydra Instance
	 * @return Returns true if the player is in the hydra instance and false otherwise.
	 */
	private boolean isInHydraRegion() {
		return client.isInInstancedRegion() && Arrays.equals(client.getMapRegions(), HYDRA_REGIONS);
	}
}
