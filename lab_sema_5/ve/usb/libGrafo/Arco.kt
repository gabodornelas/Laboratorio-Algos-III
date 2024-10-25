package ve.usb.libGrafo

public open class Arco(val inicio: Int, val fin: Int) : Lado(inicio, fin) {

    // Retorna el vértice inicial del arco
    fun fuente() : Int {
	return inicio
    }

    // Retorna el vértice final del arco
    fun sumidero() : Int {
	return fin
    }

    // Representación del arco
    override fun toString() : String {
	return """
	inicio: ${inicio}
	fin: ${fin}
	"""
     }
} 
