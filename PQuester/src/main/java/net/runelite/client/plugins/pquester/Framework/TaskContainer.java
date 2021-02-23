package net.runelite.client.plugins.pquester.Framework;
import java.util.List;

public interface TaskContainer {
    Task getTask();
    void addTask(Task t);
    List<Task> getTasks();
}
