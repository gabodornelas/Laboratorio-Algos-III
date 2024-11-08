package ve.usb.libGrafo
open class Arista(override val a: Int, override val b: Int) : Lado(a,b) {
	override fun toString() = "($a,$b)" // Representación en string de la arista
	fun invertir() = Arista(b, a)
}