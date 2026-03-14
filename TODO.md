# Project TODO List - Unimplemented Methods

This document lists the classes and their respective methods that currently act as "skeletons" (e.g., they return default values like `false`, `null`, `0`, or have empty bodies) and need to be fully implemented with game logic.

## Package: `it.polimi.ingsw.model.player`

### `Player.java`
* [ ] `Player()` (Constructor): Needs to initialize `cards`, `stats` (instantiate `PlayerStats`), `nickname`, and `id`.
* [ ] `getIsChoosing()`: Currently returns `false`.
* [ ] `getId()`: Currently returns `0`.
* [ ] `getNickname()`: Currently returns `null` (if not set in constructor).
* [ ] `getFood()`: Currently returns `0` instead of `this.food`.
* [ ] `getCards()`: Currently returns `null`.
* [ ] `getStats()`: Currently returns `null`.
* [ ] `addCard(Card c)`: Currently returns `false` and does not add the card to the list.
* [ ] `payFood(int amount)`: Currently returns `false` and does not subtract food.
* [ ] `addPP(int amount)`: Currently returns `false` and does not add PP.
* [ ] `payPP(int amount)`: Currently returns `true` but does not subtract PP.

## Package: `it.polimi.ingsw.model.game`

### `GameState.java`
* [ ] `GameState()` (Constructor): Needs to initialize lists (`players`, `turnOrder`), `deck`, `board`, etc.
* [ ] `getAge()`: Currently returns `null`.
* [ ] `getTurn()`: Currently returns `0`.
* [ ] `getPhase()`: Currently returns `null`.
* [ ] `getPlayer(int index)`: Currently returns `null`.
* [ ] `setAge(Age age)`: Currently returns `false`.
* [ ] `setTurn(int turn)`: Currently returns `false`.
* [ ] `setPhase(GamePhase phase)`: Currently returns `false`.
* [ ] `getCurrentPlayer()`: Currently returns `null`.
* [ ] `getTurnOrder()`: Currently returns `null`.

## Package: `it.polimi.ingsw.model.board`

### `Board.java`
* [ ] `Board()` (Constructor): Needs to initialize `tiles` and `orderTile`.
* [ ] `pickTopCard(int index)`: Needs bounds checking (currently might throw `IndexOutOfBoundsException`).
* [ ] `pickBottomCard(int index)`: Needs bounds checking.
* [ ] `pickTopBuilding(int index)`: Needs bounds checking.
* [ ] `pickBottomBuilding(int index)`: Needs bounds checking.

## Package: `it.polimi.ingsw.model.effects.contextual`

Several contextual effects currently have skeleton `executeEffect` methods that need their logic implemented according to the game rules:
* [ ] `CardSet.java` -> `executeEffect(Player p, GameState state)`
* [ ] `EndBuilderPpMultiplier.java` -> `executeEffect(Player p, GameState state)`
* [ ] `EndCardSet.java` -> `executeEffect(Player p, GameState state)`
* [ ] `InventorPair.java` -> `executeEffect(Player p, GameState state)`
* [ ] `SustainmentBoost.java` -> `executeEffect(Player p, GameState state)`
* [ ] `EndOfTurnExtraPick.java` -> `executeEffect(Player p, GameState state)`
* [ ] *(Check any other newly created effects in this package, such as `AddStars`, `ShamanicWinBoost`, `TotemPlacementBonus`, `ShamanicLossProtection` as they are likely skeletons too).*

## Package: `it.polimi.ingsw.controller`

### `GameController.java`
Assuming this class exists as per UML, the main game loop and phase transitions need implementation:
* [ ] `createGame(int playerCount)`
* [ ] `firstOrder()`
* [ ] `triggerPhase()`
* [ ] `nextPhase()`
* [ ] `nextTurn()`
* [ ] `nextAge()`
