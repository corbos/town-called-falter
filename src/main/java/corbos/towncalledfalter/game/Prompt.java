package corbos.towncalledfalter.game;

public class Prompt {

    private final String prompt;
    private final Ability ability;
    private final int count;
    private boolean canSelectSelf;
    private boolean dismissable;

    private int version;

    public Prompt(String prompt, Ability ability, int count) {
        this.prompt = prompt;
        this.ability = ability;
        this.count = count;
    }

    public String getPrompt() {
        return prompt;
    }

    public Ability getAbility() {
        return ability;
    }

    public int getCount() {
        return count;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setCanSelectSelf(boolean canSelectSelf) {
        this.canSelectSelf = canSelectSelf;
    }

    public boolean getCanSelectSelf() {
        return this.canSelectSelf;
    }

    public boolean isDismissable() {
        return dismissable;
    }

    public void setDismissable(boolean dismissable) {
        this.dismissable = dismissable;
    }

}
