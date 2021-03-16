package net.runelite.client.plugins.pfighteraio;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.paistisuite.api.PGroundItems;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.WebWalkerDebugRenderer;
import net.runelite.client.plugins.paistisuite.api.types.PGroundItem;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class PFighterAIOOverlay extends Overlay
{
	private final Client client;
	private final PFighterAIO plugin;
	private final PFighterAIOConfig config;

	@Inject
	private PFighterAIOOverlay(final Client client, final PFighterAIO plugin, final PFighterAIOConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableOverlay()) return null;
		if (plugin.settings == null) return null;

		/*
		List<PGroundItem> loot = PGroundItems.getGroundItems();
		for (PGroundItem i : loot){
			if (i.getSpawnTime() == null) continue;
			int dur = (int)Duration.between(i.getSpawnTime(), Instant.now()).getSeconds();
			LocalPoint itemLocalPt = LocalPoint.fromWorld(PUtils.getClient(), i.getLocation());
			if (itemLocalPt == null) continue;
			Polygon p = Perspective.getCanvasTilePoly(PUtils.getClient(), itemLocalPt);
			if (p == null || p.getBounds() == null) continue;
			Point center = new Point((int)p.getBounds().getCenterX(), (int)p.getBounds().getCenterY());
			OverlayUtil.renderTextLocation(graphics, center, "" + i.getPricePerSlot(), Color.green);
		}*/

		WebWalkerDebugRenderer.render(graphics);

		if (plugin.settings.getSearchRadiusCenter() != null){
			drawTile(graphics, plugin.settings.getSearchRadiusCenter(), new Color(66, 254, 254, 35), new Color(66, 254, 254, 120));
		}
		if (plugin.settings.getSafeSpot() != null){
			drawTile(graphics, plugin.settings.getSafeSpot(), new Color(0, 255, 0, 35), new Color(0, 255, 0, 120));
		}
		if (plugin.settings.getCannonTile() != null){
			drawTile(graphics, plugin.settings.getCannonTile(), new Color(255, 195, 15, 35), new Color(255, 195, 15, 120));
		}

		if (plugin.settings.getValidTargetFilter() != null && plugin.settings.getSearchRadiusCenter() != null){
			java.util.List<NPC> validTargets = plugin.getValidTargets();
			if (validTargets != null && validTargets.size() > 0){
				for (NPC n : validTargets){
					highlightNpc(graphics, n, new Color(66, 254, 254, 35), new Color(66, 254, 254, 120));
					//OverlayUtil.renderActorTextOverlay(graphics, n, "" + plugin.pathFindDistance(n.getWorldLocation()), new Color(255, 0, 0));
				}
			}
		}
		return null;
	}

	private void highlightNpc(Graphics2D graphics, NPC npc, Color fillColor, Color borderColor){
		Shape hull = npc.getConvexHull();
		if (hull == null) return;
		Color originalColor = graphics.getColor();
		Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(2));
		graphics.setColor(fillColor);
		graphics.fill(hull);
		graphics.setColor(borderColor);
		graphics.draw(hull);
		graphics.setStroke(originalStroke);
		graphics.setColor(originalColor);
	}

	private void drawTile(Graphics2D graphics, WorldPoint tile, Color fillColor, Color borderColor)
	{
		if (tile.getPlane() != client.getPlane())
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, tile);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly == null)
		{
			return;
		}
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(fillColor);
		graphics.fillPolygon(poly);
		graphics.setColor(borderColor);
		graphics.drawPolygon(poly);
		graphics.setStroke(originalStroke);
	}
}