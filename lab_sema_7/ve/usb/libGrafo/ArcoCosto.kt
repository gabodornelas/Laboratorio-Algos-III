package ve.usb.libGrafo
class ArcoCosto(override val a: Int,override val b: Int, val costo: Double) : Arco(a,b), Comparable<ArcoCosto> {
	override fun toString() = "($a,$b,$costo)" // Representación en string del arco
	override fun compareTo(other: ArcoCosto): Int { return costo.compareTo(other.costo) }
	fun costo() = costo // Retorna el peso o costo asociado del arco
}