# A Town Called Falter

... is a social deduction game similar to Mafia or [Werewolf](https://en.wikipedia.org/wiki/Werewolf_(social_deduction_game)). It's an homage to my 5th Dev10 cohort, who played Werewolf with fire and gusto.

## Gameplay

See [Werewolf](https://en.wikipedia.org/wiki/Werewolf_(social_deduction_game)) for basics. This technology-aided version is not meant to replace the experience of being in a room together and reading social cues. It's designed to reduce the ceremony (_heads down, wake up, who do you want to...?_) that can lead to unintended cheating and confusion. At night, each player must respond to a prompt, even if they don't have special powers. In the ideal situation, this allows people to play heads-up, with no one the wiser.

### House Rules

1. Ballots are secret. (planning to make configurable)
2. Roles are never revealed.
3. Nominations are formal and can backfire. Some roles affect you when you nominate them.
4. A nomination is also a yes-to-execute vote.
5. A player can be nominated only once during the day.
6. A player can only nominate once during the day.

## Roles

- **Villager**: no special abilities
- **Wolf**: kill at night
- **Seer**: ascertain another player's alignment, just remember that you're not always right
- **Cur**: appears evil, but it good
- **Psychiatrist**: ascertain if another player's perceived and actual alignment are the same (are they conflicted?)
- **Guardian**: protect one player each night, can't repeat players and can't protect yourself

## Technology

* Spring MVC @RestController for creating and joining games
* WebSockets for game play communication
* static HTML, CSS, and JavaScript for the UI