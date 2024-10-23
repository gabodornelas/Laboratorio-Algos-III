import ve.usb.libGrafo.*
import Jama.Matrix
import java.io.File
import kotlin.reflect.full.primaryConstructor

fun getGrafo(rutaArchivo: String): Grafo? { // para leer el grafo de un archivo. De acuerdo al nombre en el archivo se crea el tipo de grafo
    val clases = arrayOf("GrafoNoDirigidoCosto","GrafoNoDirigido","GrafoDirigidoCosto","GrafoDirigido")
    for (tipoGrafo in clases) {
        if (rutaArchivo.contains(tipoGrafo)) {
            return try {
                val clase = Class.forName("ve.usb.libGrafo.${tipoGrafo}").kotlin
                val constructor = clase.constructors.find{it.parameters.size == 1 && it.parameters[0].type.classifier == String::class}
                val instancia = constructor?.call(rutaArchivo) as Grafo
                // feedback
                val V = instancia.obtenerNumeroDeVertices()
                val E = instancia.obtenerNumeroDeLados()
                for(i in 1..V) println("${File(rutaArchivo).name} $tipoGrafo V=$V E=$E $i:${instancia.adyacentes(i).map{it.elOtroVertice(i)}}")
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
    for(i in 1..n){
        for( k in g.adyacentes(i) ){
            for(j in 1..n){
                if( j == k.elOtroVertice(i) ){
                    A[i-1,j-1] = 1.0;
                }
            }
        }
    }
    return A
}

fun getMatrizDeAlcance(A: Matrix): Matrix {
    val R = A.copy()
    val n = R.rowDimension
    /*
    SU CODIGO
    */
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
    val visited = BooleanArray(n) // El constructor por defecto inicializa en False
    val components = mutableListOf<List<Int>>()
    /*
    SU CODIGO
    */
    return components
}

fun main(args: Array<String>) {
    File(".").listFiles{_,name->name.endsWith(".txt")}?.sortedBy{it.name}?.forEach{file->
        getGrafo(file.absolutePath)?.let{grafo->
            print("Componentes conexas: ${getComponentesConexas(grafo)}\n\n${"*".repeat(120)}\n")
        }?: print("No se pudo crear la instancia del grafo para el archivo ${file.name}.")
    }
 }