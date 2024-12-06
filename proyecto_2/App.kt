//Mauricio Fragachan 20-10265
//Gabriel De Ornelas 15-10377

package org.example

import org.jetbrains.kotlinx.dataframe.api.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Timer
import javax.swing.*
import kotlin.concurrent.timer
import kotlin.math.sqrt
import kotlin.random.Random

const val TIMEOUT: Long = 10L // tiempo límite en segundos por defecto
const val TSP_DIR: String = "C:/TSP" // carpeta con tests TSP
const val offsetOfWindows: Int = 30
val sizeOfWindows: Dimension = Dimension(600, 600)
var numberOfWindows: Int = 0
var showCharts: Boolean = true

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

class App {
    val greeting: String get() = "Hello World!"  // generado por Gradle como parte del esqueleto inicial
    private val frame = JFrame().apply {
        setLocation(numberOfWindows * offsetOfWindows, numberOfWindows * offsetOfWindows)
        numberOfWindows++
        isVisible = false
    }

    fun ending(texto: String) {
        message(texto)
        frame.dispose()
    }

    private fun message(texto: String) {
        println(texto)
        JOptionPane.showMessageDialog(frame, texto, "message", JOptionPane.INFORMATION_MESSAGE)
    }

    fun selectFiles(directory: String): List<String> {
        val fileChooser = JFileChooser(directory).apply {
            isMultiSelectionEnabled = true
        }
        return if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFiles.map { it.absolutePath }
        } else {
            emptyList()
        }
    }

    fun askCharts() {
        showCharts = JOptionPane.showConfirmDialog(
            frame,
            "¿mostrar Charts?",
            "Pregunta",
            JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION
    }
}

enum class ChartType { CIUDADES, FITNESS }

class MyChart(private val chartType: ChartType, private val params: Map<String, Any>) : JPanel() {
    private val margin = 65

    init {
        val id = params["id"] as? String ?: throw IllegalArgumentException("id no especificado.")
        val test = params["test"] as? String ?: throw IllegalArgumentException("test no especificado.")
        val algoritmo = params["algoritmo"] as? String ?: throw IllegalArgumentException("algoritmo no especificado.")
        preferredSize = sizeOfWindows
        background = Color.WHITE
        ToolTipManager.sharedInstance().registerComponent(this)
        JFrame("$numberOfWindows $chartType $id $test $algoritmo").apply {
            setLocation(numberOfWindows * offsetOfWindows, numberOfWindows * offsetOfWindows)
            numberOfWindows += 1
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            add(this@MyChart)
            pack()
            isVisible = true
        }
    }

