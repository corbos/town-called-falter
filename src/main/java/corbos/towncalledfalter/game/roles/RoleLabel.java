package corbos.towncalledfalter.game.roles;

public enum RoleLabel {
    VILLAGER("Villager", "You are a plain old boring Villager. Sorry."),
    SEER("Seer", "The Seer wakes up at night "
            + "and can intuit the alignment of any one player."),
    WEREWOLF("Werewolf", "You are a werewolf. You kill people at night."),
    CUR("Cur", "The Cur is, let's face it, a bit mangy. "
            + "You appear to be EVIL, but you're actually GOOD. "
            + "In fact, you're a very nice person."),
    PSYCHIATRIST("Psychiatrist", "The Psychiatrist diagnoses other players. "
            + "They can tell if a player's perceived alignment matches "
            + "their actual alignment."),
    GUARDIAN("Guardian", "Each night, you protect one player from harm. "
            + "You cannot pick yourself. "
            + "You cannot pick the same player two nights in a row."),
    INNOCENT("The Innocent", "You're incredibly good, pure, and beyond reproach. "
            + "Once per game, if nominated for execution the nominator dies.");

    private final String name;
    private final String description;

    RoleLabel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
