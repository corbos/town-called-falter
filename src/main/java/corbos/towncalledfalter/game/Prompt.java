package corbos.towncalledfalter.game;

public class Prompt {

    private final String prompt;
    private final Ability ability;
    private final int count;
    private boolean selectSelf;
    private boolean dismissable;

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

    public boolean canSelectSelf() {
        return selectSelf;
    }

    public void setSelectSelf(boolean selectSelf) {
        this.selectSelf = selectSelf;
    }

    public boolean isDismissable() {
        return dismissable;
    }

    public void setDismissable(boolean dismissable) {
        this.dismissable = dismissable;
    }

}
