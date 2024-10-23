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
        // k itera por la lista de lados donde i es el vertice de inicio
        for( k in g.adyacentes(i) ){
            for(j in 1..n){
                // j itera sobre los vertices hasta encontrar elOtroVertice del lado k que
                //contiene a i (grafos no dirigidos) o que inicia en i (grafos dirigidos)
                if( j == k.elOtroVertice(i) ){
                    //Al conseguirlo se asigna 1.0 en la casilla que representa al lado i,j
                    A[i-1,j-1] = 1.0
                }
            }
        }
    }

    return A
}

fun getMatrizDeAlcance(A: Matrix): Matrix {
    val R = A.copy()
    val n = R.rowDimension
    
    
    for (k in 0 until n) {
        R[k,k] = 1.0    // Se agregan 1.0s en la diagonal principal (la matriz identidad) 
        for (i in 0 until n) {
            for (j in 0 until n) {
                // Si no hay camino directo de i a j, pero hay un camino de i a k y de k a j, entonces hay un camino de i a j y
                // por lo tanto se debe marcar con 1.0 la posición (i,j) de la matriz de alcance
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
    val visited = BooleanArray(n) // El constructor por defecto inicializa en False
    val components = mutableListOf<List<Int>>()
    
    //Recorrer cada posible camino que conecta con el vertice i de la matriz de alcance
    for (i in 0 until n) {
        //Si el vertice no ha sido visitado, procedemos a buscar los vertices que estan conectados con el
        if (!visited[i]) {
            val component = mutableListOf<Int>()

            //Agregamos el vertice a la lista de componentes, ya que en el peor caso que no este conectado con ningun otro vertice,
            //entonces el es una componente conexa por si solo
            component.add(i)

            //Recorrer los vertices que estan conectados con el vertice i
            for (j in 0 until n) {

                //Booleano para verificar si este vertice es parte de la componente conexa, es decir, si
                //este vertice j esta conectado con todos los vertices de la componente
                var parteDeComponente = true
                //Primero, se verifica que i y j esten conectados
                if (i != j && C.get(i, j) == 1.0 && C.get(j, i) == 1.0) {

                    //Luego, se verifica que j este conectado con todos los vertices de la componente
                    for(k in 0 until component.size){
                        if(C.get(component[k],j) == 0.0 && C.get(j,component[k]) == 0.0){
                            //Si no esta conectado con algun vertice de la componente, entonces no es parte de la componente, por lo
                            //tanto se cambia el valor del booleano a false y se rompe el ciclo
                            parteDeComponente = false
                            break
                        }
                    }

                    //Se verifica que el valor del booleano sea true para proceder a agregarlo a la componente conexa y
                    //a marcarlo como visitado
                    if(parteDeComponente){
                        component.add(j)
                        visited[j] = true
                        C.set(j, i, 0.0)
                        C.set(i, j, 0.0)  
                    }

                }
            }
            //Si la componente no esta vacia, entonces se agrega a la lista de componentes. Y ademas, antes de agregar la componente
            //a dicha lista, se le debe sumar 1 a todos los vertices pertenecientes a ella, ya que los vertices dados en el grafo se empiezan
            //a contar desde 1 y hasta ahora hemos trabajado es con los indices de las listas y por lo tanto, hemos estado contando desde 0 y no 
            //desde 1 
            if(component.isNotEmpty()){

                for (k in 0 until component.size) {
                    component[k] += 1
                }
                components.add(component)
            }
        }
    }
    
    return components
}

fun main(args: Array<String>) {
    File(".").listFiles{_,name->name.endsWith(".txt")}?.sortedBy{it.name}?.forEach{file->
        getGrafo(file.absolutePath)?.let{grafo->
            print("Componentes conexas: ${getComponentesConexas(grafo)}\n\n${"*".repeat(120)}\n")
        }?: print("No se pudo crear la instancia del grafo para el archivo ${file.name}.")
    }
 }
