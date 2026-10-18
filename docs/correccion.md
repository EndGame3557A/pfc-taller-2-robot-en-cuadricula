````# Informe de corrección
 
Fundamentos de Programación Funcional y Concurrente.

 
---
---
 
## Punto 1: `girar`
 
### 1.1 Especificación
 
Sea $f : \text{Direccion} \times \text{Sentido} \to \text{Direccion}$ la función que, dada una dirección y un sentido, devuelve la dirección que resulta de girar noventa grados en ese sentido. Girando a la derecha el orden es Norte → Este → Sur → Oeste → Norte, y girando a la izquierda es el orden inverso.
 
### 1.2 Programa
 
```scala
def girar(d: Direccion, s: Sentido): Direccion = (d,s) match {
  case (Norte, Derecha) => Este
  case (Sur, Derecha) => Oeste
  case (Este, Derecha) => Sur
  case (Oeste, Derecha) => Norte
  case (Norte, Izquierda) => Oeste
  case (Sur, Izquierda) => Este
  case (Este, Izquierda) => Norte
  case (Oeste, Izquierda) => Sur
}
```
 
### 1.3 Teorema a demostrar
 
```math
\forall (d, s) \in \text{Direccion} \times \text{Sentido} : P_f(d, s) == f(d, s)
```
 
### 1.4 Demostración
 
`girar` **no es recursiva** y su dominio es un conjunto **finito** (4 direcciones × 2 sentidos = 8 valores). Los tipos `Direccion` y `Sentido` solo tienen valores básicos (no hay casos recursivos), por lo que la inducción estructural se reduce a verificar **todos los casos base**.
 
Esto es análisis exhaustivo de casos. Como el `match` evalúa el par $(d, s)$ contra cada patrón, basta comparar el resultado de $P_f$ con la especificación en cada caso:
 
| $(d, s)$ | $P_f(d, s)$ (por el `case` correspondiente) | $f(d, s)$ (especificación) | ¿Coinciden? |
|---|---|---|---|
| (Norte, Derecha) | Este | Este | Sí |
| (Este, Derecha) | Sur | Sur | Sí |
| (Sur, Derecha) | Oeste | Oeste | Sí |
| (Oeste, Derecha) | Norte | Norte | Sí |
| (Norte, Izquierda) | Oeste | Oeste | Sí |
| (Oeste, Izquierda) | Sur | Sur | Sí |
| (Sur, Izquierda) | Este | Este | Sí |
| (Este, Izquierda) | Norte | Norte | Sí |
 
Por ejemplo, para el caso (Este, Derecha), usando el modelo de sustitución:
 
```math
P_f(\text{Este}, \text{Derecha}) \rightarrow (\text{Este}, \text{Derecha}) \text{ match } \{\ldots\} \rightarrow \text{Sur}
```
 
y por especificación $f(\text{Este}, \text{Derecha}) = \text{Sur}$.
 
**Cobertura:** los 8 pares posibles tienen exactamente un `case` que los reconoce. Por lo tanto el `match` es exhaustivo (nunca lanza `MatchError`) y no hay ambigüedad entre casos.
 
**Terminación:** el programa no tiene llamadas recursivas, así que termina siempre.
 
**Conclusión:** $\forall (d, s) : P_f(d, s) == f(d, s)$. `girar` es correcta respecto a su especificación.
 
---
 
## Punto 2: `ejecutar`
 
### 2.1 Especificación
 
Sea $f : \text{Estado} \times \text{Orden} \to \text{Estado}$ la función que da el estado resultante de aplicar una orden a un estado. Se define por casos sobre la forma de la orden. Para abreviar, se escribe $f_o(e) = f(e, o)$ y $f_o^k$ para la composición de $f_o$ consigo misma $k$ veces ($f_o^0$ es la identidad):
 
