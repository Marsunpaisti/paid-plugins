package net.runelite.client.plugins.pfighteraio.states;

import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

import java.util.ArrayList;
import java.util.List;

public abstract class State {
    PFighterAIO plugin;
    PFighterAIOSettings settings;
    List<State> subStates = new ArrayList<State>();
    String chainedName;
    public State(PFighterAIO plugin, PFighterAIOSettings settings){
        this.plugin = plugin;
        this.settings = settings;
        this.chainedName = getName();
    }

    public State getValidState(){
        for (State s : subStates) {
            if (s.condition()) return s;
        }

        return null;
    }
    public abstract boolean condition();
    public abstract String getName();

    public synchronized String chainedName(){
        return this.chainedName;
    }

    public void loop(){
        if (getValidState() != null) {
            synchronized (this) {
                chainedName = getName() + " > " +  getValidState().chainedName();
            }
            getValidState().loop();
            return;
        }
    };
}
