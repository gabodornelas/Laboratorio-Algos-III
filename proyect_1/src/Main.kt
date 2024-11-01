import ve.usb.libGrafo.*
import Jama.Matrix
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import java.util.ArrayDeque
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess

fun getVecinos(g: Grafo, u: Int): List<Int> { // obtener la lista de vecinos a un nodo, ordenados por el # del nodo
    return g.adyacentes(u).map{it.elOtroVertice(u)}.sorted()
}

fun getGrafo(rutaArchivo: String): Grafo? { // para leer el grafo de un archivo. De acuerdo al nombre en el archivo se crea el tipo de grafo
    
    return try {
        val clase = Class.forName("ve.usb.libGrafo.GrafoNoDirigido").kotlin
        val constructor = clase.constructors.find{it.parameters.size == 1 && it.parameters[0].type.classifier == String::class}
        val instancia = constructor?.call(rutaArchivo) as Grafo
        // feedback
        val V = instancia.obtenerNumeroDeVertices()
        val E = instancia.obtenerNumeroDeLados()
        for(i in 1..V)  println("${File(rutaArchivo).name} GrafoNoDirigido V=$V E=$E $i:${getVecinos(instancia,i)}")
        instancia
    } catch (e: Exception) {
        println("Error al instanciar la clase: ${e.message}")
        null
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


fun main(args: Array<String>) {
    fun seleccionaArchivoTxt(prompt:String): String?{
        val directory = File(".")
        val txtFiles = directory.listFiles{ _, name -> name.endsWith(".txt")}?.sortedBy{it.name}
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
    val amigos_txt = seleccionaArchivoTxt("Seleccionar archivo de amigos.txt")
    if(amigos_txt == null) exitProcess(1)
    val candidatos_txt = seleccionaArchivoTxt("Seleccionar archivo de candidatos.txt")
    if(candidatos_txt == null) exitProcess(1)

    var amigos_grafo = getGrafo(amigos_txt) as Grafo
    val V = amigos_grafo.obtenerNumeroDeVertices()
    var amigos_per_vertice = mutableListOf<Int>()
    var amigos_componentes_conexas = getComponentesConexas(amigos_grafo)
    amigos_componentes_conexas = amigos_componentes_conexas.sortedByDescending { it.size }

    for(i in 1..V)  {
        //Add an element to the end of the list
        amigos_per_vertice.add(getVecinos(amigos_grafo,i).size)
    }
    //Find maximum number inside of an array
    var max = amigos_per_vertice.maxOrNull()
    var min = amigos_per_vertice.minOrNull()

    var max_quantity = amigos_per_vertice.count{it == max}
    var min_quantity = amigos_per_vertice.count{it == min}

    
    var candidatos_grafo = getGrafo(candidatos_txt)



    println("amigos=$amigos_txt\ncandidatos=$candidatos_txt")
    /*
    *********************************************************************************
    *********************************************************************************
    A PARTIR AQUI EL CODIGO QUE SOPORTE LA GENERACION DEL REPORTE PARA I♥CATS
    *********************************************************************************
    *********************************************************************************
    */
    println("INFORME I♥CATS")
    println("       USUARIOS CON MAS AMIGOS=$max_quantity")
    var contador = 0    
    for(i in 0..(V-1))  {
        if(amigos_per_vertice[i] == max){
            contador++
            println("       $contador:${i+1}:${amigos_per_vertice[i]}: ${getVecinos(amigos_grafo,i+1)}")
        }
    }
    println("       USUARIOS CON MENOS AMIGOS=$min_quantity")
    contador = 0
    for(i in 0..(V-1))  {
        if(amigos_per_vertice[i] == min){
            contador++
            println("       $contador:${i+1}:${amigos_per_vertice[i]}: ${getVecinos(amigos_grafo,i+1)}")
        }
    }
    println("       COMUNIDADES DE AMIGOS=${amigos_componentes_conexas.size}")
    
    for (i in 0 until amigos_componentes_conexas.size) {
        var comunidades = mutableListOf<List<Int>>()
        for (j in 0 until amigos_componentes_conexas[i].size) {
            comunidades.add(getVecinos(amigos_grafo,amigos_componentes_conexas[i][j]))
        }

        var comunidades_ordenadas = comunidades.sortedByDescending { it.size }

        println("               COMUNIDAD ${i+1}")
        contador = 0

        for (j in 0 until amigos_componentes_conexas[i].size) {
            contador++
            println("                   $contador:${amigos_componentes_conexas[i][j]}:${getVecinos(amigos_grafo,amigos_componentes_conexas[i][j]).size}: ${comunidades_ordenadas[j]}")
        }
    }

 }