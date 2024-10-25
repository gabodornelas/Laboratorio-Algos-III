package ve.usb.libGrafo

public open class Arista(val v: Int, val u: Int) : Lado(v, u) {

    // Representación en string de la arista
    override fun toString() : String {
	return """
	v: ${v}
	w: ${u}
	"""
    }

} 
