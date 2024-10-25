
package com.hydra;

import java.awt.Color;
import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("alchemicalhydra")
public interface AlchemicalHydraConfig extends Config {
	// Sections
	@ConfigSection(
		name = "General",
		description = "",
		position = 0
	)
	String general = "General";

	@ConfigSection(
		name = "Special Attacks",
		description = "",
		position = 1
	)
	String specialAttacks = "Special Attacks";

	@ConfigSection(
		name = "Misc",
		description = "",
		position = 2
	)
	String misc = "Misc";

	// General
	@ConfigItem(
		keyName = "hydraImmunityOutline",
		name = "Hydra immunity outline",
		description = "Overlay the hydra with a colored outline while it has immunity/not weakened.",
		position = 0,
		section = general
	)
	default boolean hydraImmunityOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fountainOutline",
		name = "Fountain occupancy outline",
		description = "Overlay fountains with a colored outline indicating if the hydra is standing on it.",
		position = 1,
		section = general
	)
	default boolean fountainOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fountainTicks",
		name = "Fountain Ticks",
		description = "Overlay fountains with the ticks until the fountain activates.",
		position = 2,
		section = general
	)
	default boolean fountainTicks()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hidePrayerOnSpecial",
		name = "Hide prayer on special attack",
		description = "Hide prayer overlay during special attacks."
			+ "<br>This can help indicate when to save prayer points.",
		position = 8,
		section = general
	)
	default boolean hidePrayerOnSpecial()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showHpUntilPhaseChange",
		name = "Show HP until phase change",
		description = "Overlay hydra with hp remaining until next phase change.",
		position = 9,
		section = general
	)
	default boolean showHpUntilPhaseChange() {
		return false;
	}

	@ConfigItem(
		keyName = "lightningOutline",
		name = "Lightning outline",
		description = "Overlay lightning tiles with a colored outline.",
		position = 0,
		section = specialAttacks
	)
	default boolean lightningOutline()
	{
		return true;
	}

	@Range(
		min = 1,
		max = 8
	)
	@ConfigItem(
		name = "Outline width",
		description = "Change the stroke width of the lightning tile outline.",
		position = 1,
		keyName = "lightningStroke",
		section = specialAttacks
	)
	@Units(Units.PIXELS)
	default int lightningStroke()
	{
		return 2;
	}

	@Alpha
	@ConfigItem(
		name = "Outline color",
		description = "Change the tile outline color of lightning.",
		position = 2,
		keyName = "lightningOutlineColor",
		section = specialAttacks
	)
	default Color lightningOutlineColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		name = "Outline fill color",
		description = "Change the tile fill color of lightning.",
		position = 3,
		keyName = "lightningFillColor",
		section = specialAttacks
	)
	default Color lightningFillColor()
	{
		return new Color(20, 220, 220, 98);
	}

	@ConfigItem(
		keyName = "poisonOutline",
		name = "Poison outline",
		description = "Overlay poison tiles with a colored outline.",
		position = 4,
		section = specialAttacks
	)
	default boolean poisonOutline()
	{
		return false;
	}

	@Range(
		min = 1,
		max = 8
	)
	@ConfigItem(
		name = "Outline width",
		description = "Change the stroke width of the poison tile outline.",
		position = 5,
		keyName = "poisonStroke",
		section = specialAttacks
	)
	@Units(Units.PIXELS)
	default int poisonStroke()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonOutlineColor",
		name = "Outline color",
		description = "Outline color of poison area tiles.",
		position = 6,
		section = specialAttacks
	)
	default Color poisonOutlineColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
		keyName = "poisonFillColor",
		name = "Outline fill color",
		description = "Fill color of poison area tiles.",
		position = 7,
		section = specialAttacks
	)
	default Color poisonFillColor()
	{
		return new Color(18, 227, 61, 197);
	}

	// Misc
	@Alpha
	@ConfigItem(
		keyName = "safeColor",
		name = "Safe color",
		description = "Color indicating there are at least two hydra attacks pending.",
		position = 0,
		section = misc
	)
	default Color safeColor()
	{
		return new Color(0, 150, 0, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "warningColor",
		name = "Warning color",
		description = "Color indicating there is one hydra attack pending.",
		position = 1,
		section = misc
	)
	default Color warningColor()
	{
		return new Color(200, 150, 0, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "dangerColor",
		name = "Danger color",
		description = "Color indiciating the hydra will change attacks.",
		position = 2,
		section = misc
	)
	default Color dangerColor()
	{
		return new Color(150, 0, 0, 150);
	}
}
