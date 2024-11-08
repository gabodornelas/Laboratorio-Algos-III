package ve.usb.libGrafo
interface Grafo : Iterable<Lado> {
    fun obtenerNumeroDeLados() : Int // Retorna el número de lados del grafo
    fun obtenerNumeroDeVertices() : Int // Retorna el número de vértices del grafo
    fun adyacentes(v: Int) : Iterable<Lado> // Retorna los adyacentes de v, en este caso los lados que tienen como vértice inicial a v. 
    override operator fun iterator() : Iterator<Lado> // Retorna un iterador de los lados del grafo
}
