package net.runelite.client.plugins.pfighteraio;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.PWidgets;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Singleton
public class PFighterAIOOverlayMinimap extends Overlay
{
	private final Client client;
	private final PFighterAIO plugin;
	private final PFighterAIOConfig config;

	@Inject
	private PFighterAIOOverlayMinimap(final Client client, final PFighterAIO plugin, final PFighterAIOConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableOverlay()) return null;
		if (plugin.settings == null) return null;

		renderInfoBox(graphics);

		if (plugin.settings.getSearchRadiusCenter() != null){
			renderFightArea(graphics);
		}
		return null;
	}

	public void renderInfoBox(Graphics2D graphics){
		if (!plugin.isRunning()) return;
		if (plugin.settings.getStartedTimestamp() == null) return;
		Widget base = null;
		if (PWidgets.isSubstantiated(WidgetInfo.CHATBOX)){
			base = PWidgets.get(WidgetInfo.CHATBOX_PARENT);
		} else {
			base = PWidgets.get(WidgetInfo.CHATBOX_BUTTONS);
		}
		if (base == null) return;
		double topOfWidget  = base.getBounds().getY();
		double infoBoxHeight = 40;
		double infoBoxWidth = base.getBounds().getWidth();
		double drawPos = topOfWidget - infoBoxHeight;
		graphics.setColor(Color.white);
		graphics.fillRect(0, (int)drawPos, (int)infoBoxWidth, (int)infoBoxHeight);
		graphics.setColor(Color.black);
		graphics.setFont(new Font("Arial Bold", Font.PLAIN, 18));
		int rowHeight = 18;
		int currentRowPos = (int)drawPos + 16;
		int horizontalPos1 = (int)base.getBounds().getX() + 3;
		int horizontalPos2 = (int)base.getBounds().getX() + 3 + (int)infoBoxWidth/2;
		long d = Duration.between(plugin.settings.getStartedTimestamp(), Instant.now()).getSeconds();
		String runTimeStr = String.format("%d:%02d:%02d", d / 3600, (d % 3600) / 60, (d % 60));
		graphics.drawString("Runtime: " + runTimeStr, horizontalPos1, currentRowPos);
		graphics.drawString("State: " + plugin.settings.getCurrentStateName(), horizontalPos2, currentRowPos);
		currentRowPos += rowHeight;
		//graphics.drawString("Runtime: " + runTimeStr, horizontalPos1, currentRowPos);
		//currentRowPos += rowHeight;
	}

	public void renderFightArea(Graphics2D graphics)
	{
		int radius = plugin.settings.getSearchRadius()*4;
		LocalPoint lp = LocalPoint.fromWorld(PUtils.getClient(), plugin.settings.getSearchRadiusCenter());
		if (lp == null) return;
		Point mini = Perspective.localToMinimap(PUtils.getClient(), lp);
		if (mini == null) return;
		graphics.setColor(new Color(66, 254, 254, 50));
		graphics.fillOval(mini.getX() - radius, mini.getY() - radius, radius*2, radius*2);
		graphics.setColor(new Color(66, 254, 254, 155));
		graphics.drawOval(mini.getX() - radius, mini.getY() - radius, radius*2, radius*2);
	}
}