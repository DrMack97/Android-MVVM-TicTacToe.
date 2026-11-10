# Tres en Raya — MVVM amb Jetpack Compose

Aplicación Android del juego del Tres en Raya (Tic-Tac-Toe) desarrollada en **Kotlin** con **Jetpack Compose** y la arquitectura **MVVM con modelo separado**.

## Índice

1. [Anàlisi i documentació de l'arquitectura MVVM](#1-anàlisi-i-documentació-de-larquitectura-mvvm)
2. [Arquitectura del Tres en Raya](#2-arquitectura-del-tres-en-raya)
3. [Conceptos y métodos aprendidos](#3-conceptos-y-métodos-aprendidos)

---

## 1. Anàlisi i documentació de l'arquitectura MVVM

### 1.1. Documentació dels components (basant-vos en MVVMSimple)

El flujo de MVVM siempre va en un solo sentido:

```
View ──(acción del usuario)──► ViewModel ──(estado nuevo)──► State
 ▲                                                             │
 └───────────────(Compose detecta el cambio y recompone)───────┘
```

#### Model — `SimpleState`

**Quines dades guarda i quina és la seva funció principal?**

Guarda los datos que se muestran en pantalla: el contador (`counter`) y el mensaje (`message`). Su función es **describir el estado de la pantalla** en un momento dado. Es una `data class` con todos sus campos `val`, así que es **inmutable**: un `SimpleState` nunca cambia una vez creado. En este ejemplo el modelo no tiene métodos ni lógica, solo datos.

#### ViewModel — `SimpleViewModel`

**Quina feina fa exactament?**

Prepara lo que se mostrará en la vista. Para ello:
- **Guarda el estado** de la pantalla y es el único que puede modificarlo.
- **Recibe las acciones del usuario** (`incrementCounter()`, `reset()`) y decide cómo cambia el estado.
- **Sobrevive a la rotación de pantalla**, porque se obtiene con `by viewModels()`.

**Com es comunica amb el Model?**

No modifica el modelo, porque es inmutable. Crea un estado nuevo a partir del actual con `copy()` (o con `SimpleState()` para volver al inicio) y lo sustituye por el anterior con `_state.value = ...`.

**Com exposa les dades a la interfície?**

Usa dos variables:
- `_state`: privada y modificable (`mutableStateOf`). Solo el ViewModel puede cambiarla.
- `state`: pública y de solo lectura (`State<SimpleState>`). La vista puede leerla, pero no modificarla.

El ViewModel **no envía** los datos a la vista ni sabe que existe: solo expone la propiedad `state`. Así el ViewModel es la única fuente de verdad del estado.

#### View (UI / Compose) — `MainActivity` + `SimpleScreen`

**Quin és el seu paper?**

Solo pinta el estado que recibe. No contiene lógica. `MainActivity` obtiene el ViewModel con `by viewModels()` y muestra la pantalla `SimpleScreen`.

**Com rep els canvis d'estat?**

Lee `viewModel.state.value`. Como es un `State`, Compose registra que `SimpleScreen` lo ha leído y, cuando cambia, vuelve a ejecutar esa función con los datos nuevos (**recomposición**).

**Com notifica les accions de l'usuari al ViewModel?**

Llamando a las funciones del ViewModel desde los eventos de la interfaz, por ejemplo `Button(onClick = { viewModel.incrementCounter() })`. La vista nunca modifica el estado directamente: solo avisa de lo que ha hecho el usuario.

---

### 1.2. Anàlisi comparatiu (MVVMSimple vs MVVM-separatModel)

#### Estructura i separació de responsabilitats

**Quina diferència hi ha entre la forma d'organitzar el Model a MVVMSimple i a MVVM-separatModel?**

| | MVVMSimple | MVVM-separatModel |
|---|---|---|
| Clases del modelo | Una: `SimpleState` | Dos: `CardModel` (una carta) y `BoardModel` (el tablero) |
| Contenido | Solo datos | Datos **y** reglas del juego |
| Métodos | Ninguno | `flip()`, `isSameCard()`, `markAsMatched()`, `onCardClick()`, `checkMatches()`, `createInitialBoard()` |

En MVVMSimple el modelo es un simple contenedor de datos. En MVVM-separatModel el modelo es **"inteligente"**: cada carta sabe girarse y compararse con otra, y el tablero sabe aplicar las reglas del juego.

**On s'executa la lògica de negoci del joc en cada un dels fitxers?**

- **MVVMSimple:** en el **ViewModel**. La única regla ("sumar 1 al contador y actualizar el mensaje") está en `SimpleViewModel.incrementCounter()`, porque el modelo no tiene métodos.
- **MVVM-separatModel:** principalmente en el **modelo**:
  - `BoardModel.onCardClick()`: gira la carta pulsada si no está emparejada.
  - `BoardModel.checkMatches()`: comprueba las dos cartas giradas; si son iguales las marca como emparejadas, si no las tapa, y detecta el final de la partida.
  - `CardModel`: `flip()`, `isSameCard()` y `markAsMatched()`.

  Aun así, **una parte de la lógica se ha quedado en el ViewModel**: la regla "no se pueden girar más de 2 cartas" (`if (flippedUnmatched.size >= 2) return`) y la decisión de comprobar las parejas tras 1 segundo. En nuestra versión del Joc de les Parelles movimos la regla de las 2 cartas a `BoardModel`, porque es una regla del juego.

#### Immutabilitat i gestió de l'estat

**Com es modifiquen les dades del tauler a MVVM-separatModel?**

No se modifican: **se sustituyen**. Cada método del modelo (`onCardClick()`, `checkMatches()`) devuelve un `BoardModel` **nuevo** con los cambios, y el ViewModel lo guarda en el `StateFlow`:

```kotlin
_board.value = _board.value.checkMatches()
```

**Quin paper juga el mètode .copy() i per què és important per a l'arquitectura reactiva?**

`copy()` crea un objeto **nuevo** igual al actual, cambiando solo los campos indicados. El objeto original no se toca. Es importante porque:
- **Compose y `StateFlow` detectan los cambios comparando** el valor anterior con el nuevo. Si se modificara el mismo objeto por dentro, el valor seguiría pareciendo "el mismo" y la pantalla no se actualizaría.
- **Evita efectos secundarios:** nadie puede cambiar un estado que otra parte del código está usando.
- **Hace el código más predecible:** cada acción produce un estado nuevo a partir del anterior.

#### Avantatges de la separació

**Per què creieu que és millor desconnectar la lògica de negoci (BoardModel) del propi ViewModel a mesura que l'aplicació creix?**

Para que, a medida que se implementan cambios en la aplicación, cada capa tenga una sola responsabilidad:
- **Mantenimiento:** si cambia una regla del juego, solo se modifica `BoardModel`. Lo comprobamos en el Joc de les Parelles: para cambiar de "comprobar tras 1 segundo" a "comprobar al pulsar VALIDATE" solo hubo que tocar el modelo y añadir una función al ViewModel; la vista apenas cambió.
- **Tests:** `BoardModel` es Kotlin puro y se puede probar con tests unitarios sin emulador.
- **Reutilización:** el mismo modelo sirve con otra interfaz (Compose o XML) o con otro ViewModel.
- **Legibilidad:** el ViewModel queda corto y solo coordina; si tuviera todas las reglas, sería una clase enorme y difícil de entender.

---

### 1.3. Anàlisi comparatiu: StateFlow vs mutableStateOf

#### Per a què es fan servir?

Los dos sirven para lo mismo: **guardar un estado observable**, de forma que cuando cambia, quien lo está observando (la vista) se entera y se actualiza.

- **`StateFlow` / `MutableStateFlow`** (librería de corrutinas de Kotlin): un flujo que siempre tiene un valor actual y emite cada valor nuevo a quien lo recoge. `MutableStateFlow` es la versión modificable (privada en el ViewModel) y `StateFlow` la de solo lectura (pública).
- **`mutableStateOf` / `State<T>`** (librería de Compose): una "caja" observable por Compose. `mutableStateOf` es la versión modificable y `State<T>` la de solo lectura.

#### Quines són les diferències entre ells?

| | `StateFlow` | `mutableStateOf` |
|---|---|---|
| Pertenece a | Kotlin Coroutines (independiente de la interfaz) | Jetpack Compose |
| Lectura en Compose | `collectAsState()` | Directamente con `.value` |
| Sirve fuera de Compose (XML, otras capas) | Sí, con `collect` | No está pensado para ello |
| Operadores para transformar datos (`map`, `combine`…) | Sí | No |
| Complejidad | Algo mayor (corrutinas) | Más simple |
| Ejemplo de referencia | MVVM-separatModel | MVVMSimple |

#### Quin escolliries per a l'aplicació i per què?

Elegiría **`StateFlow`**, y es el que usa este Tres en Raya:
- **El ViewModel no depende de Compose.** Si mañana la vista cambia, el ViewModel sigue sirviendo. Lo comprobamos en el Joc de les Parelles: el mismo `GameViewModel` con `StateFlow` funcionó primero con una vista XML (con `collect`) y luego con Compose (con `collectAsState()`) sin cambiar ni una línea.
- **Es el estándar recomendado** para exponer el estado desde un ViewModel en Android.
- **Escala mejor** si la app crece: permite combinar varios estados o recibir datos de una base de datos o de una API.

Para una pantalla muy pequeña y que solo use Compose, como MVVMSimple, `mutableStateOf` también es una opción válida, porque es más sencilla.

---

## 2. Arquitectura del Tres en Raya

### 2.1. La app en funcionamiento

| Partida en curso | Victoria | Empate |
|:---:|:---:|:---:|
| ![Partida en curso](docs/partida.png) | ![Victoria](docs/victoria.png) | ![Empate](docs/empate.png) |

![Demo de la app](docs/demo.gif)

### 2.2. Estructura del proyecto

```
app/src/
├── main/java/com/example/tictactoe/
│   ├── model/
│   │   └── TicTacToeModels.kt      → Model: CellModel, BoardModel
│   ├── viewmodel/
│   │   └── GameViewModel.kt        → ViewModel
│   ├── ui/
│   │   ├── GameScreen.kt           → View: pantalla principal
│   │   ├── components/
│   │   │   ├── BoardGrid.kt        → View: tablero 3×3
│   │   │   └── CellItemView.kt     → View: una casilla
│   │   └── theme/                  → Tema Material 3
│   └── MainActivity.kt             → Punto de entrada
└── test/java/com/example/tictactoe/
    └── model/
        └── BoardModelTest.kt       → Tests unitarios del modelo
```

### 2.3. Responsabilidad de cada capa

| Capa | Archivo | Qué hace | Qué **no** hace |
|---|---|---|---|
| **Model** | `TicTacToeModels.kt` | Guarda el estado de la partida y aplica **todas** las reglas: movimientos válidos, turnos, ganador y empate | No conoce Android, Compose ni el ViewModel (no tiene ningún `import`) |
| **ViewModel** | `GameViewModel.kt` | Guarda el `BoardModel` actual, recibe los clics y pide al modelo el tablero nuevo | No contiene reglas del juego: sus dos funciones tienen una sola línea |
| **View** | `GameScreen.kt`, `BoardGrid.kt`, `CellItemView.kt` | Observa el estado y lo pinta; avisa de los clics | No decide nada: solo traduce el estado a pantalla |

#### Model

- **`CellModel`**: una casilla con `id`, `position` y `symbol` ("X", "O" o ""), y las propiedades calculadas `isOccupied`, `row` y `column`.
- **`BoardModel`**: la partida completa (`cells`, `currentTurn`, `winner`, `isDraw`, `isGameOver`) y sus reglas:
  - `createInitialBoard()`: método de fábrica con 9 casillas vacías y turno de X.
  - `makeMove(position)`: valida el movimiento, coloca la ficha, comprueba el resultado y cambia el turno. Si el movimiento es inválido devuelve **la misma instancia** sin cambios.
  - `checkWinner()`: recorre las 8 combinaciones ganadoras, guardadas como datos en `WINNING_LINES` en lugar de escribir 8 condiciones.
  - `checkDraw()`: tablero lleno y sin ganador.

#### ViewModel

```kotlin
private val _boardState = MutableStateFlow(BoardModel.createInitialBoard())
val boardState: StateFlow<BoardModel> = _boardState.asStateFlow()

fun onCellClick(position: Int) {
    _boardState.value = _boardState.value.makeMove(position)
}
```

El estado modificable es privado y la vista solo ve la versión de solo lectura. Como el ViewModel se obtiene con `viewModel()`, **la partida se conserva al girar el dispositivo**.

#### View

- **`GameScreen`**: único composable que conoce el ViewModel. Obtiene el estado con `collectAsState()` y lo pasa a `GameContent`.
- **`GameContent`**: pinta la pantalla sin estado propio (*stateless*). Por eso se puede previsualizar con `@Preview` sin ViewModel.
- **`BoardGrid`**: reparte las 9 casillas en 3 filas de 3 con `Column` + `Row`. Se descartó `LazyVerticalGrid` porque el tablero tiene siempre 9 casillas y no necesita carga perezosa ni desplazamiento.
- **`CellItemView`**: casilla cuadrada con borde, "X" en azul y "O" en rojo. Solo se puede pulsar si está vacía y la partida sigue activa.

### 2.4. Flujo de un movimiento

```
1. El usuario pulsa la casilla 4
        │
2. CellItemView ─► onClick() ─► BoardGrid ─► onCellClick(4)
        │
3. GameViewModel.onCellClick(4)
        │
4. BoardModel.makeMove(4)
   ├── ¿Movimiento válido? No ─► devuelve el mismo tablero (no se redibuja nada)
   └── Sí ─► copy() con la ficha, comprueba ganador/empate, cambia el turno
        │
5. _boardState.value = tablero nuevo
        │
6. collectAsState() detecta el cambio ─► Compose recompone GameScreen
        │
7. Se ve la ficha y el texto "Turno: O"
```

### 2.5. Tests

`BoardModelTest` contiene 10 tests unitarios del modelo que se ejecutan **sin emulador**: tablero inicial, cambio de turno, inmutabilidad, casilla ocupada, posición fuera de rango, victoria por fila, columna y diagonal, empate y movimientos tras terminar la partida.

---

## 3. Conceptos y métodos aprendidos

### Kotlin

| Concepto | Para qué sirve | Dónde lo he usado |
|---|---|---|
| `data class` | Clase pensada para guardar datos; genera `equals()`, `toString()` y `copy()` | `CellModel`, `BoardModel` |
| `.copy()` | Crea un objeto nuevo cambiando solo algunos campos, sin modificar el original | `makeMove()` |
| `val` frente a `var` | `val` no se puede reasignar; es la base de la inmutabilidad | Todos los campos del modelo |
| Propiedad calculada (`get()`) | Valor que se calcula cada vez a partir de otros campos | `isOccupied`, `row`, `column` |
| `companion object` | Miembros que pertenecen a la clase y no a cada objeto | Constantes y `createInitialBoard()` |
| `const val` | Constante conocida en tiempo de compilación | `PLAYER_X`, `SIZE`… |
| Método de fábrica | Función que crea un objeto ya configurado | `createInitialBoard()` |
| `List(n) { }` | Crea una lista de `n` elementos a partir de una función | Las 9 casillas iniciales |
| `map` | Transforma cada elemento de una lista en otro | Colocar la ficha en la casilla |
| `chunked(n)` | Divide una lista en grupos de `n` | Repartir las casillas en filas de 3 |
| `all` / `none` | Comprueba si todos / ningún elemento cumple una condición | `checkDraw()`, tests |
| Desestructuración `val (a, b, c)` | Extrae varios valores de una lista o un objeto a la vez | `checkWinner()` |
| `in 0 until n` | Comprueba si un número está en un rango | Validar la posición |
| Referencia a función `::` | Pasa una función como valor sin llamarla | `viewModel::onCellClick` |
| KDoc (`/** */`) | Documentación de clases, métodos y propiedades | Todos los archivos |

### Arquitectura y estado

| Concepto | Para qué sirve | Dónde lo he usado |
|---|---|---|
| `ViewModel` | Guarda el estado de la pantalla y sobrevive a la rotación | `GameViewModel` |
| `MutableStateFlow` | Estado observable y modificable | `_boardState` |
| `StateFlow` | Versión de solo lectura que se expone a la vista | `boardState` |
| `asStateFlow()` | Convierte un `MutableStateFlow` en `StateFlow` de solo lectura | `GameViewModel` |
| `viewModel()` | Obtiene el ViewModel dentro de un composable; devuelve siempre la misma instancia | `GameScreen` |
| Fuente única de verdad | Solo una clase puede cambiar el estado | Patrón `_boardState` / `boardState` |

### Jetpack Compose

| Concepto | Para qué sirve | Dónde lo he usado |
|---|---|---|
| `@Composable` | Marca una función que describe una parte de la interfaz | Todas las funciones de la View |
| `collectAsState()` | Convierte un `StateFlow` en estado de Compose para que se recomponga al cambiar | `GameScreen` |
| `by` (delegado) | Permite usar `board` en lugar de `board.value` | `val board by ...collectAsState()` |
| Recomposición | Compose vuelve a ejecutar las funciones que leyeron un estado cuando este cambia | Toda la View |
| Composable *stateless* | Recibe datos y funciones en lugar del ViewModel; es más reutilizable y previsualizable | `GameContent`, `BoardGrid`, `CellItemView` |
| `@Preview` | Muestra el composable en Android Studio sin ejecutar la app | `GameContentPreview` |
| `Scaffold` + `innerPadding` | Estructura de pantalla Material 3 que respeta las barras del sistema | `MainActivity` |
| `Column` / `Row` | Colocan elementos en vertical / horizontal | Pantalla y tablero |
| `LazyVerticalGrid` | Cuadrícula que solo dibuja lo visible; útil para listas largas | Estudiada en MVVM-separatModel; descartada aquí por tener solo 9 casillas |
| `Modifier.weight()` | Reparte el espacio disponible entre hermanos | Filas y casillas del tablero |
| `Modifier.aspectRatio(1f)` | Mantiene una proporción (cuadrado) | Tablero y casillas |
| `Modifier.clickable(enabled)` | Hace pulsable un elemento y permite desactivarlo | `CellItemView` |
| `Modifier.border()` / `clip()` | Borde y recorte con esquinas redondeadas | `CellItemView` |

### Tests y herramientas

| Concepto | Para qué sirve |
|---|---|
| Tests unitarios (JUnit) | Probar el modelo sin emulador |
| `assertEquals` frente a `assertSame` | `assertEquals` compara el contenido; `assertSame` comprueba que es **el mismo objeto** (usado para verificar que un movimiento inválido no crea un tablero nuevo) |
| Sincronización de Gradle | Hace que Android Studio reconozca el proyecto como app Android; un nombre de proyecto terminado en `.` impedía sincronizar |
| `git add` / `commit` / `push` | Guardar y subir el trabajo, con un commit por capa |
| `git log --oneline` | Ver el historial; `origin/main` indica lo que ya está en GitHub |
