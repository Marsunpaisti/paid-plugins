package net.runelite.client.plugins.pquester.Framework;

import net.runelite.api.coords.WorldPoint;

import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class Task {
    private List<BooleanSupplier> completionConditions;
    private List<BooleanSupplier> entryConditions;
    private boolean markedComplete;
    private boolean markedFailed;

    public abstract String name();
    public abstract boolean execute();

    public void addEntryCondition(BooleanSupplier condition){
        entryConditions.add(condition);
    }

    public void addCompletionCondition(BooleanSupplier condition){
        completionConditions.add(condition);
    }

    public boolean canBegin() {
        if (entryConditions.size() == 0) {
            return true;
        }
        return entryConditions.stream().allMatch(BooleanSupplier::getAsBoolean);
    };

    public void markAsCompleted(){
        this.markedComplete = true;
    }

    public void markAsFailed(){
        this.markedFailed = true;
    }

    public boolean isCompleted() {
        this.markedComplete = this.markedComplete ||
                (completionConditions.size() > 0 &&
                completionConditions
                .stream()
                .allMatch(BooleanSupplier::getAsBoolean));
        return this.markedComplete;
    };

    public boolean isFailed(){
        return this.markedFailed;
    };
}
