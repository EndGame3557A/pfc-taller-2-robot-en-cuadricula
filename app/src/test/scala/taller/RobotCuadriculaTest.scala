package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))
  val todas: List[Direccion] = List(Norte, Este, Sur, Oeste)

  // Punto 1 -------------------------------------------------------------------

  test("girar: Norte a la derecha da Este") { assert(girar(Norte, Derecha) == Este) }
  test("girar: Norte a la izquierda da Oeste") { assert(girar(Norte, Izquierda) == Oeste) }
  test("girar: Oeste a la derecha da Norte") { assert(girar(Oeste, Derecha) == Norte) }
  test("girar: Sur a la izquierda da Este") { assert(girar(Sur, Izquierda) == Este) }

  test("girar: cuatro giros al mismo lado vuelven al principio") {
    val cuatro = List(Derecha, Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Norte)
  }

  test("girar: un giro y su contrario se cancelan") {
    assert(girar(girar(Este, Derecha), Izquierda) == Este)
  }

  //Pruebas adicionales-------------------------------------------------------
  test("girar: Este a la derecha da Sur") {
    assert(girar(Este, Derecha) == Sur)
  }
 
  test("girar: Sur a la derecha da Oeste") {
    assert(girar(Sur, Derecha) == Oeste)
  }
 
  test("girar: Oeste a la izquierda da Sur") {
    assert(girar(Oeste, Izquierda) == Sur)
  }
 
  test("girar: dos giros a la derecha equivalen a dos a la izquierda (media vuelta)") {
    assert(todas.forall(d =>
      girar(girar(d, Derecha), Derecha) == girar(girar(d, Izquierda), Izquierda)))
  }
 
  test("girar: un giro siempre cambia la dirección") {
    assert(todas.forall(d => girar(d, Derecha) != d && girar(d, Izquierda) != d))
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar tres al norte") {
    assert(ejecutar(inicial, Avanzar(3)) == Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de avance, giro y avance") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))) ==
      Estado(4, 2, Este))
  }

  test("ejecutar: el cuadrado vuelve al punto de partida") {
    assert(ejecutar(inicial, cuadrado) == Estado(0, 0, Norte))
  }

  test("ejecutar: avanzar con paso negativo retrocede") {
    assert(ejecutar(inicial, Avanzar(-2)) == Estado(0, -2, Norte))
  }

  test("ejecutar: repetir cero veces no hace nada") {
    assert(ejecutar(inicial, Repetir(0, Avanzar(5))) == inicial)
  }

  test("ejecutar: la secuencia vacía no hace nada") {
    assert(ejecutar(inicial, Secuencia(Nil)) == inicial)
  }

  test("ejecutar: avanzar al sur disminuye y") {
    assert(ejecutar(Estado(5, 5, Sur), Avanzar(2)) == Estado(5, 3, Sur))
  }

  test("ejecutar: avanzar al oeste disminuye x") {
    assert(ejecutar(Estado(5, 5, Oeste), Avanzar(2)) == Estado(3, 5, Oeste))
  }

  //Pruebas adicionales-------------------------------------------------------
  test("ejecutar: girar no cambia la posición, solo la dirección") {
    assert(ejecutar(Estado(2, 3, Este), Girar(Izquierda)) == Estado(2, 3, Norte))
  }
 
  test("ejecutar: repetir anidado multiplica las repeticiones") {
    assert(ejecutar(inicial, Repetir(2, Repetir(3, Avanzar(1)))) == Estado(0, 6, Norte))
  }
 
  test("ejecutar: repetir con veces negativas no hace nada") {
    assert(ejecutar(inicial, Repetir(-3, Avanzar(5))) == inicial)
  }
 
  test("ejecutar: secuencia anidada se ejecuta en orden") {
    val o = Secuencia(List(
      Avanzar(1),
      Secuencia(List(Girar(Derecha), Avanzar(2))),
      Girar(Izquierda),
      Avanzar(3)))
    assert(ejecutar(inicial, o) == Estado(2, 4, Norte))
  }
 
  test("ejecutar: un rectángulo de 3x1 vuelve al punto de partida") {
    val rectangulo = Repetir(2,
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(1), Girar(Derecha))))
    assert(ejecutar(inicial, rectangulo) == Estado(0, 0, Norte))
  }


  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: una orden básica cuenta uno") {
    assert(cuantasBasicas(Avanzar(3)) == 1)
  }

  test("cuantasBasicas: el cuadrado ejecuta ocho básicas") {
    assert(cuantasBasicas(cuadrado) == 8)
  }

  test("cuantasBasicas: repetir cero veces no ejecuta nada") {
    assert(cuantasBasicas(Repetir(0, cuadrado)) == 0)
  }

  test("cuantasBasicas: la secuencia vacía no ejecuta nada") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("profundidad: una orden básica tiene profundidad 1") {
    assert(profundidad(Avanzar(3)) == 1)
  }

  test("profundidad: una secuencia de básicas tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Avanzar(1), Girar(Derecha)))) == 2)
  }

  test("profundidad: el cuadrado tiene profundidad 3") {
    assert(profundidad(cuadrado) == 3)
  }

  test("profundidad: la secuencia vacía tiene profundidad 1") {
    assert(profundidad(Secuencia(Nil)) == 1)
  }

  test("profundidad: manda la rama más honda") {
    assert(profundidad(Secuencia(List(Avanzar(1), cuadrado))) == 4)
  }

  //Pruebas Adicionales -------------------------------------------------------

  test("cuantasBasicas: girar cuenta uno") {
    assert(cuantasBasicas(Girar(Izquierda)) == 1)
  }
 
  test("cuantasBasicas: repetir una secuencia de tres básicas tres veces da nueve") {
    assert(cuantasBasicas(
      Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha), Avanzar(2))))) == 9)
  }
 
  test("cuantasBasicas: repetir anidado multiplica") {
    assert(cuantasBasicas(Repetir(2, Repetir(3, Avanzar(1)))) == 6)
  }
 
  test("cuantasBasicas: suma las ramas de una secuencia con compuestas") {
    val o = Secuencia(List(
      Avanzar(1),
      Secuencia(List(Girar(Izquierda), Avanzar(2))),
      cuadrado))
    assert(cuantasBasicas(o) == 11)
  }
 
  test("cuantasBasicas: repetir con veces negativas no ejecuta nada") {
    assert(cuantasBasicas(Repetir(-2, Avanzar(1))) == 0)
  }
  test("profundidad: girar tiene profundidad 1") {
    assert(profundidad(Girar(Izquierda)) == 1)
  }
 
  test("profundidad: repetir una básica tiene profundidad 2") {
    assert(profundidad(Repetir(3, Avanzar(1))) == 2)
  }
 
  test("profundidad: repetir anidado suma un nivel por cada repetir") {
    assert(profundidad(Repetir(2, Repetir(3, Avanzar(1)))) == 3)
  }
 
  test("profundidad: secuencias anidadas tres veces tienen profundidad 4") {
    assert(profundidad(Secuencia(List(Secuencia(List(Secuencia(List(Avanzar(1))))))))
      == 4)
  }
 
  test("profundidad: repetir una secuencia vacía tiene profundidad 2") {
    assert(profundidad(Repetir(5, Secuencia(Nil))) == 2)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: deja las dos órdenes en secuencia") {
    assert(componer(Avanzar(1), Girar(Derecha)) ==
      Secuencia(List(Avanzar(1), Girar(Derecha))))
  }

  test("componer: ejecutar la compuesta es ejecutar una y después la otra") {
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) ==
      ejecutar(ejecutar(inicial, Avanzar(2)), Girar(Derecha)))
  }

  test("rutina: currificada, el cuadrado de cuatro vuelve al origen") {
    val cuadradoDe = rutina(1)(Derecha) _
    assert(ejecutar(inicial, cuadradoDe(4)) == Estado(0, 0, Norte))
  }

  test("rutina: con los mismos argumentos construye el cuadrado del enunciado") {
    assert(rutina(1)(Derecha)(4) == cuadrado)
  }

  test("aplicarA: ejecuta la misma orden sobre varios estados") {
    assert(aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2)) ==
      List(Estado(0, 2, Norte), Estado(5, 3, Sur)))
  }

  test("aplicarA: sobre la lista vacía no hay nada que hacer") {
    assert(aplicarA(Nil, Avanzar(2)) == Nil)
  }

  test("transformar: duplicar los avances deja los giros como estaban") {
    val doble = transformar(cuadrado, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }

  test("transformar: con la identidad no cambia nada") {
    assert(transformar(cuadrado, (o: Orden) => o) == cuadrado)
  }

  //Pruebas adicionales -------------------------------------------------------

  test("componer: el orden importa (girar y luego avanzar no es avanzar y luego girar)") {
    assert(ejecutar(inicial, componer(Girar(Derecha), Avanzar(2))) == Estado(2, 0, Este))
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) == Estado(0, 2, Este))
  }
 
  test("componer: acepta órdenes compuestas sin aplanarlas") {
    assert(componer(cuadrado, Avanzar(1)) == Secuencia(List(cuadrado, Avanzar(1))))
    assert(ejecutar(inicial, componer(cuadrado, Avanzar(1))) == Estado(0, 1, Norte))
  }
  test("rutina: el cuadrado de lado 2 girando a la izquierda vuelve al origen") {
    assert(ejecutar(inicial, rutina(2)(Izquierda)(4)) == Estado(0, 0, Norte))
  }
 
  test("rutina: dos repeticiones con pasos 3 a la derecha terminan en (3, 3) mirando al sur") {
    assert(ejecutar(inicial, rutina(3)(Derecha)(2)) == Estado(3, 3, Sur))
  }
 
  test("rutina: construye la estructura esperada con otros argumentos") {
    assert(rutina(2)(Izquierda)(3) ==
      Repetir(3, Secuencia(List(Avanzar(2), Girar(Izquierda)))))
  }
 
  test("aplicarA: mantiene el orden y aplica un giro a cada estado") {
    assert(aplicarA(List(Estado(1, 1, Este), Estado(0, 0, Oeste)), Girar(Derecha)) ==
      List(Estado(1, 1, Sur), Estado(0, 0, Norte)))
  }
 
  test("aplicarA: una orden que no hace nada devuelve los mismos estados") {
    val estados = List(inicial, Estado(5, 5, Sur), Estado(-2, 7, Oeste))
    assert(aplicarA(estados, Avanzar(0)) == estados)
  }
  test("transformar: cambiar los giros a la derecha por giros a la izquierda") {
    val espejo = transformar(cuadrado, {
      case Girar(Derecha) => Girar(Izquierda)
      case otra => otra
    })
    assert(espejo == Repetir(4, Secuencia(List(Avanzar(1), Girar(Izquierda)))))
  }
 
  test("transformar: sobre una orden básica aplica f directamente") {
    val mas1 = transformar(Avanzar(3), {
      case Avanzar(n) => Avanzar(n + 1)
      case otra => otra
    })
    assert(mas1 == Avanzar(4))
  }
 
  test("transformar: llega a las básicas dentro de repetir anidado") {
    val doble = transformar(Repetir(2, Repetir(3, Avanzar(1))), {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(2, Repetir(3, Avanzar(2))))
  }


  // Punto 5 -------------------------------------------------------------------

  test("simplificar: dos avances seguidos se juntan") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(3)))) == Avanzar(5))
  }

  test("simplificar: repetir una vez es la orden misma") {
    assert(simplificar(Repetir(1, Avanzar(4))) == Avanzar(4))
  }

  test("simplificar: avanzar cero desaparece de la secuencia") {
    assert(simplificar(Secuencia(List(Avanzar(0), Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta solo los avances contiguos") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3)))) ==
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3))))
  }

  test("simplificar: repetir cero veces no hace nada") {
    assert(simplificar(Repetir(0, Avanzar(9))) == Secuencia(Nil))
  }

  test("simplificar: dos avances que se cancelan desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2)))) == Secuencia(Nil))
  }

  test("simplificar: es idempotente") {
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Secuencia(List(Avanzar(0), Avanzar(4)))),
      Secuencia(List(Secuencia(Nil), Repetir(0, Avanzar(3)))))
    assert(ordenes.forall(o => simplificar(simplificar(o)) == simplificar(o)))
  }

  test("simplificar: no cambia el resultado de ejecutar") {
    val estados = List(inicial, Estado(3, -2, Este), Estado(-1, 4, Sur))
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(2), Avanzar(3))),
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Avanzar(4)),
      Repetir(0, Avanzar(9)),
      Secuencia(List(Avanzar(0), Girar(Derecha))),
      Secuencia(List(Avanzar(2), Avanzar(-2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(2)), Girar(Izquierda)))))
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: el anidamiento interior también se reduce") {
    assert(simplificar(Repetir(3, Repetir(1, Avanzar(2)))) == Repetir(3, Avanzar(2)))
  }

  //Pruebas Adicionales ---------------------------------------------------
  test("simplificar: una secuencia de un solo elemento es ese elemento") {
    assert(simplificar(Secuencia(List(Girar(Izquierda)))) == Girar(Izquierda))
  }
 
  test("simplificar: tres avances seguidos se juntan en uno") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Avanzar(3)))) == Avanzar(6))
  }
 
  test("simplificar: al juntar avances que suman cero también desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2), Girar(Derecha)))) ==
      Girar(Derecha))
  }
 
  test("simplificar: las órdenes que no hacen nada se quitan de la secuencia") {
    val o = Secuencia(List(Secuencia(Nil), Girar(Derecha), Repetir(0, Avanzar(3))))
    assert(simplificar(o) == Girar(Derecha))
  }
 
  test("simplificar: dos giros seguidos no se juntan") {
    val o = Secuencia(List(Girar(Derecha), Girar(Derecha)))
    assert(simplificar(o) == o)
  }
 
  test("simplificar: reduce el interior de un repetir") {
    assert(simplificar(Repetir(3, Secuencia(List(Avanzar(0), Avanzar(2))))) ==
      Repetir(3, Avanzar(2)))
  }
 
  test("simplificar: conserva el resultado de ejecutar en otras órdenes y estados") {
    val estados = List(inicial, Estado(-4, 2, Oeste), Estado(6, -1, Sur))
    val ordenes = List(
      Secuencia(List(Avanzar(3), Avanzar(-3), Girar(Izquierda), Avanzar(2), Avanzar(0))),
      Repetir(2, Secuencia(List(Avanzar(1), Avanzar(1), Girar(Derecha)))),
      Secuencia(List(Repetir(1, Girar(Derecha)), Secuencia(List(Avanzar(2)))))
    )
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: Avanzar(0) suelto desaparece") {
    assert(simplificar(Avanzar(0)) == Secuencia(Nil))
  }

  test("simplificar: repetir una orden que no hace nada no hace nada") {
    assert(simplificar(Repetir(3, Avanzar(0))) == Secuencia(Nil))
    assert(simplificar(Repetir(3, Secuencia(Nil))) == Secuencia(Nil))
  }

  test("simplificar: repetir con veces negativas desaparece") {
    assert(simplificar(Repetir(-2, Avanzar(1))) == Secuencia(Nil))
  }
}

