package ve.usb.libGrafo
import java.io.File
class GrafoNoDirigidoCosto:Grafo{
	var numeroDeLados = 0
	var numDeVertices = 0
	var adj = ArrayList<ArrayList<AristaCosto>>();
	override fun obtenerNumeroDeLados() = numeroDeLados // Retorna el número de lados del grafo
	override fun obtenerNumeroDeVertices() = numDeVertices // Retorna el número de vértices del grafo
	override fun adyacentes(v: Int) : Iterable<AristaCosto> = adj.get(v) // Retorna los lados adyacentes al vértice v
	override fun toString() = "V=$numDeVertices E=$numeroDeLados " + joinToString(", ") // Retorna un string con una representación del grafo
	fun grado(v: Int) = adj[v].size // Grado del del vértice v
	fun agregarAristaCosto(a: AristaCosto) {(adj.get(a.a)).add(a);(adj.get(a.b)).add(a);numeroDeLados++} // Agrega un lado al grafo no dirigido
	constructor(numDeVertices: Int) { // Se construye un grafo a partir del número de vértices
		this.numDeVertices = numDeVertices;
		for (i in 0..numDeVertices) {
			adj.add(ArrayList<AristaCosto>());
		}
	}
	constructor(nombreArchivo: String) {
		File(nombreArchivo).useLines { lines ->
			val iterator = lines.iterator()
			if (iterator.hasNext()) {
				numDeVertices = iterator.next().toInt()
				adj = ArrayList<ArrayList<AristaCosto>>(numDeVertices + 1).apply { repeat(numDeVertices + 1) { add(ArrayList()) } }
			}
			if (iterator.hasNext()) {
				val E = iterator.next().toInt()
				iterator.forEachRemaining { line ->
					val tok = line.split(" ")
					if (tok.size == 3) {
						agregarAristaCosto(AristaCosto(tok[0].toInt(), tok[1].toInt(), tok[2].toDouble()))
					}
				}
			}
		}
	}
	override operator fun iterator(): Iterator<AristaCosto> { // Retorna todos los lados del grafo no dirigido
		val lados = mutableSetOf<AristaCosto>()
		for (i in 1..numDeVertices) {
			for (e in adj[i]) {
				if (!lados.contains(e) && !lados.contains(e.invertir())) {
					lados.add(e)
				}
			}
		}
		return lados.iterator()
	}
}