- $f(\text{Estado}(x,y,m), \text{Avanzar}(n))$ desplaza $n$ unidades en la dirección $m$: Norte $\to (x, y+n, m)$, Sur $\to (x, y-n, m)$, Este $\to (x+n, y, m)$, Oeste $\to (x-n, y, m)$.
- $f(\text{Estado}(x,y,m), \text{Girar}(s)) = \text{Estado}(x, y, \text{girar}(m, s))$.
- $f(e, \text{Repetir}(k, o)) = f_o^{\max(k,0)}(e)$: aplica $o$ exactamente $k$ veces (ninguna si $k \leq 0$).
- $f(e, \text{Secuencia}(\text{List}(o_1, \ldots, o_m))) = f_{o_m}(\cdots f_{o_2}(f_{o_1}(e)) \cdots)$: aplica las órdenes de izquierda a derecha. Para $m = 0$ es $e$.
### 2.2 Programa
 
```scala
def ejecutar(e: Estado, o: Orden): Estado = o match {
  case Avanzar(n) => e.mira match {
    case Norte => Estado(e.x, e.y+n, e.mira)
    case Sur => Estado(e.x, e.y-n, e.mira)
    case Este => Estado(e.x+n, e.y, e.mira)
    case Oeste => Estado(e.x-n, e.y, e.mira)
  }
  case Girar(s) => Estado(e.x, e.y, girar(e.mira, s))
  case Repetir(veces,_) if veces <= 0 => e
  case Repetir(veces, orden) => ejecutar(ejecutar(e,orden),Repetir(veces-1,orden))
  case Secuencia(Nil) => e
  case Secuencia(primera::resto) => ejecutar(ejecutar(e,primera), Secuencia(resto))
}
```
 
### 2.3 Teorema a demostrar
 
```math
\forall o \in \text{Orden}, \ \forall e \in \text{Estado} : P_f(e, o) == f(e, o)
```
 
### 2.4 Estrategia
 
`Orden` es un conjunto definido recursivamente:
 
