package ve.usb.libGrafo

abstract class Lado(val a: Int, val b: Int) {

    // Retorna cualquiera de los dos vértices del grafo
    fun cualquieraDeLosVertices() : Int {
	return a
    }

    // Dado un vertice w, si w == a entonces retorna b, de lo contrario si w == b  entonces retorna a,  y si w no es igual a a ni a b, entonces se lanza una RuntimeExpception 
    fun elOtroVertice(w: Int) : Int {
	if (w == a) {
	    return b
	} else if (w == b) {
	    return a
	} else {
	    throw RuntimeException()
	}
    }
}
