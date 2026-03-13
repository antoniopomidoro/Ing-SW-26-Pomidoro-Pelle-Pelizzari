# EnumMap nella classe Deck

## Cos'è una EnumMap?

`EnumMap<K extends Enum<K>, V>` è un'implementazione specializzata dell'interfaccia `Map` di Java, progettata **esclusivamente** per chiavi di tipo `Enum`. Fa parte del package `java.util`.

### Caratteristiche principali

| Caratteristica | Dettaglio |
|---|---|
| **Struttura interna** | Utilizza un **array** di dimensione fissa, dove ogni posizione corrisponde a una costante dell'enum (tramite il suo `ordinal()`). Non usa hashing né bucket. |
| **Prestazioni** | Tutte le operazioni (`get`, `put`, `containsKey`, `remove`) sono **O(1)** nel caso peggiore, più veloci di `HashMap`. |
| **Memoria** | Occupa **meno memoria** di una `HashMap` perché non crea oggetti `Entry` né mantiene una tabella hash. |
| **Ordinamento** | Le iterazioni seguono l'**ordine naturale** dell'enum (l'ordine di dichiarazione delle costanti). |
| **Null** | Ammette valori `null`, ma **non** ammette chiavi `null`. |
| **Thread-safety** | **Non** è sincronizzata; per uso concorrente va wrappata con `Collections.synchronizedMap()`. |

---

## Come viene usata nella classe Deck

La classe `Deck` gestisce le carte e gli edifici del gioco, organizzati per Era (Age). Invece di dichiarare 6 campi separati:

```java
// ❌ Approccio con campi separati (da evitare)
private List<Card> age_1_Cards;
private List<Card> age_2_Cards;
private List<Card> age_3_Cards;
private List<Building> age_1_Buildings;
private List<Building> age_2_Buildings;
private List<Building> age_3_Buildings;
```

Si utilizzano **due EnumMap** con chiave `Age`:

```java
// ✅ Approccio con EnumMap (adottato)
private EnumMap<Age, List<Card>> cards;
private EnumMap<Age, List<Building>> buildings;
```

---

## Inizializzazione

Una `EnumMap` richiede il `.class` dell'enum nel costruttore:

```java
this.cards = new EnumMap<>(Age.class);
this.buildings = new EnumMap<>(Age.class);

// Popolare con liste vuote per ogni Era
for (Age age : Age.values()) {
    cards.put(age, new ArrayList<>());
    buildings.put(age, new ArrayList<>());
}
```

---

## Esempio di utilizzo nei metodi della classe Deck

### `popCard(Age age)`
Estrae e rimuove la prima carta dal mazzo dell'era specificata:

```java
public Card popCard(Age age) {
    List<Card> ageCards = cards.get(age);
    if (ageCards == null || ageCards.isEmpty()) {
        return null;
    }
    return ageCards.remove(0);
}
```

### `popBuilding(Age age)`
Analogo a `popCard`, ma per gli edifici:

```java
public Building popBuilding(Age age) {
    List<Building> ageBuildings = buildings.get(age);
    if (ageBuildings == null || ageBuildings.isEmpty()) {
        return null;
    }
    return ageBuildings.remove(0);
}
```

### `shuffle(Age age)`
Mescola le carte di una specifica era:

```java
public boolean shuffle(Age age) {
    List<Card> ageCards = cards.get(age);
    if (ageCards == null) {
        return false;
    }
    Collections.shuffle(ageCards);
    return true;
}
```

---

## Perché EnumMap e non HashMap?

| Aspetto | `EnumMap<Age, ...>` | `HashMap<Age, ...>` |
|---|---|---|
| **Velocità** | Accesso diretto via indice array | Calcolo hash + gestione collisioni |
| **Memoria** | Array compatto (3 slot per `Age`) | Tabella hash con overhead |
| **Ordine iterazione** | Garantito (ordine dell'enum) | Non garantito |
| **Type-safety** | Accetta solo chiavi `Age` | Accetta qualsiasi `Object` come chiave |
| **Null keys** | Non ammesse (errore esplicito) | Ammesse (possibile fonte di bug) |

### Vantaggi concreti nel progetto

1. **Scalabilità**: Se in futuro si aggiunge un'`AGE_4` all'enum `Age`, basta aggiungere la costante. La `EnumMap` la gestirà automaticamente senza modifiche alla classe `Deck`.
2. **Eliminazione di switch/if-else**: Invece di scrivere blocchi condizionali per selezionare la lista giusta in base all'era, si usa direttamente `cards.get(age)`.
3. **Codice più pulito**: Due campi invece di sei, con accesso uniforme e parametrico.
4. **Iterazione naturale**: Quando si deve operare su tutte le ere (es. contare le carte totali), basta iterare sulla mappa:

```java
int totalCards = 0;
for (List<Card> ageCards : cards.values()) {
    totalCards += ageCards.size();
}
```

---

## Best Practices per l'uso di EnumMap

1. **Preferire `EnumMap` a `HashMap`** quando le chiavi sono un enum.
2. **Inizializzare tutte le chiavi** nel costruttore per evitare `NullPointerException`.
3. **Non usare `ordinal()` direttamente** per accedere ai dati — `EnumMap` lo gestisce internamente.
4. **Usare `EnumMap` come tipo concreto** nella dichiarazione del campo per chiarezza, o `Map` come tipo dell'interfaccia nei metodi pubblici per flessibilità.
5. **Iterare con `entrySet()`** quando servono sia chiave che valore:

```java
for (Map.Entry<Age, List<Card>> entry : cards.entrySet()) {
    Age age = entry.getKey();
    List<Card> ageCards = entry.getValue();
    System.out.println(age + ": " + ageCards.size() + " carte");
}
```

---

## Riferimenti

- [Javadoc ufficiale — EnumMap](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/EnumMap.html)
- UML del progetto: [`Documentation/uml.puml`](uml.puml)
