package ve.usb.libGrafo
import java.io.File
class GrafoDirigidoCosto:Grafo{
	var numeroDeLados = 0
	var numDeVertices = 0
	var adj = ArrayList<ArrayList<ArcoCosto>>();
	override fun obtenerNumeroDeLados() = numeroDeLados
	override fun obtenerNumeroDeVertices() = numDeVertices
	override fun adyacentes(v: Int) : Iterable<ArcoCosto> = adj.get(v) // Retorna los lados adyacentes al vértice v
	override fun toString() = "V=$numDeVertices E=$numeroDeLados " + joinToString(", ") // Retorna un string con una representación del grafo
	fun agregarArcoCosto(a: ArcoCosto) {(adj.get(a.fuente())).add(a);numeroDeLados++;} // Agrega un lado al digrafo
	constructor(numDeVertices: Int) { // Se construye un grafo a partir del número de vértices
		this.numDeVertices = numDeVertices;
		for (i in 0..numDeVertices) {
			adj.add(ArrayList<ArcoCosto>());
		}
	}
	constructor(nombreArchivo: String) { // Se construye un grafo a partir de un archivo
		File(nombreArchivo).useLines { lines ->
			val iterator = lines.iterator()
			if (iterator.hasNext()) {
				numDeVertices = iterator.next().toInt()
				adj = ArrayList<ArrayList<ArcoCosto>>(numDeVertices + 1).apply { repeat(numDeVertices + 1) { add(ArrayList()) } }
			}
			if (iterator.hasNext()) {
				val E = iterator.next().toInt()
				iterator.forEachRemaining { line ->
					val tok = line.split(" ")
					if (tok.size == 3) {
						agregarArcoCosto(ArcoCosto(tok[0].toInt(), tok[1].toInt(), tok[2].toDouble()))
					}
				}
			}
		}
	}
	override operator fun iterator(): Iterator<ArcoCosto> { // Retorna todos los lados del digrafo
		val lados = mutableSetOf<ArcoCosto>()
		for (i in 1..numDeVertices) {
			for (e in adj[i]) {
				lados.add(e)
			}
		}
		return lados.iterator()
	}
}