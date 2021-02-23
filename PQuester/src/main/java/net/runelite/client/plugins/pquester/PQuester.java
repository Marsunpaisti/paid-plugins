package net.runelite.client.plugins.pquester;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.paistisuite.PScript;
import net.runelite.client.plugins.paistisuite.PaistiSuite;
import org.pf4j.Extension;

import javax.inject.Singleton;

@Extension
@PluginDependency(PaistiSuite.class)
@PluginDescriptor(
        name = "PQuester",
        enabledByDefault = false,
        description = "Completes quests",
        tags = {"npcs", "items", "paisti"}
)

@Slf4j
@Singleton
public class PQuester extends PScript {
    @Override
    protected void loop() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
