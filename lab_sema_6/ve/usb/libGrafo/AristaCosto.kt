package ve.usb.libGrafo

public class AristaCosto(val x: Int,
			 val y: Int,
			 val costo: Double) : Comparable<AristaCosto>, Arista(x, y) {

    // Retorna el costo del arco
    fun costo() : Double {
	return costo
    }

    // Representación en string de la arista
    override fun toString() : String {
	return """
	v: ${x}
	w: ${y}
	costo: ${costo}
	"""
    }

    /* 
     Se compara dos arista con respecto a su costo. 
     Si this.obtenerCosto > other.obtenerCosto entonces
     retorna 1. Si this.obtenerCosto < other.obtenerCosto 
     entonces retorna -1. Si this.obtenerCosto == other.obtenerCosto
     entonces retorna 0 
     */
     override fun compareTo(other: AristaCosto): Int {
	 return when {
	     costo > other.costo -> 1
	     costo == other.costo -> 0
	     else -> -1
	 }
     }
} 