- **Casos básicos:** $\text{Avanzar}(n)$ y $\text{Girar}(s)$.
- **Casos recursivos:** $\text{Repetir}(k, o')$ y $\text{Secuencia}(\text{List}(o_1, \ldots, o_m))$, construidos a partir de órdenes $o'$, $o_i$ más pequeñas.
Se usa **inducción estructural sobre `Orden`**. La hipótesis de inducción (HI) es: para toda subórden $o'$ y **para todo estado** $e'$, $P_f(e', o') == f(e', o')$. Es importante que la HI quede cuantificada sobre todos los estados, porque las llamadas recursivas se hacen con estados distintos al inicial.
 
En los casos `Repetir` y `Secuencia`, la llamada recursiva exterior **no** es sobre una subórden estricta (`Repetir(veces-1, orden)` y `Secuencia(resto)` son órdenes del mismo "nivel"), así que dentro de cada uno se necesita una **inducción auxiliar** sobre `veces` y sobre la longitud de la lista, respectivamente.
 
### 2.5 Casos base
 
**Caso `Avanzar(n)`.** Se tiene $e = \text{Estado}(x, y, m)$. Por el modelo de sustitución, `ejecutar(e, Avanzar(n))` evalúa `e.mira match {...}`, y según $m$:
 
```math
m = \text{Norte}: \ P_f(e, \text{Avanzar}(n)) \rightarrow \text{Estado}(x, y+n, \text{Norte}) = f(e, \text{Avanzar}(n))
```
 
```math
m = \text{Sur}: \ P_f(e, \text{Avanzar}(n)) \rightarrow \text{Estado}(x, y-n, \text{Sur}) = f(e, \text{Avanzar}(n))
```
 
```math
m = \text{Este}: \ P_f(e, \text{Avanzar}(n)) \rightarrow \text{Estado}(x+n, y, \text{Este}) = f(e, \text{Avanzar}(n))
```
 
```math
m = \text{Oeste}: \ P_f(e, \text{Avanzar}(n)) \rightarrow \text{Estado}(x-n, y, \text{Oeste}) = f(e, \text{Avanzar}(n))
```
 
En los cuatro casos coincide con la especificación.
 
**Caso `Girar(s)`.** Se tiene $e = \text{Estado}(x, y, m)$:
 
```math
P_f(e, \text{Girar}(s)) \rightarrow \text{Estado}(x, y, \text{girar}(m, s))
```
 
que es exactamente $f(e, \text{Girar}(s))$ por especificación. Aquí se usa la **corrección de `girar`**, demostrada en el Punto 1.
 
### 2.6 Caso inductivo: `Repetir(k, o)`
 
**HI estructural:** $\forall e' : P_f(e', o) == f(e', o)$.
 
Se demuestra por inducción sobre $k$ el enunciado auxiliar:
 
```math
Q(k): \ \forall e' \in \text{Estado} : P_f(e', \text{Repetir}(k, o)) == f_o^{\max(k,0)}(e')
```
 
**Base: $k \leq 0$.** Se cumple la guardia del tercer `case`:
 
```math
P_f(e', \text{Repetir}(k, o)) \rightarrow e' = f_o^{0}(e')
```
 
**Paso: $k + 1$ con $k \geq 0$.** Como $k + 1 > 0$, la guardia `veces <= 0` es falsa y se aplica el cuarto `case`:
 
```math
P_f(e', \text{Repetir}(k+1, o)) \rightarrow P_f(P_f(e', o), \text{Repetir}(k, o))
```
 
Por la HI estructural, $P_f(e', o) = f_o(e')$. Luego, por la HI auxiliar $Q(k)$ aplicada al estado $f_o(e')$:
 
```math
\rightarrow P_f(f_o(e'), \text{Repetir}(k, o)) = f_o^{k}(f_o(e')) = f_o^{k+1}(e')
```
 
que es $f(e', \text{Repetir}(k+1, o))$.
 
Por lo tanto $Q(k)$ vale para todo $k$ y el caso `Repetir` es correcto.
 
### 2.7 Caso inductivo: `Secuencia(l)`
 
**HI estructural:** para cada $o_i$ de la lista, $\forall e' : P_f(e', o_i) == f(e', o_i)$.
 
Se demuestra por inducción sobre la longitud $m$ de la lista el enunciado auxiliar:
 
```math
R(m): \ \forall e' : P_f(e', \text{Secuencia}(\text{List}(o_1, \ldots, o_m))) == f_{o_m}(\cdots f_{o_1}(e') \cdots)
```
 
**Base: $m = 0$.**
 
```math
P_f(e', \text{Secuencia}(\text{Nil})) \rightarrow e'
```
 
y por especificación una secuencia vacía no modifica el estado.
 
**Paso: $m + 1$ con $m \geq 0$.** La lista es $o_1 :: \text{resto}$ con $\text{resto} = \text{List}(o_2, \ldots, o_{m+1})$:
 
```math
P_f(e', \text{Secuencia}(o_1 :: \text{resto})) \rightarrow P_f(P_f(e', o_1), \text{Secuencia}(\text{resto}))
```
 
Por la HI estructural, $P_f(e', o_1) = f_{o_1}(e')$. Por la HI auxiliar $R(m)$ aplicada al estado $f_{o_1}(e')$:
 
```math
\rightarrow f_{o_{m+1}}(\cdots f_{o_2}(f_{o_1}(e')) \cdots)
```
 
que es $f(e', \text{Secuencia}(\text{List}(o_1, \ldots, o_{m+1})))$.
 
Por lo tanto $R(m)$ vale para todo $m$ y el caso `Secuencia` es correcto.
 
### 2.8 Terminación
 
Se define un tamaño para las órdenes:
 
- $\text{tam}(\text{Avanzar}(n)) = \text{tam}(\text{Girar}(s)) = 1$.
- $\text{tam}(\text{Secuencia}(l)) = 1 + \sum_{o_i \in l} \text{tam}(o_i)$.
- $\text{tam}(\text{Repetir}(k, o)) = 1 + \max(k, 0) \cdot \text{tam}(o)$.
Cada llamada recursiva se hace sobre una orden de menor tamaño:
 
- `Repetir(veces, orden)` con `veces > 0`: llama a `orden` (tamaño menor, pues `veces`$\geq 1$) y a `Repetir(veces-1, orden)` (tamaño $1 + (k-1)\,\text{tam}(o) <1 + k\,\text{tam}(o)$).
- `Secuencia(primera::resto)`: llama a `primera` y a `Secuencia(resto)`, ambas de tamaño estrictamente menor.
Como el tamaño es un natural que decrece en cada llamada, se llega siempre a un caso básico (`Avanzar`, `Girar`, `Repetir` con `veces <= 0` o `Secuencia(Nil)`), y `ejecutar` termina siempre.
 
### 2.9 Conclusión
 
Se demostró por inducción estructural sobre `Orden` que
 
```math
\forall o \in \text{Orden}, \ \forall e \in \text{Estado} : P_f(e, o) == f(e, o)
```
 
Por lo tanto `ejecutar` es correcta respecto a su especificación.
 
### 2.10 Ejemplo de ejecución (opcional, ilustrativo)
 
Con $e_0 = \text{Estado}(0, 0, \text{Norte})$ y $o = \text{Secuencia}(\text{List}(\text{Avanzar}(2), \text{Girar}(\text{Derecha}), \text{Avanzar}(3)))$:
 
```math
\begin{aligned}
P_f(e_0, o) &\rightarrow P_f(P_f(e_0, \text{Avanzar}(2)), \text{Secuencia}(\text{List}(\text{Girar}(\text{Derecha}), \text{Avanzar}(3)))) \\
&\rightarrow P_f(\text{Estado}(0,2,\text{Norte}), \text{Secuencia}(\ldots)) \\
&\rightarrow P_f(\text{Estado}(0,2,\text{Este}), \text{Secuencia}(\text{List}(\text{Avanzar}(3)))) \\
&\rightarrow \text{Estado}(3, 2, \text{Este})
\end{aligned}
```
 
---
 
## Punto 3: `cuantasBasicas` y `profundidad`
 
Ambas funciones son recursivas sobre `Orden`, por lo que se demuestran por **inducción estructural**, igual que `ejecutar`. Para la terminación se usa el **tamaño estructural** de una orden:
 
- $\text{tam}_e(\text{Avanzar}(n)) = \text{tam}_e(\text{Girar}(s)) = 1$.
- $\text{tam}_e(\text{Repetir}(k, o)) = 1 + \text{tam}_e(o)$.
- $\text{tam}_e(\text{Secuencia}(\text{List}(o_1, \ldots, o_m))) = 1 + \sum_{i=1}^{m} \text{tam}_e(o_i)$.
(Es distinto de la medida del Punto 2. Allí hacía falta considerar `veces` porque `ejecutar` se llamaba sobre `Repetir(veces-1, orden)`. Aquí ninguna llamada repite la misma orden con otro contador.)
 
Como $\text{tam}_e \geq 1$ siempre, cada llamada recursiva de estas funciones es sobre una orden de menor $\text{tam}_e$:
 
- `orden` dentro de `Repetir(k, orden)`: $\text{tam}_e(o) < 1 + \text{tam}_e(o)$.
- `primera` dentro de `Secuencia(primera::resto)`: es un sumando de la suma.
- `Secuencia(resto)`: su tamaño es $1 + \sum_{i \geq 2} \text{tam}_e(o_i)$, menor que $1 + \text{tam}_e(o_1) + \sum_{i \geq 2} \text{tam}_e(o_i)$.
Por lo tanto, ambas funciones terminan siempre.
 
### 3.1 Función `cuantasBasicas`
 
#### Especificación
 
Sea $c : \text{Orden} \to \mathbb{N}$ la función que cuenta cuántas órdenes `Avanzar` y `Girar` llega a ejecutar una orden, contando las repeticiones. Siguiendo la semántica de `ejecutar` (Punto 2), un `Repetir(k, o)` ejecuta $o$ exactamente $\max(k, 0)$ veces:
 
- $c(\text{Avanzar}(n)) = 1$ y $c(\text{Girar}(s)) = 1$.
- $c(\text{Repetir}(k, o)) = \max(k, 0) \cdot c(o)$.
- $c(\text{Secuencia}(\text{List}(o_1, \ldots, o_m))) = \sum_{i=1}^{m} c(o_i)$ (vale $0$ si $m = 0$).
#### Programa
 
```scala
def cuantasBasicas(o: Orden): Int = o match {
  case Avanzar(_) => 1
  case Girar(_) => 1
  case Repetir(veces, _) if veces <= 0 => 0
  case Repetir(veces, orden) => veces * cuantasBasicas(orden)
  case Secuencia(Nil) => 0
  case Secuencia(primera::resto) => cuantasBasicas(primera) + cuantasBasicas(Secuencia(resto))
}
```
 
#### Teorema a demostrar
 
```math
\forall o \in \text{Orden} : P_c(o) == c(o)
```
 
#### Casos base
 
**`Avanzar(n)`:** $P_c(\text{Avanzar}(n)) \rightarrow 1 = c(\text{Avanzar}(n))$.
 
**`Girar(s)`:** $P_c(\text{Girar}(s)) \rightarrow 1 = c(\text{Girar}(s))$.
 
#### Caso inductivo: `Repetir(k, o)`
 
**HI estructural:** $P_c(o) == c(o)$.
 
- Si $k \leq 0$, se cumple la guardia del tercer `case`: $P_c(\text{Repetir}(k, o)) \rightarrow 0 = 0 \cdot c(o) = \max(k, 0) \cdot c(o)$.
- Si $k > 0$, se aplica el cuarto `case`:
```math
P_c(\text{Repetir}(k, o)) \rightarrow k \ast P_c(o) \overset{\text{HI}}{=} k \cdot c(o) = \max(k, 0) \cdot c(o)
```
 
En ambos casos coincide con $c(\text{Repetir}(k, o))$.
 
#### Caso inductivo: `Secuencia(l)`
 
**HI estructural:** $P_c(o_i) == c(o_i)$ para cada $o_i$ de la lista.
 
Como `Secuencia(resto)` no es una subórden estricta, se usa una inducción auxiliar sobre la longitud $m$ de la lista:
 
```math
R(m): \ P_c(\text{Secuencia}(\text{List}(o_1, \ldots, o_m))) == \sum_{i=1}^{m} c(o_i)
```
 
**Base: $m = 0$.** $P_c(\text{Secuencia}(\text{Nil})) \rightarrow 0$, que es la suma vacía.
 
**Paso: $m + 1$.** Con $\text{resto} = \text{List}(o_2, \ldots, o_{m+1})$:
 
```math
P_c(\text{Secuencia}(o_1 :: \text{resto})) \rightarrow P_c(o_1) + P_c(\text{Secuencia}(\text{resto}))
```
 
Por la HI estructural, $P_c(o_1) = c(o_1)$. Por $R(m)$, $P_c(\text{Secuencia}(\text{resto})) = \sum_{i=2}^{m+1} c(o_i)$. Entonces:
 
```math
\rightarrow c(o_1) + \sum_{i=2}^{m+1} c(o_i) = \sum_{i=1}^{m+1} c(o_i)
```
 
#### Conclusión
 
Por inducción estructural, $\forall o \in \text{Orden} : P_c(o) == c(o)$. `cuantasBasicas` es correcta.
 
### 3.2 Función `profundidad`
 
#### Especificación
 
Sea $g : \text{Orden} \to \mathbb{N}$ la función que da los niveles de anidamiento de una orden: una básica tiene profundidad 1 y una compuesta tiene uno más que la más honda de sus componentes. Para una secuencia vacía se toma 1:
 
- $g(\text{Avanzar}(n)) = g(\text{Girar}(s)) = 1$.
- $g(\text{Repetir}(k, o)) = 1 + g(o)$.
- $g(\text{Secuencia}(\text{List}(o_1, \ldots, o_m))) = 1 + \max(0, g(o_1), \ldots, g(o_m))$. Con $m = 0$ vale $1$.
Para abreviar se escribe $M(o_1, \ldots, o_m) = \max(0, g(o_1), \ldots, g(o_m))$.
 
#### Programa
 
```scala
def profundidad(o: Orden): Int = o match {
  case Avanzar(_) => 1
  case Girar(_) => 1
  case Repetir(_, orden) => 1 + profundidad(orden)
  case Secuencia(Nil) => 1
  case Secuencia(primera::resto) =>
    val p1 = profundidad(primera)
    val pResto = profundidad(Secuencia(resto)) - 1
    1 + (if (p1 > pResto) p1 else pResto)
}
```
 
#### Teorema a demostrar
 
```math
\forall o \in \text{Orden} : P_g(o) == g(o)
```
 
#### Casos base
 
**`Avanzar(n)` y `Girar(s)`:** $P_g(o) \rightarrow 1 = g(o)$.
 
#### Caso inductivo: `Repetir(k, o)`
 
**HI estructural:** $P_g(o) == g(o)$.
 
```math
P_g(\text{Repetir}(k, o)) \rightarrow 1 + P_g(o) \overset{\text{HI}}{=} 1 + g(o) = g(\text{Repetir}(k, o))
```
 
#### Caso inductivo: `Secuencia(l)`
 
**HI estructural:** $P_g(o_i) == g(o_i)$ para cada $o_i$ de la lista.
 
Inducción auxiliar sobre la longitud $m$ de la lista:
 
```math
R(m): \ P_g(\text{Secuencia}(\text{List}(o_1, \ldots, o_m))) == 1 + M(o_1, \ldots, o_m)
```
 
**Base: $m = 0$.** $P_g(\text{Secuencia}(\text{Nil})) \rightarrow 1 = 1 + M() = 1 + 0$.
 
**Paso: $m + 1$.** Con $\text{resto} = \text{List}(o_2, \ldots, o_{m+1})$, el programa calcula
 
```math
p_1 = P_g(o_1) \overset{\text{HI}}{=} g(o_1)
```
 
```math
p_{resto} = P_g(\text{Secuencia}(\text{resto})) - 1 \overset{R(m)}{=} (1 + M(o_2, \ldots, o_{m+1})) - 1 = M(o_2, \ldots, o_{m+1})
```
 
Entonces el resultado es:
 
```math
1 + \max(p_1, p_{resto}) = 1 + \max\big(g(o_1), \max(0, g(o_2), \ldots, g(o_{m+1}))\big) = 1 + M(o_1, \ldots, o_{m+1})
```
 
La última igualdad es por asociatividad y conmutatividad de $\max$. El `- 1` de `pResto` es justamente lo que "deshace" el $1 +$ que agrega cada `Secuencia`, de modo que la comparación se hace entre alturas de componentes.
 
#### Conclusión
 
Por inducción estructural, $\forall o \in \text{Orden} : P_g(o) == g(o)$. `profundidad` es correcta.
 
---
 
## Punto 4: `componer`, `rutina`, `aplicarA` y `transformar`
 
### 4.1 Función `componer`
 
#### Especificación
 
Sea $f_{comp} : \text{Orden} \times \text{Orden} \to \text{Orden}$ la función que da la secuencia de las dos órdenes: $f_{comp}(o_1, o_2) = \text{Secuencia}(\text{List}(o_1, o_2))$.
 
#### Programa
 
```scala
def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1,o2))
```
 
#### Demostración
 
`componer` **no es recursiva** y no hay casos que analizar. Por el modelo de sustitución, para cualesquiera $o_1, o_2$:
 
```math
P_{comp}(o_1, o_2) \rightarrow \text{Secuencia}(\text{List}(o_1, o_2)) = f_{comp}(o_1, o_2)
```
 
Termina siempre porque no tiene llamadas recursivas.
 
**Propiedad adicional (semántica).** Usando la corrección de `ejecutar` (Punto 2), `componer` hace lo que se espera: ejecutar la composición es ejecutar $o_1$ y luego $o_2$:
 
```math
\text{ejecutar}(e, \text{componer}(o_1, o_2)) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), \text{Secuencia}(\text{List}(o_2)))
```
 
```math
\rightarrow \text{ejecutar}(\text{ejecutar}(\text{ejecutar}(e, o_1), o_2), \text{Secuencia}(\text{Nil})) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)
```
 
### 4.2 Función `rutina`
 
#### Especificación
 
Sea $f_{rut}(p, s, v)$ la orden que repite $v$ veces la secuencia "avanzar $p$ pasos y girar en el sentido $s$":
 
```math
f_{rut}(p, s, v) = \text{Repetir}(v, \text{Secuencia}(\text{List}(\text{Avanzar}(p), \text{Girar}(s))))
```
 
#### Programa
 
```scala
def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))
```
 
#### Demostración
 
`rutina` **no es recursiva**. Estar currificada no cambia lo que calcula: `rutina(p)(s)` es una función de tipo `Int => Orden` que, aplicada a `v`, evalúa el mismo cuerpo con `pasos = p`, `giro = s`, `veces = v`. Entonces, para cualesquiera $p, s, v$:
 
```math
P_{rut}(p)(s)(v) \rightarrow \text{Repetir}(v, \text{Secuencia}(\text{List}(\text{Avanzar}(p), \text{Girar}(s)))) = f_{rut}(p, s, v)
```
 
Termina siempre porque no tiene llamadas recursivas.
 
**Propiedad adicional.** Usando la corrección de `cuantasBasicas` (Punto 3):
 
```math
c(\text{Secuencia}(\text{List}(\text{Avanzar}(p), \text{Girar}(s)))) = 1 + 1 + 0 = 2
```
 
```math
c(\text{rutina}(p)(s)(v)) = \max(v, 0) \cdot 2
```
 
### 4.3 Función `aplicarA`
 
#### Especificación
 
Sea $f_{ap} : \text{List}[\text{Estado}] \times \text{Orden} \to \text{List}[\text{Estado}]$ la función que ejecuta la misma orden sobre cada estado, conservando el orden de la lista:
 
```math
f_{ap}(\text{List}(e_1, \ldots, e_n), o) = \text{List}(\text{ejecutar}(e_1, o), \ldots, \text{ejecutar}(e_n, o))
```
 
En particular $f_{ap}(\text{Nil}, o) = \text{Nil}$. Aquí `ejecutar` es la función $f$ del Punto 2, y por la corrección demostrada allí coincide con el programa.
 
#### Programa
 
```scala
def aplicarA(estados: List[Estado], o: Orden): List[Estado] = estados match {
  case Nil => Nil
  case cabeza::cola => ejecutar(cabeza,o) +: aplicarA(cola,o)
}
```
 
#### Teorema a demostrar
 
```math
\forall o \in \text{Orden}, \ \forall n \geq 0, \ \forall e_1, \ldots, e_n : P_{ap}(\text{List}(e_1, \ldots, e_n), o) == f_{ap}(\text{List}(e_1, \ldots, e_n), o)
```
 
#### Demostración
 
`List[Estado]` es un conjunto definido recursivamente (`Nil` y `cabeza::cola`), así que se usa **inducción estructural sobre la lista**, con $o$ fijo.
 
**Caso base: `Nil`.**
 
```math
P_{ap}(\text{Nil}, o) \rightarrow \text{Nil} = f_{ap}(\text{Nil}, o)
```
 
**Caso inductivo: $e_1 :: \text{cola}$** con $\text{cola} = \text{List}(e_2, \ldots, e_{n})$.
 
**HI:** $P_{ap}(\text{cola}, o) == \text{List}(\text{ejecutar}(e_2, o), \ldots, \text{ejecutar}(e_n, o))$.
 
```math
P_{ap}(e_1 :: \text{cola}, o) \rightarrow \text{ejecutar}(e_1, o) +: P_{ap}(\text{cola}, o)
```
 
Como `x +: xs` antepone `x` a la lista `xs`, y usando la HI:
 
```math
\rightarrow \text{ejecutar}(e_1, o) +: \text{List}(\text{ejecutar}(e_2, o), \ldots, \text{ejecutar}(e_n, o)) = \text{List}(\text{ejecutar}(e_1, o), \ldots, \text{ejecutar}(e_n, o))
```
 
que es $f_{ap}(e_1 :: \text{cola}, o)$.
 
**Terminación:** en cada llamada la lista `cola` es más corta que `estados`, y por la corrección de `ejecutar` (que termina, Punto 2) cada evaluación de `ejecutar(cabeza, o)` termina.
 
**Conclusión:** `aplicarA` es correcta respecto a su especificación.
 
### 4.4 Función `transformar`
 
#### Especificación
 
Sea $h : \text{Orden} \times (\text{Orden} \to \text{Orden}) \to \text{Orden}$ la función que reemplaza cada orden básica $b$ por $\varphi(b)$ (el parámetro `f` del programa se escribe aquí $\varphi$ para no confundirlo con la especificación), respetando la estructura de las compuestas. **Precondición:** $\varphi$ es total y termina para toda orden básica.
 
- $h(\text{Avanzar}(n), \varphi) = \varphi(\text{Avanzar}(n))$ y $h(\text{Girar}(s), \varphi) = \varphi(\text{Girar}(s))$.
- $h(\text{Repetir}(k, o), \varphi) = \text{Repetir}(k, h(o, \varphi))$.
- $h(\text{Secuencia}(\text{List}(o_1, \ldots, o_m)), \varphi) = \text{Secuencia}(\text{List}(h(o_1, \varphi), \ldots, h(o_m, \varphi)))$.
#### Programa
 
```scala
def transformar(o: Orden, f: Orden => Orden): Orden = o match {
  case Avanzar(_) => f(o)
  case Girar(_) => f(o)
  case Repetir(veces, orden) => Repetir(veces, transformar(orden, f))
  case Secuencia(Nil) => Secuencia(Nil)
  case Secuencia(head::tail) => transformar(head, f) match {
    case tHead => transformar(Secuencia(tail), f) match {
      case Secuencia(tTail) => Secuencia(tHead :: tTail)
      case otra => Secuencia(List(tHead, otra))
    }
  }
}
```
 
#### Teorema a demostrar
 
```math
\forall o \in \text{Orden}, \ \forall \varphi : P_t(o, \varphi) == h(o, \varphi)
```
 
#### Casos base
 
**`Avanzar(n)` y `Girar(s)`:** $P_t(o, \varphi) \rightarrow \varphi(o) = h(o, \varphi)$.
 
#### Caso inductivo: `Repetir(k, o)`
 
**HI estructural:** $P_t(o, \varphi) == h(o, \varphi)$.
 
```math
P_t(\text{Repetir}(k, o), \varphi) \rightarrow \text{Repetir}(k, P_t(o, \varphi)) \overset{\text{HI}}{=} \text{Repetir}(k, h(o, \varphi)) = h(\text{Repetir}(k, o), \varphi)
```
 
Se conserva `veces` tal cual, incluso si es no positivo, como pide la especificación.
 
#### Caso inductivo: `Secuencia(l)`
 
**HI estructural:** $P_t(o_i, \varphi) == h(o_i, \varphi)$ para cada $o_i$ de la lista.
 
Inducción auxiliar sobre la longitud $m$ de la lista:
 
```math
R(m): \ P_t(\text{Secuencia}(\text{List}(o_1, \ldots, o_m)), \varphi) == \text{Secuencia}(\text{List}(h(o_1, \varphi), \ldots, h(o_m, \varphi)))
```
 
**Base: $m = 0$.** $P_t(\text{Secuencia}(\text{Nil}), \varphi) \rightarrow \text{Secuencia}(\text{Nil}) = \text{Secuencia}(\text{List}())$.
 
**Paso: $m + 1$.** Con $\text{tail} = \text{List}(o_2, \ldots, o_{m+1})$:
 
1. `tHead` se liga a $P_t(o_1, \varphi)$, que por la HI estructural es $h(o_1, \varphi)$. (El patrón `case tHead` es una variable, así que siempre coincide.)
2. Por $R(m)$, `transformar(Secuencia(tail), f)` evalúa a $\text{Secuencia}(\text{List}(h(o_2, \varphi), \ldots, h(o_{m+1}, \varphi)))$. Este valor **siempre es una `Secuencia`**, de modo que el patrón `Secuencia(tTail)` coincide con $tTail = \text{List}(h(o_2, \varphi), \ldots, h(o_{m+1}, \varphi))$.
3. El resultado es:
```math
\text{Secuencia}(tHead :: tTail) = \text{Secuencia}(\text{List}(h(o_1, \varphi), h(o_2, \varphi), \ldots, h(o_{m+1}, \varphi)))
```
 
que es $h(\text{Secuencia}(\text{List}(o_1, \ldots, o_{m+1})), \varphi)$.
 
**Observación:** como el resultado de `transformar` sobre una `Secuencia` siempre es una `Secuencia` (lo que da $R(m)$), el caso `case otra => ...` **nunca se ejecuta**. Es código inalcanzable que no afecta la corrección.
 
#### Terminación
 
En cada llamada recursiva el argumento tiene menor $\text{tam}_e$ (`orden`, `head` y `Secuencia(tail)`, por el argumento del inicio del Punto 3). Además $\varphi$ termina por la precondición, así que `transformar` termina siempre.
 
#### Conclusión
 
Por inducción estructural, $\forall o, \varphi : P_t(o, \varphi) == h(o, \varphi)$. `transformar` es correcta respecto a su especificación.````