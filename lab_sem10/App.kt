//Mauricio Fragachan 20-10265
//Gabriel De Ornelas 15-10377

package org.example // generado por Gradle como parte del esqueleto inicial
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JFrame
import kotlin.concurrent.timer
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.DataFrame

class App {val greeting: String get(){return "Hello World!"}} // generado por Gradle como parte del esqueleto inicial

const val TIME_LM = 5 // tiempo límite en segundos
const val TSP_DIR = "C:/TEMP/TSP05/TSP" // carpeta con tests TSP (en caso que no pueda encontrarse en ../tsp)
val tspAlgoritmos = listOf("fuerza_bruta", "mejor_vecino", "insercion1", "insercion2") // algoritmos implementados

fun seleccionarTsp(): List<String> {
    var targetDirectory = Paths.get("").toAbsolutePath().resolve("../tsp").normalize()
    if (!Files.exists(targetDirectory) || !Files.isDirectory(targetDirectory)) {
        targetDirectory = Paths.get(TSP_DIR) 
    }
    val tspDir = targetDirectory.toFile()
    val fileChooser = JFileChooser(tspDir)
    fileChooser.dialogTitle = "Seleccionar archivos *.tsp de $tspDir"
    fileChooser.fileFilter = FileNameExtensionFilter("Archivos de tsp (*.tsp)", "tsp")
    fileChooser.isMultiSelectionEnabled = true
    val frame = JFrame()
    frame.isVisible = false
    val result = fileChooser.showOpenDialog(frame)
    frame.dispose()
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFiles.map { it.absolutePath }
    } else {
        emptyList()
    }
}

fun <T> List<T>.permutations(): Sequence<List<T>> = sequence {
    if (this@permutations.isEmpty()) {
        yield(emptyList())
    } else {
        for (i in this@permutations.indices) {
            val element = this@permutations[i]
            val rest = this@permutations.take(i) + this@permutations.drop(i + 1)
            for (perm in rest.permutations()) {
                yield(listOf(element) + perm)
            }
        }
    }
}

class tspTest(filename: String){
    var name: String
    var optimo: Double
    var comentario: String
    var matriz: List<List<Double>>
    var n : Int
    init {
        val lines = File(filename).readLines()
        name = lines[0].trim()
        optimo = lines[1].trim().toDouble()
        comentario = lines[2].trim()
        matriz = lines.drop(3).map{it.split(" ").map(String::toDouble)}
        n = matriz.size
    }
}

class tspExec(testinst: tspTest, algname: String) {
    val algoritmos = mapOf(
        "fuerza_bruta" to ::fuerzaBruta,
        "mejor_vecino" to ::mejorVecino,
        "insercion1" to ::insercion1,
        "insercion2" to ::insercion2
    )
    var algoritmo: String
    var efic: Double
    var mejorDist: Double
    var mejorRuta: List<Int>?
    var salir : Boolean
    var test : tspTest
    var tiempoIni : Long
    var tiempoFin : Long
    var temporizador: Timer

    private fun interrupcion() {
        salir = true
    }

    init {
        if (algname in algoritmos) {
            algoritmo = algname
            efic = 0.0
            mejorDist = Double.POSITIVE_INFINITY
            mejorRuta = mutableListOf()
            salir = false
            test = testinst
            tiempoIni = System.currentTimeMillis()
            temporizador = timer("temporizador",false,TIME_LM*1000L,TIME_LM*1000L){interrupcion()}
            algoritmos[algname]?.invoke()
            tiempoFin = System.currentTimeMillis()
        } else {
            throw IllegalArgumentException("Algoritmo '$algname' no existe.")
        }
    }

