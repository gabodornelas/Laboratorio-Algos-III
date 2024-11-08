package ve.usb.libGrafo
class AristaCosto(override val a: Int, override val b: Int, val costo: Double) :  Arista(a, b), Comparable<AristaCosto>{
	//override fun toString()  = "($a,$b,$costo)" // Representación en string de la arista
	override fun toString() = "($a,$b,${if (costo % 1 == 0.0) costo.toInt() else costo})" // Representación en string de la arista
	override fun compareTo(other: AristaCosto): Int { return costo.compareTo(other.costo) }
	fun costo() = costo // Retorna el costo del arco
}