    // Sobrescribir el método paintComponent para dibujar gráficos
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        when (chartType) {
            ChartType.CIUDADES -> drawCiudades(g)
            ChartType.FITNESS -> drawFitness(g)
        }
    }

    override fun getToolTipText(event: MouseEvent): String? {
        return when (chartType) {
            ChartType.CIUDADES -> getCiudadesToolTip(event)
            ChartType.FITNESS -> return ""
        }
    }

    private fun getCiudadesToolTip(event: MouseEvent): String? {
        @Suppress("UNCHECKED_CAST") val vector =
            params["vector"] as? Array<Point> ?: throw IllegalArgumentException("vector no especificado.")
        val drawableWidth = width - 2 * margin
        val drawableHeight = height - 2 * margin
        val minX = vector.minOfOrNull { it.x } ?: return "vector vacío"
        val maxX = vector.maxOfOrNull { it.x } ?: return "vector vacío"
        val minY = vector.minOfOrNull { it.y } ?: return "vector vacío"
        val maxY = vector.maxOfOrNull { it.y } ?: return "vector vacío"
        val scaleX = drawableWidth / (maxX - minX)
        val scaleY = drawableHeight / (maxY - minY)

        vector.forEach { point ->
            val scaledX = ((point.x - minX) * scaleX).toInt() + margin
            val scaledY = height - ((point.y - minY) * scaleY).toInt() - margin
            if (event.x in (scaledX - 5)..(scaledX + 5) && event.y in (scaledY - 5)..(scaledY + 5)) {
                return point.toString()
            }
        }
        return null
    }

    private fun drawCiudades(g: Graphics) {
        val mejor = params["mejor"] as? Tour ?: throw IllegalArgumentException("mejor no especificado.")
        @Suppress("UNCHECKED_CAST") val vector =
            params["vector"] as? Array<Point> ?: throw IllegalArgumentException("vector no especificado.")
        g.color = Color.BLACK
        // Definir márgenes
        val drawableWidth = width - 2 * margin
        val drawableHeight = height - 2 * margin
        // Guardar la fuente actual
        val originalFont = g.font
        // Cambiar la fuente y el tamaño de la letra
        val font = Font("Courier", Font.BOLD, 11)
        g.font = font
        // Dibujar el texto en la parte superior
        val clavesNoRequeridas = setOf("algoritmo", "id", "test", "tspTest", "vector")
        val resumen: String = params.filterKeys { it !in clavesNoRequeridas }.map { (clave, valor) ->
            val valorFormateado = if (valor is Double) String.format("%.2f", valor) else valor.toString()
            "$clave=[$valorFormateado]"
        }.joinToString(" ")
        g.drawString(resumen, 2, 15)
        // Restaurar la fuente original
        g.font = originalFont
        // Calcular los factores de escala
        val minX = vector.minOf { it.x }
        val maxX = vector.maxOf { it.x }
        val minY = vector.minOf { it.y }
        val maxY = vector.maxOf { it.y }
        val scaleX = drawableWidth / (maxX - minX)
        val scaleY = drawableHeight / (maxY - minY)
        // Dibujar los ejes X e Y
        g.drawLine(margin, height - margin, width - margin, height - margin) // Eje X
        g.drawLine(margin, height - margin, margin, margin) // Eje Y
        // Calcular intervalos de rotulación
        val xInterval = ((maxX - minX) / 10).toInt()
        val yInterval = ((maxY - minY) / 10).toInt()
        // Rotular el eje X
        for (i in 1 until 12) {
            val xLabel = (minX + i * xInterval).toInt()
            if (xLabel < maxX) {
                val scaledX = ((xLabel - minX) * scaleX).toInt() + margin
                g.drawLine(scaledX, height - margin - 5, scaledX, height - margin + 5)
                g.drawString(xLabel.toString(), scaledX - 10, height - margin + 20)
            }
        }
        // Rotular el eje Y
        for (i in 1 until 12) {
            val yLabel = (minY + i * yInterval).toInt()
            if (yLabel < maxY) {
                val scaledY = height - ((yLabel - minY) * scaleY).toInt() - margin
                g.drawLine(margin - 5, scaledY, margin + 5, scaledY)
                g.drawString(yLabel.toString(), margin - 30, scaledY + 5)
            }
        }
        // Dibujar los puntos y sus rótulos
        vector.forEachIndexed { index, point ->
            val scaledX = ((point.x - minX) * scaleX).toInt() + margin
            val scaledY = height - ((point.y - minY) * scaleY).toInt() - margin
            g.color = Color.RED
            g.fillOval(scaledX - 5, scaledY - 5, 10, 10)
            g.drawString((index + 1).toString(), scaledX + 5, scaledY - 5)
            g.color = Color.BLACK
        }
        // Dibujar las líneas de la ruta
        g.color = Color.RED
        val points = mejor.tour.map { vector[it] }
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val scaledX1 = ((p1.x - minX) * scaleX).toInt() + margin
            val scaledY1 = height - ((p1.y - minY) * scaleY).toInt() - margin
            val scaledX2 = ((p2.x - minX) * scaleX).toInt() + margin
            val scaledY2 = height - ((p2.y - minY) * scaleY).toInt() - margin
            g.drawLine(scaledX1, scaledY1, scaledX2, scaledY2)
        }
        // Dibujar la línea de vuelta al punto inicial
        val p1 = points.last()
        val p2 = points.first()
        val scaledX1 = ((p1.x - minX) * scaleX).toInt() + margin
        val scaledY1 = height - ((p1.y - minY) * scaleY).toInt() - margin
        val scaledX2 = ((p2.x - minX) * scaleX).toInt() + margin
        val scaledY2 = height - ((p2.y - minY) * scaleY).toInt() - margin
        g.drawLine(scaledX1, scaledY1, scaledX2, scaledY2)
    }

    private fun drawFitness(g: Graphics) {
        @Suppress("UNCHECKED_CAST") val avgFitness =
            params["avgFitness"] as? MutableList<Double>
                ?: throw IllegalArgumentException("avgFitness no especificado.")
        g.color = Color.BLUE
        val width = width - 2 * margin
        val height = height - 2 * margin
        val maxFitness = avgFitness.maxOrNull() ?: 1.0
        val minFitness = avgFitness.minOrNull() ?: 0.0
        val yScale = height.toDouble() / (maxFitness - minFitness)
        val xScale = width.toDouble() / avgFitness.size

        // Dibujar el eje Y con diez divisiones
        g.color = Color.BLACK
        val originalFont = g.font
        val smallFont = originalFont.deriveFont(12f) // Cambiar el tamaño de la fuente a 8
        g.font = smallFont
        for (i in 0..10) {
            val y = height + margin - (i * height / 10)
            g.drawLine(margin - 4, y, margin + 4, y)
            val label = String.format("%.7f", minFitness + i * (maxFitness - minFitness) / 10)
            g.drawString(label, 5, y + 5)
        }
        g.font = originalFont // Restaurar la fuente original

        // Dibujar la línea de fitness
        g.color = Color.BLUE
        for (i in 1 until avgFitness.size) {
            val x1 = margin + ((i - 1) * xScale).toInt()
            val y1 = height + margin - ((avgFitness[i - 1] - minFitness) * yScale).toInt()
            val x2 = margin + (i * xScale).toInt()
            val y2 = height + margin - ((avgFitness[i] - minFitness) * yScale).toInt()
            g.drawLine(x1, y1, x2, y2)
        }
    }
}

