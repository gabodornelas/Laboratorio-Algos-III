// Mauricio Fragachan 20-10265
// Gabriel De Ornelas 15-10377

import ve.usb.libGrafo.*
import Jama.Matrix
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import java.util.ArrayDeque
import java.util.Stack
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess

val clases = arrayOf("GrafoNoDirigidoCosto","GrafoNoDirigido","GrafoDirigidoCosto","GrafoDirigido")

fun seleccionaArchivoTxt(prompt:String): String?{
    val directory = File(".")
    val txtFiles = directory.listFiles{ _, name -> clases.any { name.contains(it) } && name.endsWith(".txt")}?.sortedBy{it.name}
    if (txtFiles != null && txtFiles.isNotEmpty()) {
        val fileChooser = JFileChooser(directory)
        fileChooser.dialogTitle = "Selecciona un archivo TXT"
        fileChooser.isMultiSelectionEnabled = false
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        val options = txtFiles.map { it.name }.toTypedArray()
        val selectedFile = JOptionPane.showInputDialog(
            null,
            prompt,
            "Selector de archivos *.txt",
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        )
        return selectedFile  as String?
    }
    return null
}

fun getVecinos(g: Grafo, u: Int): List<Int> { // obtener la lista de vecinos a un nodo, ordenados por el # del nodo
    return g.adyacentes(u).map{it.elOtroVertice(u)}.sorted()
}

fun getGrafo(rutaArchivo: String): Grafo? { // para leer el grafo de un archivo. De acuerdo al nombre en el archivo se crea el tipo de grafo
    for (tipoGrafo in clases) {
        if (rutaArchivo.contains(tipoGrafo)) {
            return try {
                val clase = Class.forName("ve.usb.libGrafo.${tipoGrafo}").kotlin
                val constructor = clase.constructors.find{it.parameters.size == 1 && it.parameters[0].type.classifier == String::class}
                val instancia = constructor?.call(rutaArchivo) as Grafo
                // feedback
                val V = instancia.obtenerNumeroDeVertices()
                val E = instancia.obtenerNumeroDeLados()
                for(i in 1..V)  println("${File(rutaArchivo).name} $tipoGrafo V=$V E=$E $i:${getVecinos(instancia,i)}")
                instancia
            } catch (e: Exception) {
                println("Error al instanciar la clase: ${e.message}")
                null
            }
        }
    }
    return null
}

fun getMatrizDeAdyacencia(g: Grafo): Matrix {
    var n = g.obtenerNumeroDeVertices()
    var A = Matrix(n,n) // el constructor por defecto inicializa en ceros
    val esNoDirigido = g is GrafoNoDirigido || g is GrafoNoDirigidoCosto
    for(i in 1..n){
        for(j in getVecinos(g,i)){
            A.set(i-1,j-1,1.0)
            if(esNoDirigido) A.set(j-1,i-1,1.0)
        }
    }
    return A
}

fun getMatrizDeAlcance(A: Matrix): Matrix {
    val R = A.copy()
    val n = R.rowDimension
    for (k in 0 until n) {
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (R.get(i, j) == 0.0 && R.get(i, k) == 1.0 && R.get(k, j) == 1.0) {
                    R.set(i, j, 1.0)
                }
            }
        }
    }
    return R
}

fun getComponentesConexas(g: Grafo): List<List<Int>> { //Construir una lista de lista de los rótulos (Int) de los nodos con las componentes conexas
    val n = g.obtenerNumeroDeVertices()
    val A = getMatrizDeAdyacencia(g) // construir la matriz de adyacencia (A)
    val R = getMatrizDeAlcance(A) // construir la matriz de alcance (R)
    print("\nMatriz de adyacencia (A) $n x $n"); A.print(0,0); print("\nMatriz de alcance (R) $n x $n"); R.print(0,0) // feedback
    // Utilizar la matriz C (copia de R) para extraer las componentes conexas
    val C = R.copy()
    val esNoDirigido = g is GrafoNoDirigido || g is GrafoNoDirigidoCosto
    val visitado = BooleanArray(n) // El constructor por defecto inicializa en False
    val componentes = mutableListOf<List<Int>>()
    for (i in 0 until n) {
        C.set(i,i,1.0)
        if (!visitado[i]) {
            var componente = mutableListOf<Int>()
            for (j in 0 until n) {
                val criterio = if(esNoDirigido) (C.get(i,j) == 1.0 && !visitado[j]) else (C.get(i,j) == 1.0 && C.get(j,i) == 1.0)
                if (criterio) {
                    componente.add(j+1) // OJO, la matrices comienzan en cero mientras los grafos en uno
                    visitado[j] = true
                }
            }
            componentes.add(componente)
        }
    }
    return componentes
}

fun BFS(g: Grafo, inicio: Int): List<Int> { // presenta el recorrido BFS del grafo g desde el nodo inicio
    val visitado = mutableListOf<Int>()
    val cola = ArrayDeque<Int>()
    cola.add(inicio)
    visitado.add(inicio)
    while (cola.isNotEmpty()) {
        val nodo = cola.removeFirst()
        getVecinos(g,nodo)?.forEach { vecino ->
            if (vecino !in visitado) {
                visitado.add(vecino)
                cola.add(vecino)
            }
        }
    }
    return visitado
}

fun DFS(g: Grafo, inicio: Int): List<Int>  { // presenta el recorrido DFS del grafo g desde el nodo inicio
    val visitado = mutableListOf<Int>()
    val P = Stack<Int>()
    P.push(inicio)  //Se apila el vertice de inicio
    while(P.isNotEmpty()){
        val NodoActual = P.pop()    //El ultimo apilado sera el nodo actual
        if(NodoActual !in visitado){
            visitado.add(NodoActual)
            getVecinos(g,NodoActual)?.forEach{ w ->     //Cada vecino del NodoActual sera apilado (si no ha sido visitado)
                if(w !in visitado){                     //y representaran las posibles ramas a recorrer
                    P.push(w)
                }
            }
        }
    }
    return visitado
}

fun main(args: Array<String>) {
    val sep = "*".repeat(120)
    val txt = seleccionaArchivoTxt("Seleccionar archivo de grafo")
    if(txt == null) exitProcess(1)
    getGrafo(txt)?.let{g->
        val n = g.obtenerNumeroDeVertices()
        println(sep)
        println("Componentes conexas: ${getComponentesConexas(g)}")
        for(i in 1..n) println("BFS($i)=${BFS(g,i)}")
        println(sep)
        for(i in 1..n) println("DFS($i)=${DFS(g,i)}")
        println(sep)
    }?: println("No se pudo crear la instancia del grafo para el archivo $txt")
 }
