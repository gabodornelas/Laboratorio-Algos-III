package ve.usb.libGrafo

import java.io.File

public class GrafoNoDirigidoCosto: Grafo {
    var numeroDeLados = 0
    var numDeVertices = 0
    var adj = ArrayList<ArrayList<AristaCosto>>();
    //var ari = AristaCosto(1,2, 0.0)

    // Se construye un grafo a partir del número de vértices
    constructor(numDeVertices: Int) {
	this.numDeVertices = numDeVertices;
   	for (i in 0..numDeVertices) {
	    adj.add(ArrayList<AristaCosto>());
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
    constructor(nombreArchivo: String) {
	var cont = 0
	var E = 0
	File(nombreArchivo).forEachLine {
	    if (cont == 0) {
		numDeVertices = it.toInt() 
		println("Numero de vertices ${numDeVertices}")
		cont++
		for (i in 0..numDeVertices) {
		    adj.add(ArrayList<AristaCosto>());
		}
	    } else if (cont == 1) {
		println("Numero de lados ${it}")
		E = it.toInt()
		cont++
	    } else {
		if (E > (cont-2)) {
		    var tok = it.split(" ")
		    //println("Lado con peso: ${tok.get(0)} ${tok.get(1)} ${tok.get(2)}")
		    agregarAristaCosto( AristaCosto(tok.get(0).toInt(), tok.get(1).toInt(), tok.get(2).toDouble()) )
		    cont++
		}
	    }
	}
    }

    // Agrega un lado al grafo no dirigido
    fun agregarAristaCosto(a: AristaCosto) {
	(adj.get(a.v)).add(a);
	(adj.get(a.u)).add(a);
	numeroDeLados++;
    }

    // Retorna el número de lados del grafo
    override fun obtenerNumeroDeLados() : Int {
	return numeroDeLados;
    }

    // Retorna el número de vértices del grafo
    override fun obtenerNumeroDeVertices() : Int {
	return numDeVertices;
    }

    // Retorna los lados adyacentes al vértice v, es decir, los lados que contienen al vértice v
    override fun adyacentes(v: Int) : Iterable<AristaCosto> {
	return adj.get(v);
    }

    // Retorna todos los lados del grafo no dirigido
     override operator fun iterator() : Iterator<AristaCosto> {
	 var lados = ArrayList<AristaCosto>()
	 for (i in 0..numDeVertices) {
	     for (e in adj.get(i) )
	     if (e.u > e.v) {
		 lados.add(e)
	     }
	 }
	 return lados.iterator()
     }
    
    // Grado del grafo
    override fun grado(v: Int) : Int {
	return 1;
    }

    // Retorna un string con una representación del grafo, en donde se nuestra todo su contenido
    override fun toString() : String {
	return """
	V: ${numDeVertices}
	E: ${numeroDeLados}
	"""
     }
}
