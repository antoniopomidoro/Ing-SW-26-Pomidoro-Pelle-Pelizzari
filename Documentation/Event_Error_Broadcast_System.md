# Event/Error Broadcast System

## Obiettivo

Questa documentazione descrive il sistema di notifica eventi/errori implementato nel model con architettura Active MVC.

Requisito applicato:
- l'errore di mossa e uno stato osservabile del model;
- il model notifica sempre prima gli osservatori tramite `GameEvent`;
- subito dopo lancia `IllegalMoveException`;
- il controller intercetta l'eccezione e restituisce `false` senza bloccare il flusso applicativo.

## Nuovi componenti introdotti

### `GameEvent`
Percorso: `src/main/java/it/polimi/ingsw/model/game/GameEvent.java`

Contiene:
- `Type` (enum): `WRONG_TURN`, `INSUFFICIENT_FOOD`, `INVALID_INDEX`, `INVALID_PHASE`;
- `culprit`: player associato all'evento;
- `message`: messaggio descrittivo per UI/logica view.

### `GameStateObserver`
Percorso: `src/main/java/it/polimi/ingsw/model/game/GameStateObserver.java`

Interfaccia funzionale con callback unica:
- `void onGameEvent(GameEvent event)`

### `IllegalMoveException`
Percorso: `src/main/java/it/polimi/ingsw/model/game/IllegalMoveException.java`

Eccezione runtime di dominio usata per segnalare mosse non valide dopo il broadcast dell'evento.

## Estensione di `GameState`

Percorso: `src/main/java/it/polimi/ingsw/model/game/GameState.java`

Aggiunte:
- `private transient List<GameStateObserver> observers = new ArrayList<>();`
- `addObserver(GameStateObserver observer)`
- `raiseEvent(GameEvent event)`

Note:
- la lista observer e `transient` per non serializzare listener runtime;
- `raiseEvent` ignora `null` e notifica tutti gli observer registrati.

## Integrazione nelle fasi di gioco

### `StartTurnPhase`
Percorso: `src/main/java/it/polimi/ingsw/model/game/StartTurnPhase.java`

Nel metodo `occupyOfferTrailTile(...)`, se il player non e quello corrente:
1. `context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player, "It's not your turn to place a totem."));`
2. `throw new IllegalMoveException("It's not your turn to place a totem.");`

### `PlayerTurnPhase`
Percorso: `src/main/java/it/polimi/ingsw/model/game/PlayerTurnPhase.java`

Nei metodi:
- `pickTopCard(...)`
- `pickBottomCard(...)`
- `pickTopBuilding(...)`
- `pickBottomBuilding(...)`

ogni validazione che prima lanciava eccezioni standard ora segue lo schema:
1. `context.raiseEvent(...)`
2. `throw new IllegalMoveException(...)`

Mappatura eventi:
- `"Only the active player can perform picks"` -> `WRONG_TURN`
- `"No upper picks remaining"` / `"No bottom picks remaining"` -> `INVALID_PHASE`
- `"Invalid card index"` -> `INVALID_INDEX`
- `"Card is not buyable"` -> `INVALID_PHASE`
- `"Player cannot afford this building"` -> `INSUFFICIENT_FOOD`

## Gestione nel controller

Percorso: `src/main/java/it/polimi/ingsw/controller/GameController.java`

I metodi pubblici di azione:
- `pickTopCard`
- `pickBottomCard`
- `pickTopBuilding`
- `pickBottomBuilding`
- `occupyOfferTrailTile`

sono stati incapsulati in `try/catch`:
- `try`: delega a `state.*`
- `catch (IllegalMoveException)`: ritorna `false`

In questo modo il controller non si occupa di generare eventi: l'evento e gia stato broadcastato dal model.

## Flusso completo (errore di mossa)

1. Il client invoca una action sul controller.
2. Il controller delega al `GameState`.
3. La fase corrente valida la mossa.
4. Se la mossa e illegale:
   - il model emette `GameEvent` (`raiseEvent`);
   - il model lancia `IllegalMoveException`.
5. Il controller intercetta `IllegalMoveException` e restituisce `false`.
6. La view (observer) reagisce in base al `GameEvent.Type`.

## Esempi di reazione lato view

- `WRONG_TURN`: messaggio broadcast a tutti i giocatori.
- `INSUFFICIENT_FOOD`: feedback mirato (es. animazione errore su building).
- `INVALID_INDEX`: comportamento silenzioso o feedback minimale.
- `INVALID_PHASE`: messaggio contestuale alla fase corrente.

## Note finali

- Il sistema non usa librerie GUI/network/IO nel package `model/game`.
- Il meccanismo `publishTrigger(...)` dei building resta separato da `raiseEvent(...)`.
- La logica di gioco esistente non e stata ristrutturata: e stato aggiunto solo il canale di notifica eventi/errori.

