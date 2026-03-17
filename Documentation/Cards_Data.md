# 🗂️ Cards Data Documentation: Mesos

This document contains the exact count and distribution of all the cards in the game, categorized by type, Era, and scalability based on the number of players.

---
## 🗺️ Board Tiles (Offer Track)

The Offer Track is built using tiles **A through G**, which scale depending on the player count:
* **Tile A:** 5 players only
* **Tile D:** 3+ players
* **Tile G:** 4+ players
* *(All other tiles are always used)*
* **Tile A:** +3food
* **Tile B:** 1 bottomPick
* **Tile C:** 1 topPick
* **Tile D:** 2 bottomPicks
* **Tile E:** 1 bottomPick + 1 topPick
* **Tile F:** 2 topPicks
* **Tile G:** 2 topPicks + 1 bottomPick
## Turn Order Tiles (Tessere Ordine di Turno)

Questa sezione definisce il setup dinamico del tracciato iniziale dell'ordine di turno. La configurazione degli slot e dei relativi effetti cambia in base al numero di giocatori al tavolo.

**Regola Globale:** L'ultima tessera del tracciato (`LAST_TILE`) prevede sempre un malus: il giocatore che la occupa deve scegliere se pagare 1 Cibo o 2 Punti Prestigio (PP).

### Setup per 2 Giocatori
* **Slot 1** (`orderBonus1`): +1 Cibo
* **Slot 2** (`orderBonus2`): Paga 1 Cibo o 2 PP

### Setup per 3 Giocatori
* **Slot 1** (`orderBonus1`): +2 Cibo
* **Slot 2** (`orderBonus2`): Nessun effetto
* **Slot 3** (`orderBonus3`): Paga 1 Cibo o 2 PP

### Setup per 4 Giocatori
* **Slot 1** (`orderBonus1`): +2 Cibo
* **Slot 2** (`orderBonus2`): +1 Cibo
* **Slot 3** (`orderBonus3`): Nessun effetto
* **Slot 4** (`orderBonus4`): Paga 1 Cibo o 2 PP

### Setup per 5 Giocatori
* **Slot 1** (`orderBonus1`): +3 Cibo
* **Slot 2** (`orderBonus2`): +1 Cibo
* **Slot 3** (`orderBonus3`): Nessun effetto
* **Slot 4** (`orderBonus4`): Nessun effetto
* **Slot 5** (`orderBonus5`): Paga 1 Cibo o 2 PP


**Note di implementazione per il JSON:** Ogni order tile deve avere come id ORDER_TILE

---


## 📊 Character Deck Scalability

The main deck (Characters) scales based on the number of participants (excluding Buildings and Events).

| Number of Players | Base Cards (Always present) | Added Cards | **Total Characters in Deck** |
| :--- | :--- | :--- | :--- |
| **2 Players** | 51 | - | **51** |
| **3 Players** | 51 | + 11 (3+ cards) | **62** |
| **4 Players** | 62 | + 11 (4+ cards) | **73** |
| **5 Players** | 73 | + 11 (5+ cards) | **84** |

---

## 🏛️ Building Cards (Total: 21)

Building cards are divided into three separate decks. During the initial setup, a specific number of cards is drawn per Era based on the player count:

| Number of Players | Era 1 | Era 2 | Era 3 |
| :--- | :--- | :--- | :--- |
| **2 Players** | 1 | 2 | 3 |
| **3 Players** | 2 | 2 | 4 |
| **4 Players** | 2 | 3 | 4 |
| **5 Players** | 2 | 3 | 5 |

*(Total available pool: 6 cards in Era 1, 7 cards in Era 2, 8 cards in Era 3)*
---

## 🎴 Character Cards: Details per Era (Total: 84)

### ERA 1 (Total: 29 cards)

