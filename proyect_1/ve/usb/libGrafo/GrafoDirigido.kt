package ve.usb.libGrafo

import java.io.File

public class GrafoDirigido : Grafo {
    var numeroDeLados = 0
    var numDeVertices = 0
    var adj = ArrayList<ArrayList<Arco>>();

    // Se construye un grafo a partir del número de vértices
    constructor(numDeVertices: Int) {
	this.numDeVertices = numDeVertices;
   	for (i in 0..numDeVertices) {
	    adj.add(ArrayList<Arco>());
	}
    }

    /*
     Se construye un grafo a partir de un archivo. Existen dos tipos de formatos de archivo.
     El primero solo incluye los vétices de los lados, sin los pesos. El formato es como sigue.
     La primera línea es el número de vértices. La segunda línea es el número de lados. Las siguientes líneas
     corresponden a los lados, con los vértices de un lado separados por un espacio en blanco.
     El segundo formato solo se diferencia del primero en que cada línea de los lados tiene a dos enteros
     que corresponden a los vértices de los lados y un número real que es el peso o costo del lado.
     La variable conPeso es true si el archivo de entrada contiene un formato que incluye los pesos de los
     lados. Es false si el formato solo incluye los vértices de los lados. Se asume que los datos del 
     archivo están correctos, no se verificas.
     */  
    constructor(nombreArchivo: String)  {
	var cont = 0
	var E = 0
	File(nombreArchivo).forEachLine {
	    if (cont == 0) {
		numDeVertices = it.toInt() 
		println("Numero de vertices ${numDeVertices}")
		cont++
		for (i in 0..numDeVertices) {
		    adj.add(ArrayList<Arco>());
		}
	    } else if (cont == 1) {
		println("Numero de lados ${it}")
		E = it.toInt()
		cont++
	    } else {
		if (E > (cont-2)) {
		    var tok = it.split(" ")
		    //println("Lado sin peso: ${tok.get(0)} ${tok.get(1)}")
		    agregarArco(Arco(tok.get(0).toInt(), tok.get(1).toInt()))
		    cont++
		}
	    }
	}
    }

    // Agrega un lado al digrafo
    fun agregarArco(a: Arco) : Boolean {
	(adj.get(a.fuente())).add(a);
	numeroDeLados++;
	return true
    }

    // Retorna el grado del grafo. Si el vértice no pertenece al grafo se lanza una RuntimeException
    override fun grado(v: Int) : Int {
	return 1;
    }

    // Retorna el grado exterior del grafo. Si el vértice no pertenece al grafo se lanza una RuntimeException
    fun gradoExterior(v: Int) : Int {
	return v
    }

    // Retorna el grado interior del grafo. Si el vértice no pertenece al grafo se lanza una RuntimeException
    fun gradoInterior(v: Int) : Int {
	return v
    }

    // Retorna el número de lados del grafo
    override fun obtenerNumeroDeLados() : Int {
	return numeroDeLados;
    }

    // Retorna el número de vértices del grafo
    override fun obtenerNumeroDeVertices() : Int {
	return numDeVertices;
    }

    /* 
     Retorna los adyacentes de v, en este caso los lados que tienen como vértice inicial a v. 
     Si el vértice no pertenece al grafo se lanza una RuntimeException
     */
    override fun adyacentes(v: Int) : Iterable<Arco> {
	return adj.get(v);
    }

    // Retorna todos los lados del digrafo
    override operator fun iterator() : Iterator<Arco> {
	var lados = ArrayList<Arco>()
	for (i in 0..numDeVertices) {
	    for (e in adj.get(i) ) {
	   	lados.add(e)
	    }
	}
	return lados.iterator()
     }
     
    // String que muestra el contenido del grafo
    override fun toString() : String {
	return """
	V: ${numDeVertices}
	E: ${numeroDeLados}
	"""
     }
}