// Definición de la clase Point
data class Point(val x: Double, val y: Double) {
    // Calcular la distancia a otro punto
    fun distanciaA(otro: Point): Double {
        val dx = otro.x - x
        val dy = otro.y - y
        return sqrt(dx * dx + dy * dy)
    }

    // Representar el punto como una cadena
    override fun toString(): String {
        val xStr = if (x % 1.0 == 0.0) x.toInt().toString() else x.toString()
        val yStr = if (y % 1.0 == 0.0) y.toInt().toString() else y.toString()
        return "($xStr,$yStr)"
    }
}

class TspTest(archivoTest: String) {
    private val comentario: String
    val nombre: String
    val n: Int
    val optimo: Double
    val vector: Array<Point>
    val matriz: Array<Array<Double>>

    init {
        val lines = File(archivoTest).readLines()
        comentario = lines[0].trim()
        nombre = lines[1].trim()
        n = lines[2].trim().toInt()
        optimo = lines[3].trim().toDouble()
        println("nombre=$nombre n=$n optimo=$optimo comentario=$comentario ")
        // Inicializar el vector de puntos
        vector = Array(n) { i ->
            val (_, x, y) = lines[4 + i].split("\\s+".toRegex()).map { it.toDouble() }
            Point(x, y)
        }
        // Inicializar la matriz de distancias
        matriz = Array(n) { i ->
            Array(n) { j ->
                vector[i].distanciaA(vector[j])
            }
        }
    }

