# Project TODO List - Unimplemented Methods

This document lists the classes and their respective methods that currently act as "skeletons" (e.g., they return default values like `false`, `null`, `0`, or have empty bodies) and need to be fully implemented with game logic.

## Package: `it.polimi.ingsw.model.player`

### `Player.java`
* [x] `Player()` (Constructor): Needs to initialize `cards`, `stats` (instantiate `PlayerStats`), `nickname`, and `id`.
* [x] `getIsChoosing()`: Currently returns `false`.
* [x] `getId()`: Currently returns `0`.
* [x] `getNickname()`: Currently returns `null` (if not set in constructor).
* [x] `getFood()`: Currently returns `0` instead of `this.food`.
* [x] `getCards()`: Currently returns `null`.
* [x] `getStats()`: Currently returns `null`.
* [x] `addCard(Card c)`: Currently returns `false` and does not add the card to the list.
* [x] `payFood(int amount)`: Currently returns `false` and does not subtract food.
* [x] `addPP(int amount)`: Currently returns `false` and does not add PP.
* [x] `payPP(int amount)`: Currently returns `true` but does not subtract PP.

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
* [x] `CardSet.java` -> `executeEffect(Player p, GameState state)`
* [x] `EndBuilderPpMultiplier.java` -> `executeEffect(Player p, GameState state)`
* [x] `EndCardSet.java` -> `executeEffect(Player p, GameState state)`
* [x] `InventorPair.java` -> `executeEffect(Player p, GameState state)`
* [x] `SustainmentBoost.java` -> `executeEffect(Player p, GameState state)`
* [x] `EndOfTurnExtraPick.java` -> `executeEffect(Player p, GameState state)`
* [x] *(Check any other newly created effects in this package, such as `AddStars`, `ShamanicWinBoost`, `TotemPlacementBonus`, `ShamanicLossProtection` as they are likely skeletons too).*

## Package: `it.polimi.ingsw.controller`

### `GameController.java`
Assuming this class exists as per UML, the main game loop and phase transitions need implementation:
* [ ] `createGame(int playerCount)`
* [ ] `firstOrder()`
* [ ] `triggerPhase()`
* [ ] `nextPhase()`
* [ ] `nextTurn()`
* [ ] `nextAge()`
