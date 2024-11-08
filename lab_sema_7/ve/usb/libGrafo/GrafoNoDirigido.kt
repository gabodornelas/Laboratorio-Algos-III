package ve.usb.libGrafo
import java.io.File
class GrafoNoDirigido:Grafo{
	var numeroDeLados = 0
	var numDeVertices = 0
	var adj = ArrayList<ArrayList<Arista>>();
	override fun obtenerNumeroDeLados() = numeroDeLados 
	override fun obtenerNumeroDeVertices() = numDeVertices
	override fun adyacentes(v: Int) : Iterable<Arista> 	= adj.get(v) // Retorna los lados adyacentes al vértice v
	override fun toString() = "V=$numDeVertices E=$numeroDeLados " + joinToString(", ") // Retorna un string con una representación del grafo
	fun grado(v: Int) = adj[v].size // Grado del vértice v
	fun agregarArista(a: Arista) {(adj.get(a.a)).add(a);(adj.get(a.b)).add(a);numeroDeLados++;} // Agrega un lado al grafo no dirigido
	constructor(numDeVertices: Int) {// Se construye un grafo a partir del número de vértices
		this.numDeVertices = numDeVertices;
		for (i in 0..numDeVertices) {
			adj.add(ArrayList<Arista>());
		}
	}
	constructor(nombreArchivo: String) { // Se construye un grafo a partir de un archivo
		File(nombreArchivo).useLines { lines ->
			val iterator = lines.iterator()
			if (iterator.hasNext()) {
				numDeVertices = iterator.next().toInt()
				adj = ArrayList<ArrayList<Arista>>(numDeVertices + 1).apply { repeat(numDeVertices + 1) { add(ArrayList()) } }
			}
			if (iterator.hasNext()) {
				val E = iterator.next().toInt()
				iterator.forEachRemaining { line ->
					val tok = line.split(" ")
					if (tok.size == 2) {
						agregarArista(Arista(tok[0].toInt(), tok[1].toInt()))
					}
				}
			}
		}
	}
	override operator fun iterator(): Iterator<Arista> { // Retorna todos los lados del grafo no dirigido
		val lados = mutableSetOf<Arista>()
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