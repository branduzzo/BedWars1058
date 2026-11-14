package it.branduzzo.nextbedwars.api.sidebar.lib;

public class PlayerTab {

    public enum PushingRule {
        NEVER,
        PUSH_OTHER_TEAMS
    }

    public enum NameTagVisibility {
        ALWAYS,
        NEVER,
        HIDE_FOR_OTHER_TEAMS,
        HIDE_FOR_OWN_TEAM
    }

    private final String identifier;
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;

    public PlayerTab(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
    }

    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }
}
