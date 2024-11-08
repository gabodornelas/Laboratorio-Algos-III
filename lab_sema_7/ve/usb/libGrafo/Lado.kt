package ve.usb.libGrafo
abstract class Lado(open val a: Int, open val b: Int) {
	fun cualquieraDeLosVertices() = a // Retorna cualquiera de los dos vértices del grafo
	fun elOtroVertice(w: Int) = if (w == a) b else if (w == b) a else throw RuntimeException() // Dado un vertice w, si w == a entonces b, si w == b entonces a,  y si w no es igual a a ni a b, entonces lanza una RuntimeExpception
}
