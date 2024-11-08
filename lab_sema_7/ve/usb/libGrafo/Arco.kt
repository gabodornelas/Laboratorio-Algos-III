package ve.usb.libGrafo
open class Arco(override val a: Int, override val b: Int) : Lado(a,b) {
	override fun toString() = "($a,$b)" // Representación del arco
	fun fuente() : Int = a // Retorna el vértice inicial del arco
	fun sumidero() : Int = b // Retorna el vértice final del arco
}