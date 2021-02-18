package net.runelite.client.plugins.aiofighterpro;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Slf4j
@Singleton
public class AIOFighterProOverlay extends Overlay
{
	private final Client client;
	private final AIOFighterPro plugin;
	private final AIOFighterProConfig config;

	@Inject
	private AIOFighterProOverlay(final Client client, final AIOFighterPro plugin, final AIOFighterProConfig config)
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


		if (plugin.enemiesToTarget != null && plugin.searchRadiusCenter != null){
			/*
			for (int dx = -plugin.searchRadius; dx <= plugin.searchRadius; dx++){
				for (int dy = -plugin.searchRadius; dy <= plugin.searchRadius; dy++){
					WorldPoint p = new WorldPoint(
							plugin.searchRadiusCenter.getX() + dx,
							plugin.searchRadiusCenter.getY() + dy,
							   plugin.searchRadiusCenter.getPlane());

					log.info(p.toString());
					if (p.isInScene(PUtils.getClient()) && p.distanceToHypotenuse(plugin.searchRadiusCenter) <= (double)plugin.searchRadius) {
						drawTile(graphics, p, new Color(50, 50, 254, 25));
					}
				}
			}

			 */

			/*
			NPC target = plugin.fightEnemiesState.getNewTarget();
			if (target != null) {
				highlightNpc(graphics, target, new Color(150, 0, 0, 35), new Color(150, 0, 0, 120));
			}
			*/

			java.util.List<NPC> validTargets = plugin.getValidTargets();
			if (validTargets != null){
				for (NPC n : validTargets){
					//if (target != null && n.equals(target)) continue;
					highlightNpc(graphics, n, new Color(66, 254, 254, 35), new Color(66, 254, 254, 120));
				}
			}
		}


		return null;
	}

	private void highlightNpc(Graphics2D graphics, NPC npc, Color fillColor, Color borderColor){
		Shape hull = npc.getConvexHull();
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

	private void drawTile(Graphics2D graphics, WorldPoint tile, Color color)
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
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(color);
		graphics.fillPolygon(poly);
		graphics.setStroke(originalStroke);
	}
}