    override fun toString(): String {
        val info = "${test.name} $algoritmo d=%.2f o=%.2f e=%.2f t=%.2fs".format(mejorDist,test.optimo,efic,(tiempoFin-tiempoIni)/1000.0)
        val localMejorRuta = mejorRuta
        if (localMejorRuta.isNullOrEmpty()){return info}
        val i = localMejorRuta.indexOf(0)
        val rutaNormal = localMejorRuta.subList(i, localMejorRuta.size) + localMejorRuta.subList(0, i)
        val rutaInvers = listOf(rutaNormal[0]) + rutaNormal.subList(1, rutaNormal.size).reversed()
        return "$info ${if (rutaNormal[1] < rutaInvers[1]) rutaNormal else rutaInvers}}"
    }

    private fun calcDist(ruta: List<Int>): Double {
        var dist = 0.0
        for (i in 0 until ruta.size - 1) dist += test.matriz[ruta[i]][ruta[i + 1]]
        dist += test.matriz[ruta.last()][ruta[0]]
        return dist
    }

    private fun updMejor(dist: Double, ruta: List<Int>) {
        mejorDist = dist
        mejorRuta = ruta
        efic = mejorDist / test.optimo
    }

    private fun fuerzaBruta() {
        try {
            val ciudades = (0 until test.n).toList()
            for (ruta in ciudades.permutations()) {
                val distancia = calcDist(ruta)
                if (distancia < mejorDist) updMejor(distancia, ruta)
                if(salir)break
            }
        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }

    private fun mejorVecino() {
        try {
            val n = test.n
            for(i in 0 until n){    // Generaremos recorridos iniciando desde cada vertice
                val visitadas = BooleanArray(n)   // Inicializar array para llevar un registro de las ciudades visitadas.
                val tour = mutableListOf<Int>() 
                var actual = i   // El recorrido parte desde el vertice i.
                tour.add(actual)
                visitadas[actual] = true
                // Seleccionar iterativamente la ciudad no visitada más cercana y se añade al recorrido.
                while (tour.size < n) {
                    val ultima = tour.last()
                    var mejorDist = Double.POSITIVE_INFINITY
                    var mejorCiudad = -1

                    for (ciudad in 0 until n) {
                        if (!visitadas[ciudad] && test.matriz[ultima][ciudad] < mejorDist) {
                            mejorDist = test.matriz[ultima][ciudad]
                            mejorCiudad = ciudad
                        }
                    }
                    if (mejorCiudad != -1) {
                        tour.add(mejorCiudad)
                        visitadas[mejorCiudad] = true
                    }
                }
                
                val distancia = calcDist(tour) // Calcula la distancia total del recorrido.
                if (distancia < mejorDist){
                    updMejor(distancia, tour) // Actualiza el mejor recorrido conocido y su distancia si el recorrido actual es mejor.
                }
            }
            
        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }

    private fun insercion1() {
        try {/*
            La heurística funciona de la siguiente manera:
            1. Comienza con las ciudades 0 y 1 para iniciar el recorrido.
            2. Mientras haya ciudades no visitadas:
                a. Inicializa variables para rastrear el mejor aumento en la distancia, la mejor ciudad para agregar y la mejor posición para insertar la ciudad.
                b. Para cada ciudad no visitada, evalúa el impacto de insertar la ciudad en cada posición posible en el recorrido actual.
                c. Selecciona la ciudad y la posición que resulten en el menor aumento en la distancia total del recorrido.
                d. Inserta la ciudad seleccionada en el recorrido en la mejor posición y se marca como visitada.
            3. Calcula la distancia total del recorrido final.
            4. Actualiza el mejor recorrido conocido y su distancia si el recorrido actual es mejor.
             */
            val n = test.n // Número de ciudades en el problema.
            val tour = mutableListOf<Int>()  // Inicializa una lista para almacenar el recorrido.
            tour.add(0) // Añade las ciudades 0 y 1 al recorrido
            tour.add(1)
            for (ciudad in 2 until n) {   //Agregaremos las ciudades 2, luego 3, luego... hasta n en las mejores posiciones
                var mejorPosicion = -1 // Inicializa la mejor posición para insertar la ciudad.
                var mejorAumento = Double.POSITIVE_INFINITY // Inicializa el mejor aumento en la distancia.
                for (pos in 0..tour.size) {
                    val tourTemp = tour.toMutableList()
                    tourTemp.add(pos, ciudad)
                    val aumento = calcDist(tourTemp) - calcDist(tour)
                    if (aumento < mejorAumento) {
                        mejorAumento = aumento
                        mejorPosicion = pos
                    }
                }
                tour.add(mejorPosicion, ciudad) //Agrega la ciudad en la mejor posicion
            }
            val distancia = calcDist(tour) // Calcula la distancia total del recorrido.
            updMejor(distancia, tour) // Actualiza el mejor recorrido conocido y su distancia si el recorrido actual es mejor.

        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }

    private fun insercion2() {
        try {
            /*
                La heurística funciona de la siguiente manera:
             1. Comienza con las ciudades 0 y 1 para iniciar el recorrido.
             2. Mientras haya ciudades no visitadas:
                a. Se identifica la ciudad no visitada más cercana a cualquier ciudad ya incluida en el tour.
                b. Se inserta esta ciudad en la posición del tour que minimice el incremento de la distancia total del recorrido.
             3. Finalmente, se calcula la distancia total del tour y se actualiza si es la mejor encontrada.
             */
            val n = test.n // Número de ciudades en el problema.
            val tour = mutableListOf<Int>() // Inicializa una lista para almacenar el recorrido.
            val noVisitadas = (2 until n).toMutableList() // Inicializa una lista de ciudades no visitadas.
            tour.add(0) // Añade las ciudades 0 y 1 al recorrido.
            tour.add(1)

            while (noVisitadas.isNotEmpty()) {
                var mejorCiudad = -1 // Inicializa la mejor ciudad para agregar.
                var mejorDist = Double.POSITIVE_INFINITY // Inicializa la distancia mínima encontrada.

                for (ciudad in noVisitadas) {
                    for (visitada in tour) {
                        val dist = test.matriz[visitada][ciudad]
                        if (dist < mejorDist) {
                            mejorDist = dist
                            mejorCiudad = ciudad
                        }
                    }
                }

                var mejorPosicion = -1
                var menorAumento = Double.POSITIVE_INFINITY

                for (pos in 0..tour.size) {
                    val tourTemp = tour.toMutableList()
                    tourTemp.add(pos, mejorCiudad)
                    val aumento = calcDist(tourTemp) - calcDist(tour)
                    if (aumento < menorAumento) {
                        menorAumento = aumento
                        mejorPosicion = pos
                    }
                }

                if (mejorCiudad != -1) {
                    tour.add(mejorPosicion, mejorCiudad)
                    noVisitadas.remove(mejorCiudad)
                }
            }

            val distancia = calcDist(tour) // Calcula la distancia total del recorrido.
            updMejor(distancia, tour) // Actualiza el mejor recorrido conocido y su distancia si el recorrido actual es mejor.

        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }
}

fun main() {
    val tspData = seleccionarTsp().flatMap { tspFile ->
        val test = tspTest(tspFile)
        tspAlgoritmos.map{algoritmo -> tspExec(test,algoritmo).also{println(it)}}
    }
    if (tspData.isNotEmpty()) {
        val df = dataFrameOf(
            "Test" to tspData.map { it.test.name },
            "Algoritmo" to tspData.map { it.algoritmo },
            "Mejor_Dist" to tspData.map { it.mejorDist },
            "Optimo" to tspData.map { it.test.optimo },
            "Eficiencia" to tspData.map { it.efic },
            "Tiempo" to tspData.map {(it.tiempoFin-it.tiempoIni)/1000.0}
        )
        df.print(rowsLimit=Int.MAX_VALUE,borders=true)
        val reporte = df.groupBy("Algoritmo").aggregate {
            mean("Eficiencia") into "Eficiencia_AVG"
            std("Eficiencia") into "Eficiencia_STD"
            mean("Tiempo") into "Tiempo_AVG"
            std("Tiempo") into "Tiempo_STD"
        }
        reporte.print(rowsLimit=Int.MAX_VALUE,borders=true)
    } else {
        println("No se seleccionaron archivos TSP.")
    }
}
