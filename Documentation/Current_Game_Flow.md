# Funzionamento attuale del gioco (as-is)

Questo documento descrive **come il gioco funziona oggi nel codice**, con focus su:
- state machine delle fasi
- risoluzione degli eventi
- trigger ed effetti dei building

Riferimenti principali:
- `src/main/java/it/polimi/ingsw/controller/GameController.java`
- `src/main/java/it/polimi/ingsw/model/game/GameState.java`
- `src/main/java/it/polimi/ingsw/model/game/*.java`
- `src/main/java/it/polimi/ingsw/model/board/Board.java`
- `src/main/java/it/polimi/ingsw/model/cards/Event.java`
- `src/main/java/it/polimi/ingsw/model/cards/Building.java`
- `src/main/java/it/polimi/ingsw/model/effects/EventEffect.java`
- `src/main/resources/json/cards.json`
- `src/main/resources/json/buildings.json`

## 1) Avvio partita

`GameController`:
1. carica i dati JSON (`JsonFactory.loadAllData()`),
2. crea deck e board,
3. istanzia `GameState`, che parte subito in `SetupPhase` tramite `setPhase(new SetupPhase())`.

`setPhase(...)` in `GameState` **esegue immediatamente** la fase (`execute`).

## 2) State machine delle fasi

Flusso principale:

`SetupPhase` -> `StartTurnPhase` -> `TurnPhase` <-> `PlayerTurnPhase` -> `EndTurnPhase` -> (`StartTurnPhase` | `ChangeAgePhase` | `EndGamePhase`)

### SetupPhase
- Mescola `orderTileOrder`.
- Assegna cibo iniziale secondo `GameConfig.startingFood`.
- Popola board con carte top/bottom in base a `GameConfig`.
- Passa a `StartTurnPhase`.

### StartTurnPhase
- Ricarica le top cards dal deck.
- Se il deck dell'era corrente e' finito, passa a `ChangeAgePhase` (se c'e' un'era successiva).
- Dal turno 2 in poi applica bonus/malus ordine turno (`OrderTile`).
- Pubblica trigger `START_TURN` per i building.
- I giocatori occupano le tile tramite `occupyOfferTrailTile(...)`; quando tutti hanno piazzato il totem, passa a `TurnPhase`.

### TurnPhase
- Scansiona le tile occupate in ordine indice.
- Per ogni tile occupata entra in `PlayerTurnPhase` con quel player.
- Finite le tile, gestisce eventuali extra-pick (`extraUpperPick`) creando turni virtuali.
- Poi passa a `EndTurnPhase`.

### PlayerTurnPhase
- Applica eventuale bonus cibo della tile.
- Il player attivo effettua pick top/bottom (card o building) secondo i pick disponibili.
- Dopo ogni pick pubblica trigger `ON_CARD_PICK` **solo sui building del player attivo**.
- Quando non puo' piu' pickare, torna a `TurnPhase`.

### EndTurnPhase
- Scarta le bottom cards (qui si risolvono gli eventi).
- Sposta top -> bottom cards.
- Pubblica trigger `END_TURN`.
- Libera le tile occupate.
- Aggiorna ordine turno (`orderTileOrder = turnOrder`), svuota `turnOrder`, incrementa turno.
- Se raggiunto `maxTurns`, passa a `EndGamePhase`; altrimenti `StartTurnPhase`.

### ChangeAgePhase
- Incrementa era (`AGE_1` -> `AGE_2` -> `AGE_3`).
- Aggiorna i building disponibili su board (bottom scartata, top -> bottom, nuova top).
- Torna a `StartTurnPhase`.

### EndGamePhase
- Pubblica trigger `END_GAME`.
- Calcola i punteggi finali addizionali da stats/personaggi/building.
- Stato terminale.

## 3) Risoluzione eventi

Pipeline attuale:
1. `Board.discardBottomCards(state)` ordina le bottom cards per `resolutionPriority` e chiama `onDiscard`.
2. Per una `Event`, `onDiscard(...)` invoca `effect.executeEffect(state, triggerKey, age)`.
3. `EventEffect.executeEffect(...)` fa due passi:
   - esegue `applyEffect(...)` dell'evento,
   - poi `state.publishTrigger(triggerKey)`.

Conseguenza: dopo ogni evento, si attivano i building con trigger coerente (`HUNTER_EVENT`, `PAINTING_EVENT`, `SHAMAN_EVENT`, `SUSTAINMENT_EVENT`).

Nota priorita': in `cards.json` gli eventi `SUSTENANCE` hanno priorita' 1, gli altri in genere 0, quindi vengono risolti dopo quelli con priorita' minore.

## 4) Trigger e effetti building

I building hanno:
- `triggerKey` (quando attivarsi),
- `effect` (`ContextualEffect`) con la logica.

Dispatch globale:
- `GameState.publishTrigger(key)` itera i player in `orderTileOrder`, filtra i loro building per key e chiama `triggerBuildingEffect(...)`.

Trigger usati nel flusso:
- `START_TURN`, `END_TURN`, `END_GAME` (fasi globali)
- `ON_CARD_PICK` (durante pick del player attivo)
- trigger evento (`HUNTER_EVENT`, `PAINTING_EVENT`, `SHAMAN_EVENT`, `SUSTAINMENT_EVENT`) pubblicati dalla pipeline eventi

## 5) Comportamenti as-is da tenere presenti

- Gli effetti `ON_ACQUIRE` sono modellati (`Building.onAddedToPlayer(...)`), ma nell'acquisto building in `PlayerTurnPhase` viene chiamato `addBuilding(...)` e **non** `onAddedToPlayer(...)`; quindi oggi non risultano attivati automaticamente.
- `publishTrigger(...)` usa `orderTileOrder` (non `players`): l'ordine/insieme usato per i trigger dipende da quella lista.
- Alcuni effetti legati al piazzamento totem usano campi stats non sempre letti nel resto del flusso, quindi l'effetto reale puo' non coincidere con l'intento del JSON.
- La logica qui descritta e' volutamente "as-is": documenta il comportamento corrente, non quello desiderato di design.

## 6) Mappa rapida file

- Stato e transizioni: `src/main/java/it/polimi/ingsw/model/game`
- Scarto/risoluzione carte: `src/main/java/it/polimi/ingsw/model/board/Board.java`
- Eventi: `src/main/java/it/polimi/ingsw/model/cards/Event.java`, `src/main/java/it/polimi/ingsw/model/effects/events`
- Building: `src/main/java/it/polimi/ingsw/model/cards/Building.java`, `src/main/java/it/polimi/ingsw/model/effects/contextual`
- Dati configurazione: `src/main/resources/json/cards.json`, `src/main/resources/json/buildings.json`, `src/main/resources/json/config.json`

