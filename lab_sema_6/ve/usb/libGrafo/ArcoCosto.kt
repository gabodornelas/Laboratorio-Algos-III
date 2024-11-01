package ve.usb.libGrafo

public class ArcoCosto(val x: Int,
    val y: Int,
    val costo: Double) : Arco(x, y) {

    // Retorna el peso o costo asociado del arco
    fun costo() : Double {
	return costo
    }

    // Representación del arco
    override fun toString() : String {
	return """
	inicio: ${x}
	fin: ${y}
	peso: ${costo}
	"""
     }
} 