    // Función para calcular la distancia total de un tour
    fun calcularDistanciaTour(tour: List<Int>): Double {
        var distanciaTotal = 0.0
        for (i in 0 until tour.size - 1) {
            distanciaTotal += vector[tour[i]].distanciaA(vector[tour[i + 1]])
        }
        // Añadir la distancia de vuelta al punto inicial
        distanciaTotal += vector[tour.last()].distanciaA(vector[tour.first()])
        return distanciaTotal
    }
}

class Tour : Comparable<Tour> {
    var distancia: Double = 0.0
    var fitness: Double = 0.0
    private var tspTest: TspTest
    var tour: MutableList<Int>

    constructor(tspTest: TspTest, random: Boolean = false) {
        this.tspTest = tspTest
        tour = MutableList(tspTest.n) { it }
        if (random) tour.shuffle()
        actualizarDistanciaYFitness()
    }

    constructor(tspTest: TspTest, tour: MutableList<Int>) {
        this.tspTest = tspTest
        this.tour = tour
        actualizarDistanciaYFitness()
    }

    override fun toString(): String {
        val distanciaFormateada = "%.2f".format(distancia)
        val fitnessFormateado = "%.6f".format(fitness)
        val tourFormateado = tour.joinToString(separator = ",", prefix = "[", postfix = "]") { (it + 1).toString() }
        return "$distanciaFormateada $fitnessFormateado $tourFormateado"
    }

    override fun compareTo(other: Tour): Int = distancia.compareTo(other.distancia)

    fun normalizarTour() {
        if (tour.size > 2) {
            val i = tour.indexOf(0)
            val tourNormal = (tour.subList(i, tour.size) + tour.subList(0, i)).toMutableList()
            val tourInvertido =
                (listOf(tourNormal[0]) + tourNormal.subList(1, tourNormal.size).reversed()).toMutableList()
            tour = if (tourNormal[1] < tourInvertido[1]) tourNormal else tourInvertido
        }
    }

    fun updateTour(tour: MutableList<Int>) {
        this.tour = tour
        actualizarDistanciaYFitness()
    }

    fun updateTour(tour: MutableList<Int>, distancia: Double) {
        this.tour = tour
        this.distancia = distancia
        fitness = 1 / distancia
    }

    private fun actualizarDistanciaYFitness() {
        distancia = tspTest.calcularDistanciaTour(tour)
        fitness = 1 / distancia
    }
}

typealias Individuo = Tour

class TspToolBox {
    private val algoritmos = mapOf(
        "fuerzaBruta" to ::fuerzaBruta,
        "mejorVecino" to ::mejorVecino,
        "insertion1" to ::insertion1,
        "insertion2" to ::insertion2,
        "algGen" to ::algGen
    )
    private lateinit var mejor: Tour
    private lateinit var temporizador: Timer
    private lateinit var tspTest: TspTest
    private var salir: Boolean = false
    fun getAlgoritmos(): List<String> = algoritmos.keys.toList()

