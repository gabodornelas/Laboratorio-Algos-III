package ve.usb.libGrafo

import java.io.File

public class GrafoNoDirigido: Grafo {
    var numeroDeLados = 0
    var numDeVertices = 0
    var adj = ArrayList<ArrayList<Arista>>();
    var ari = Arista(1,2)

    // Se construye un grafo a partir del número de vértices
    constructor(numDeVertices: Int) {
	this.numDeVertices = numDeVertices;
   	for (i in 0..numDeVertices) {
	    adj.add(ArrayList<Arista>());
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
		    adj.add(ArrayList<Arista>());
		}
	    } else if (cont == 1) {
		println("Numero de lados ${it}")
		E = it.toInt()
		cont++
	    } else {
		if (E > (cont-2)) {
		    var tok = it.split(" ")
		    //println("Lado sin peso: ${tok.get(0)} ${tok.get(1)}")
		    agregarArista(Arista(tok.get(0).toInt(), tok.get(1).toInt()))
		    cont++
		}
	    }
	}
    }

    // Agrega un lado al grafo no dirigido
    fun agregarArista(a: Arista) {
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
    override fun adyacentes(v: Int) : Iterable<Arista> {
	return adj.get(v);
    }

    
/*
    fun aristas() : Iterable<Arista> {
	var lados = ArrayList<Arista>()
	for (i in 0..numDeVertices) {
	    for (e in adj.get(i) )
	    if (e.u > e.v) {
		lados.add(e)
	    }
	}
	return lados
    }
*/
/*
     inner class ListaIterato(l: ListaEnlazadaSimple<T>) : Iterator<T> {
	var actual = l.cabeza
	override fun hasNext(): Boolean = (actual != null) 
	override fun next(): T {
	    if (actual == null) {
		throw NoSuchElementException("Error, no hay mas elementos que iterar")
	    }
	    val valor = actual!!.valor
	    actual = actual?.proximo
	    return valor
	}
    }
 */
    // Retorna todos los lados del grafo no dirigido
     override operator fun iterator() : Iterator<Arista> {
	 var lados = ArrayList<Arista>()
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