| Type | Total | Player Distribution <br>*(Base / 3+ / 4+ / 5)* | Specific Details |
| :--- | :--- | :--- | :--- |
| **Artists** | 5 | 3 / 1 / 1 / 0 | - |
| **Shamans** | 4 | 2 / 0 / 1 / 1 | 2 stars (1 base, 1 **5p only**)<br>1 star (1 base, 1 **4+ only**) |
| **Builders** | 4 | 3 / 0 / 0 / 1 | Discount/PP: (1/2 base), (1/3 base), (2/1 **5p only**), (2/0 base) |
| **Gatherers** | 4 | 2 / 1 / 0 / 1 | - |
| **Inventors** | 7 | 4 / 0 / 3 / 0 | Base: Bread, Stone, Boat, Ring.<br>**4+:** Rope, Bowl, Stick. |
| **Hunters** | 5 | 3 / 2 / 0 / 0 | Food icon (2 base)<br>No icon (1 base, 2 **3+ only**) |

### ERA 2 (Total: 28 cards)

| Type | Total | Player Distribution <br>*(Base / 3+ / 4+ / 5)* | Specific Details |
| :--- | :--- | :--- | :--- |
| **Artists** | 4 | 3 / 1 / 0 / 0 | - |
| **Shamans** | 4 | 2 / 0 / 0 / 2 | 2 stars (2 base, 1 **5p only**)<br>1 star (1 **5p only**) |
| **Builders** | 4 | 3 / 1 / 0 / 0 | Discount/PP: (1/2 **3+ only**), (1/4 base), (2/3 base), (2/1 base) |
| **Gatherers** | 4 | 1 / 1 / 1 / 1 | - |
| **Inventors** | 6 | 5 / 0 / 1 / 0 | Base: Bowl, Doll, Ring, Stick, Rope.<br>**4+:** Hook. |
| **Hunters** | 6 | 3 / 1 / 1 / 1 | Food icon (1 base, 1 **3+ only**, 1 **4+ only**)<br>No icon (2 base, 1 **5p only**) |

### ERA 3 (Total: 27 cards)

| Type | Total | Player Distribution <br>*(Base / 3+ / 4+ / 5)* | Specific Details |
| :--- | :--- | :--- | :--- |
| **Artists** | 4 | 3 / 0 / 0 / 1 | - |
| **Shamans** | 5 | 3 / 1 / 1 / 0 | 3 stars (2 base)<br>2 stars (1 base, 1 **3+ only**, 1 **4+ only**) |
| **Builders** | 4 | 3 / 0 / 0 / 1 | Discount/PP: (1/4 **5p only**), (1/5 base), (2/3 base), (2/2 base) |
| **Gatherers** | 3 | 1 / 0 / 1 / 1 | - |
| **Inventors** | 7 | 4 / 2 / 1 / 0 | Base: Bread, Hook, Doll, Necklace.<br>**3+:** Boat, Stone.<br>**4+:** Necklace. |
| **Hunters** | 4 | 3 / 0 / 0 / 1 | Food icon (1 base, 1 **5p only**)<br>No icon (2 base) |

---

## 📜 Event Cards (Total: 10 + 2 Final Events)

The 10 standard Event cards are shuffled into their respective Age decks. The 2 Final Event cards are always placed at the bottom of the Age 3 deck and are always shamanic ritual and sustainance.

### Event Types & Effects

* **Sustenance**
    * **Cost:** The player must pay `1 Food` OR `- (Age) PP` for each character in their tribe.
    * *Formula:* `(-1 Food OR -Age PP) * #Characters`

* **Hunting**
    * **Reward:** The player gains `1 Food`, plus extra Prestige Points (PP) based on the card's Age multiplied by the number of Hunters they own.
    * *Formula:* `(1 Food + (1 PP * Age)) * #Hunters`

* **Shamanic Ritual**
    * **Reward/Penalty:** Players are ranked based on their total Shaman stars. The first place gains PP, the last place loses PP, depending on the card's Age:
        * **Era 1:** First Place `+5 PP` / Last Place `-3 PP`
        * **Era 2:** First Place `+10 PP` / Last Place `-5 PP`
        * **Era 3:** First Place `+15 PP` / Last Place `-7 PP`

* **Cave Paintings**
    * **Reward/Penalty:** The effect depends on the number of Artists the player owns compared to the card's Age number:
        * **From 1 to Age:** Penalty of `-2 PP`.
        * **Greater than Age (`> Age`):** Reward of `(Age * #Artists) PP`.