    fun ejecutarTest(params: Map<String, Any>): Map<String, Any> {
        tspTest = params["tspTest"] as? TspTest ?: throw IllegalArgumentException("tspTest no especificado.")
        val algoritmo = params["algoritmo"] as? String ?: throw IllegalArgumentException("algoritmo no especificado.")
        require(algoritmo in algoritmos) { "algoritmo[$algoritmo] no existe." }
        val newParams = params.toMutableMap().apply {
            this["test"] = tspTest.nombre
            this["optimo"] = tspTest.optimo
            this["vector"] = tspTest.vector
        }
        val timeout = params["timeout"] as? Long ?: TIMEOUT
        mejor = Tour(tspTest)
        salir = false
        temporizador = timer("temporizador", false, timeout * 1000L, timeout * 1000L) { salir = true }
        val tiempoIni = System.currentTimeMillis()
        algoritmos[algoritmo]?.invoke(newParams)
        val tiempo = (System.currentTimeMillis() - tiempoIni) / 1000.0
        val eficiencia = mejor.distancia / tspTest.optimo
        mejor.normalizarTour()
        return newParams.apply {
            this["eficiencia"] = eficiencia
            this["tiempo"] = tiempo
            this["mejor"] = mejor
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun fuerzaBruta(params: Map<String, Any>) {
        try {
            (0 until tspTest.n).toList().permutations().forEach { tour ->
                val d = tspTest.calcularDistanciaTour(tour)
                if (d < mejor.distancia) mejor.updateTour(tour.toMutableList(), d)
                if (salir) return
            }
        } catch (e: Exception) {
            println(e)
        } finally {
            temporizador.cancel()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun mejorVecino(params: Map<String, Any>) {
        try {
            for (inicio in 0 until tspTest.n) {
                val ruta = mutableListOf(inicio)
                val ciudadesRestantes = MutableList(tspTest.n) { it }.apply { remove(inicio) }
                while (ciudadesRestantes.isNotEmpty()) {
                    val ultimo = ruta.last()
                    val ciudad = ciudadesRestantes.minBy { tspTest.matriz[ultimo][it] }
                    ruta.add(ciudad)
                    ciudadesRestantes.remove(ciudad)
                    if (salir) return
                }
                val d = tspTest.calcularDistanciaTour(ruta)
                if (d < mejor.distancia) mejor.updateTour(ruta, d)
            }
        } catch (e: Exception) {
            println(e)
        } finally {
            temporizador.cancel()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun insertion1(params: Map<String, Any>) {
        try {
            val ruta = mutableListOf(0, 1)
            val ciudadesRestantes = MutableList(tspTest.n) { it }.apply { removeAll(ruta) }
            while (ciudadesRestantes.isNotEmpty()) {
                val ciudad = ciudadesRestantes.first()
                val mejorPos = (0..ruta.size).minBy { i ->
                    tspTest.calcularDistanciaTour(
                        ruta.subList(0, i) + ciudad + ruta.subList(
                            i,
                            ruta.size
                        )
                    ) - tspTest.calcularDistanciaTour(ruta)
                }
                ruta.add(mejorPos, ciudad)
                ciudadesRestantes.remove(ciudad)
                if (salir) return
            }
            mejor.updateTour(ruta)
        } catch (e: Exception) {
            println(e)
        } finally {
            temporizador.cancel()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun insertion2(params: Map<String, Any>) {
        try {
            val ruta = mutableListOf(0, 1)
            val ciudadesRestantes = MutableList(tspTest.n) { it }.apply { removeAll(ruta) }
            while (ciudadesRestantes.isNotEmpty()) {
                val ciudad = ciudadesRestantes.minBy { x ->
                    ruta.minOf { tspTest.calcularDistanciaTour(listOf(x, it)) }
                }
                val mejorPos = (0..ruta.size).minBy { i ->
                    tspTest.calcularDistanciaTour(
                        ruta.subList(0, i) + ciudad + ruta.subList(
                            i,
                            ruta.size
                        )
                    ) - tspTest.calcularDistanciaTour(ruta)
                }
                ruta.add(mejorPos, ciudad)
                ciudadesRestantes.remove(ciudad)
                if (salir) return
            }
            mejor.updateTour(ruta)
        } catch (e: Exception) {
            println(e)
        } finally {
            temporizador.cancel()
        }
    }

    private fun algGen(params: Map<String, Any>) {
        val popSize = params["popSize"] as? Int ?: 500
        val eliteSize = params["eliteSize"] as? Int ?: 5
        val generations = params["generations"] as? Int ?: 1000
        val mutationRate = params["mutationRate"] as? Double ?: 0.1

        fun initialPopulation(popSize: Int): MutableList<Individuo> {
            return MutableList(popSize) { Individuo(tspTest, true) }
        }

        fun rankRoutes(pop: MutableList<Individuo>): MutableList<Individuo> {
            return pop.sortedByDescending { it.fitness }.toMutableList()
        }

        /*
            La idea de esta función es generar es una lista de candidatos
            en la población que serán llevados a matrimonio.
            Se le recomienda utilizar un SORTEO POR RULETA, el cual es explicado en
            la presentación de PowerPoint.
            Yo sugiero generar una lista de candidatos del doble del tamaño
            de la población, de manera que cuando tome un padre, toma el primer elemento
            de la lista y lo remueve. Cuando seleccione la madre, hace lo mismo. A lo sumo,
            requiere hacer esto 2 * popSize veces
        */
        fun selection(popRanked: MutableList<Individuo>): MutableList<Int> {
            val Lista_De_Individuos = mutableListOf<Int>()
            val Lista_De_Fitness = mutableListOf<Double>()
            var total = 0.0
            for(i in popRanked){
                total += i.fitness
                Lista_De_Fitness.add(total) //contiene la suma acumulada del fitness para cada elemento de popRanked
            }           
            for(i in 0 until 2*popRanked.size){
                var num_random  = Random.nextDouble(Lista_De_Fitness.last())
                var valprox = Lista_De_Fitness.indexOfFirst{it > num_random}  //El indice del primer elemento en la lista que es mayor al numero random
                Lista_De_Individuos.add(valprox) // Es el mismo indice para popRanked
            }
            /*
            COLOQUE SU CÓDIGO AQUÍ, POR LOS MOMENTOS PONGO UN CÓDIGO
            QUE LO QUE HACE ES GENERAR CANDIDATOS ALEATORIOS
            */
            //return MutableList(2 * popRanked.size) { Random.nextInt(tspTest.n) }
            return Lista_De_Individuos
        }

        fun breed(padre: Individuo, madre: Individuo): Individuo {
            var pt_corte1 = Random.nextInt(padre.tour.size-1) //El primer pto de corte no puede estar en la posicion final
            var pt_corte2 = Random.nextInt(pt_corte1+1,padre.tour.size+1) //El segundo pto de corte tiene que estar delante del primero
            val hijo = padre    //Se copia todo lo del padre en el hijo
            val Lista_provisional = mutableListOf<Int>()
            //En lista provisional estan los elementos del padre entre los ptos de corte
            for(j in pt_corte1 until pt_corte2){
                Lista_provisional.add(padre.tour[j])
            }
            for(i in 0 until padre.tour.size){
                if(madre.tour[i] !in Lista_provisional){    //Se agregan los elementos de madre que no esten entre los ptos de corte en el padre
                    if( (i < pt_corte1) || (i >= pt_corte2)){
                        hijo.tour[i] = madre.tour[i]    //Se agrega en la posicion i de hijo el elemento i de madre               
                    }else{
                        if(i + Lista_provisional.size < hijo.tour.size){
                            //Se corre (Lista_provisional.size) posiciones el elemento i de la madre para agregarlo en hijo
                            hijo.tour[i+Lista_provisional.size] = madre.tour[i]
                        }
                    }
                }
            }
            /*
            COLOQUE SU CÓDIGO AQUÍ, POR LOS MOMENTOS PONGO UN CÓDIGO
            QUE LO QUE HACE ES SELECCIONAR ALEATORIAMENTE AL PADRE O LA MADRE
            */
            //return if (Random.nextDouble() < 0.5) padre else madre
            return hijo
        }

        fun mutate(individual: Individuo): Individuo {
            var pos1 = 0
            var pos2 = 0
            while(pos1 == pos2){    //Se buscan dos posiciones random hasta que sean distintas
                pos1 = Random.nextInt(individual.tour.size)
                pos2 = Random.nextInt(individual.tour.size)
            }
            //swap
            var temp = individual.tour[pos1]
            individual.tour[pos1] = individual.tour[pos2]
            individual.tour[pos2] = temp
            /*
            COLOQUE SU CÓDIGO AQUÍ
            */
            return individual
        }

        fun nextGeneration(currentGen: MutableList<Individuo>): MutableList<Individuo> {
            val popRanked = rankRoutes(currentGen)
            val selectionResults = selection(popRanked)
            val nextGen = popRanked.take(eliteSize).toMutableList()
            while (nextGen.size < popRanked.size) {
                val padre = popRanked[selectionResults.removeAt(0)]
                val madre = popRanked[selectionResults.removeAt(0)]
                var hijo = breed(padre, madre)
                if (Random.nextDouble() < mutationRate) hijo = mutate(hijo)
                nextGen.add(hijo)
            }
            return nextGen
        }

        try {
            val avgFitness = mutableListOf<Double>()
            var pop = initialPopulation(popSize)
            var generation = 0
            while (generation < generations && !salir) {
                pop = nextGeneration(pop)
                avgFitness.add(pop.sumOf { it.fitness } / pop.size)
                generation++
            }
            mejor = pop[0]
            val newParams = params.toMutableMap().apply {
                this["avgFitness"] = avgFitness
            }
            if (showCharts) MyChart(ChartType.FITNESS, newParams)
        } catch (e: Exception) {
            println(e)
        } finally {
            temporizador.cancel()
        }
    }
}

fun main() {
    val app = App()
    println(app.greeting)
    val tspDir: Path = Paths.get(TSP_DIR)
    if (!Files.exists(tspDir) || !Files.isDirectory(tspDir)) {
        app.ending("No existe el directorio $TSP_DIR")
        return
    }
    val testFiles = app.selectFiles(TSP_DIR)
    if (testFiles.isEmpty()) {
        app.ending("No se seleccionaron archivos TSP.")
        return
    }
    app.askCharts()
    val tspTests = testFiles.map { TspTest(it) }
    val ttb = TspToolBox()
    val algoritmos = ttb.getAlgoritmos().filterNot { it == "algGen" || it == "fuerzaBruta" }
    var i = 0
    val experimentos = mutableListOf<Map<String, Any>>()

    tspTests.forEach { tspTest ->
        algoritmos.forEach { algoritmo ->
            experimentos.add(
                mutableMapOf(
                    "id" to "exp${++i}",
                    "algoritmo" to algoritmo,
                    "tspTest" to tspTest
                )
            )
        }
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=1 mr=0.01",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 1,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.01
            )
        )
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=5 mr=0.01",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 1,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.01
            )
        )
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=1 mr=0.05",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 1,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.05
            )
        )
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=5 mr=0.05",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 5,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.05
            )
        )
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=1 mr=0.1",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 1,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.1
            )
        )
        experimentos.add(
            mutableMapOf(
                "id" to "exp${++i} algGen es=5 mr=0.1",
                "algoritmo" to "algGen",
                "tspTest" to tspTest,
                "popSize" to tspTest.n * 10,
                "eliteSize" to 5,
                "generations" to tspTest.n * 1000,
                "mutationRate" to 0.1
            )
        )
    }

    val resultados = experimentos.map { experimento -> ttb.ejecutarTest(experimento) }
    val reporte1 = dataFrameOf(
        "id" to resultados.map { it["id"] },
        "test" to resultados.map { it["test"] },
        "algoritmo" to resultados.map { it["algoritmo"] },
        "optimo" to resultados.map { it["optimo"] },
        "eficiencia" to resultados.map { it["eficiencia"] },
        "tiempo" to resultados.map { it["tiempo"] },
        "mejor" to resultados.map { it["mejor"] }
    )
    val reporte2 = reporte1.groupBy("algoritmo").aggregate {
        mean("eficiencia") into "eficiencia_AVG"
        std("eficiencia") into "eficiencia_STD"
        mean("tiempo") into "tiempo_AVG"
        std("tiempo") into "tiempo_STD"
    }
    reporte1.print(rowsLimit = Int.MAX_VALUE, borders = true)
    reporte2.print(rowsLimit = Int.MAX_VALUE, borders = true)
    if (showCharts) resultados.forEach { corrida -> MyChart(ChartType.CIUDADES, corrida) }
    app.ending("programa terminado")
}
