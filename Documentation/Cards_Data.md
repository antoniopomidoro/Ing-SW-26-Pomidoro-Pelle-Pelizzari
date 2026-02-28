# 🗂️ Documentazione Dati: Carte di Mesos

Questo documento contiene il conteggio esatto e la distribuzione di tutte le carte presenti nel gioco, suddivise per tipologia, Era e scalabilità in base al numero di giocatori.

---

## 📊 Scalabilità del Mazzo Personaggi

Il mazzo principale (Personaggi) scala in base al numero di partecipanti (escludendo Edifici ed Eventi).

| Numero Giocatori | Carte Base (Sempre presenti) | Carte Aggiunte | **Totale Personaggi nel Mazzo** |
| :--- | :--- | :--- | :--- |
| **2 Giocatori** | 51 | - | **51** |
| **3 Giocatori** | 51 | + 11 (carte 3+) | **62** |
| **4 Giocatori** | 62 | + 11 (carte 4+) | **73** |
| **5 Giocatori** | 73 | + 11 (carte 5+) | **84** |

---

## 🏛️ Carte Edificio (Totale: 21)

Le carte Edificio vengono divise in tre mazzetti separati durante il setup iniziale:
* **Era 1:** 6 carte
* **Era 2:** 7 carte
* **Era 3:** 8 carte

---

## 🎴 Carte Personaggio: Dettaglio per Era (Totale: 84)

*Nota di sviluppo: Il termine "Agricoltori" è stato uniformato in "Raccoglitori" per coincidere con il manuale ufficiale ed evitare incongruenze nel codice.*

### ERA 1 (Totale: 29 carte)

| Tipo | Totale | Distribuzione Giocatori <br>*(Base / 3+ / 4+ / 5)* | Dettagli Specifici (Sconti, PP, Strumenti) |
| :--- | :--- | :--- | :--- |
| **Artisti** | 5 | 3 / 1 / 1 / 0 | - |
| **Sciamani (2 stelle)** | 2 | 1 / 0 / 0 / 1 | - |
| **Sciamani (1 stella)** | 2 | 1 / 0 / 1 / 0 | - |
| **Costruttori** | 4 | 3 / 0 / 0 / 1 | Sconto/Prestigio: (1/2 base), (1/3 base), (2/1 **solo 5g**), (2/0 base) |
| **Raccoglitori** | 4 | 2 / 1 / 0 / 1 | - |
| **Inventori** | 7 | 4 / 0 / 3 / 0 | Base: Pane, Pietra, Barca, Rondella.<br>**4+:** Corda, Ciotola, Stick. |
| **Cacciatori (Cibo)** | 2 | 2 / 0 / 0 / 0 | - |
| **Cacciatori (No Cibo)** | 3 | 1 / 2 / 0 / 0 | - |

### ERA 2 (Totale: 28 carte)

| Tipo | Totale | Distribuzione Giocatori <br>*(Base / 3+ / 4+ / 5)* | Dettagli Specifici (Sconti, PP, Strumenti) |
| :--- | :--- | :--- | :--- |
| **Artisti** | 4 | 3 / 1 / 0 / 0 | - |
| **Sciamani (2 stelle)** | 3 | 2 / 0 / 0 / 1 | - |
| **Sciamani (1 stella)** | 1 | 0 / 0 / 0 / 1 | Presente **solo con 5g**. |
| **Costruttori** | 4 | 3 / 1 / 0 / 0 | Sconto/Prestigio: (1/2 **solo 3+**), (1/4 base), (2/3 base), (2/1 base) |
| **Raccoglitori** | 4 | 1 / 1 / 1 / 1 | - |
| **Inventori** | 6 | 5 / 0 / 1 / 0 | Base: Ciotola, Bambola, Rondella, Bastone, Corda.<br>**4+:** Amo. |
| **Cacciatori (Cibo)** | 3 | 1 / 1 / 1 / 0 | - |
| **Cacciatori (No Cibo)** | 3 | 2 / 0 / 0 / 1 | - |

### ERA 3 (Totale: 27 carte)

| Tipo | Totale | Distribuzione Giocatori <br>*(Base / 3+ / 4+ / 5)* | Dettagli Specifici (Sconti, PP, Strumenti) |
| :--- | :--- | :--- | :--- |
| **Artisti** | 4 | 3 / 0 / 0 / 1 | - |
| **Sciamani (3 stelle)** | 2 | 2 / 0 / 0 / 0 | - |
| **Sciamani (2 stelle)** | 3 | 1 / 1 / 1 / 0 | - |
| **Costruttori** | 4 | 3 / 0 / 0 / 1 | Sconto/Prestigio: (1/4 **solo 5g**), (1/5 base), (2/3 base), (2/2 base) |
| **Raccoglitori** | 3 | 1 / 0 / 1 / 1 | - |
| **Inventori** | 7 | 4 / 2 / 1 / 0 | Base: Pane, Amo, Bambola, Collana.<br>**3+:** Barca, Pietra.<br>**4+:** Collana. |
| **Cacciatori (Cibo)** | 2 | 1 / 0 / 0 / 1 | - |
| **Cacciatori (No Cibo)** | 2 | 2 / 0 / 0 / 0 | - |

---

## ⚠️ To-Do Sviluppo
* [ ] Aggiungere documentazione per le **10 Carte Evento**.
* [ ] Aggiungere documentazione per le **2 Carte Evento Finale**.