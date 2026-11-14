# NextBedWars

Fork natalizio di BedWars1058 per **Paper 1.21.x** e **Folia**, mantenuto da **Branduzzo**.

BedWars è un minigioco in cui devi difendere il tuo letto e distruggere quello degli avversari:
quando il tuo letto viene distrutto non puoi più respawnare.

## Requisiti

- Java 21
- Paper 1.21.x (nessun NMS, nessun modulo per versioni precedenti) oppure Folia
- Nessuna dipendenza obbligatoria: Vault, PlaceholderAPI, Citizens, Parties sono opzionali

## Comandi

- `/bw` (alias `/nbw`, `/nextbedwars`, `/bedwars`) — comando principale
- `/bw debug info` — stato delle feature natalizie
- `/bw debug loot` — forza la caduta del regalo sulla tua arena
- `/bw debug loottest` — estrae subito un loot con l'apertura simulata
- `/bw debug tree` — piazza un albero di Natale per il tuo team
- `/bw debug snowman` — evocava un pupazzo di neve
- `/bw debug bridge` — costruisce un Ice Bridge davanti a te
- `/bw debug trap` — arma la trappola natalizia del tuo team
- `/bw debug item <id>` — dà un item dello shop natalizio
- `/bw debug reload` — ricarica `christmas.yml`
- `/bw reload` — ricarica lingue, tema e `christmas.yml`

## Tema

La palette è definita in `christmas.yml` con valori esadecimali:

```yaml
theme:
  primary: "#00C853"
  secondary: "#FFFFFF"
  accent: "#D32F2F"
  prefix: "{primary}&lNEXT&f&lBEDWARS {accent}❄ {secondary}"
```

I segnaposto `{primary}`, `{secondary}`, `{accent}`, `{primaryDark}`, `{secondaryDark}` e i gradienti
`{gradient:testo}` / `{gradient:testo:secondario}` funzionano in tutti i messaggi, in `messages_it.yml`
e in `messages_en.yml`. La lingua predefinita è l'italiano, con inglese come fallback.

## Feature natalizie (`christmas.yml`)

- **Regalo dal cielo** (`loot`): ogni `drop.interval` secondi, se nell'arena ci sono almeno
  `drop.min-players` giocatori, la renna lascia cadere un regalo sopra la mappa. Il regalo cerca
  terreno solido valido (mai nel vuoto), resta fermo per `drop.lifetime` secondi e all'apertura
  distribuisce `loot.max-rolls` estrazioni tra materiali, oggetti dello shop, pozioni ed effetti.
- **Pupazzo di neve** (`snowman`): 7 oro, immune al calore e a ogni danno che non venga da un giocatore,
  stazionario, rimosso a fine partita.
- **Albero di Natale** (`tree`): 5 smeraldi, appare automaticamente alla base, dura `tree.duration`
  secondi, dà ai compagni in base gli effetti configurati, danneggia gli avversari in base senza
  poterli uccidere, ha una vita e quando viene distrutto svanisce.
- **Trappola natalizia** (`trap`): 5 diamanti, quando un avversario entra in base riceve Darkness per
  `trap.darkness` secondi, viene teletrasportato alla propria base e vede il messaggio configurato.
- **Ice Bridge** (`bridge`): click destro, crea un ponte di ghiaccio lungo `bridge.length` blocchi che
  dura `bridge.duration` secondi e poi si sgretola. Un solo utilizzo.

Tutte le feature hanno un toggle `enabled` e si possono testare con `/bw debug`.

## Config merger

All'avvio i file di configurazione vengono riscritti mettendo **prima** tutte le chiavi nuove e poi
i valori già presenti sul server, così gli aggiornamenti non perdono nulla e le nuove impostazioni
sono sempre visibili. Vale per `config.yml`, `shop.yml`, `upgrades.yml`, `generators.yml`,
`signs.yml` e `christmas.yml`.

## Arena 40x40

La dimensione dell'isola non è fissa nel codice: `island-radius` è configurabile per arena e
`max-build-y` limita l'altezza di costruzione, quindi una mappa 40x40 si ottiene costruendola e
impostando `island-radius: 20` (raggio) per ogni team.

## Licenza

Distribuito con licenza GNU GPL 3.0. Il progetto deriva da BedWars1058 di Andrei Dascălu: il
copyright originale resta di Andrei Dascălu, il fork e le modifiche sono di Branduzzo.
