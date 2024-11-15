//Mauricio Fragachan 20-10265
//Gabriel De Ornelas 15-10377

import java.io.File
import java.util.PriorityQueue

fun formatW(w: Double)=if(w%1.0==0.0) w.toInt().toString() else w.toString()

class Edge(val u:Int,val v:Int,val w:Double){override fun toString()="(${u+1},${v+1},${formatW(w)})"}

fun normalizeEdges(edges:List<Edge>):List<Edge>{
    return edges.map{e->if (e.u<e.v) e else Edge(e.v,e.u,e.w)}.sortedWith(compareBy({it.u},{it.v}))
}

class Graph {
    var V:Int = 0
    var E:Int = 0
    var adj=Array(V){mutableListOf<Edge>()}
    fun addEdge(u:Int, v:Int, w:Double) {
        adj[u].add(Edge(u,v,w))
        adj[v].add(Edge(v,u,w))
    }
    fun getEdges():List<Edge>{
        return this.adj.flatMap{edges->edges.filter{it.u<it.v}}
    }
    override fun toString(): String {
        val edges=normalizeEdges(getEdges())
        return "V=$V E=$E [${edges.joinToString(", ")}] W=${formatW(edges.sumOf{it.w})}"
    }
    // Constructor primario
    constructor(V:Int, edges:MutableList<Edge>){
        this.V = V
        this.E = edges.size
        this.adj = Array(V){mutableListOf<Edge>()}
        edges.forEach{addEdge(it.u,it.v,it.w)}
    }
    // Constructor secundario que lee desde un archivo
    constructor(fileName: String) {
        File(fileName).useLines{lines->
            val iterator = lines.iterator()
            this.V = iterator.next().toInt()
            this.E = iterator.next().toInt()
            this.adj = Array(V){mutableListOf<Edge>()}
            iterator.forEachRemaining{line->
                val parts = line.split(" ")
                val u = parts[0].toInt()-1
                val v = parts[1].toInt()-1
                val w = parts[2].toDouble()
                addEdge(u,v,w)
            }
        }
    }
    fun mstPrim(): List<Edge> {     // Prim
        val mst = mutableListOf<Edge>()
        val visited = BooleanArray(V)
        val pq = PriorityQueue<Edge>(compareBy{it.w})
        fun visit(node: Int) {
            visited[node] = true
            pq.addAll(adj[node])
        }
        for (start in 0 until V) {
            if (!visited[start]) {
                visit(start)
                while (pq.isNotEmpty()) {
                    val edge = pq.poll()
                    if (!visited[edge.v]) {
                        mst.add(edge)
                        visit(edge.v)
                    }
                }
            }
        }
        return normalizeEdges(mst)
    }
    fun mstKruskal(): List<Edge> {     // Kruskal
        val mst = mutableListOf<Edge>()
        val pq = PriorityQueue<Edge>(compareBy{it.w})
        val et = mutableListOf<Int>()
        pq.addAll(getEdges())
        for(i in 0 until V){    et.add(i)   }
        while (pq.isNotEmpty()) {
            if(et.all{it==et[0]}){  break   }
            val edge = pq.poll();   var valmay = V;     var valmen = 0;
            if (et[edge.v] != et[edge.u]) {
                mst.add(edge)
                if(et[edge.v] > et[edge.u]){    valmay = et[edge.v];    valmen = et[edge.u];
                }else{  valmay = et[edge.u];    valmen = et[edge.v];
                }
                for(i in 0 until V){
                    if(et[i] == valmay){    et[i] = valmen}
                }
            }
        }    
        return normalizeEdges(mst)
    }
}

fun main() {
    fun report(V:Int,e:List<Edge>,label:String) {
        println(label+"=[${e.joinToString(", ")}] W="+formatW(e.sumOf{it.w}))
        val lines = if(e.size<1) "" else "\n"+e.joinToString("\n"){"${it.u+1} ${it.v+1} ${formatW(it.w)}"}
        //File(label).writeText("$V\n${e.size}$lines")
    }
    File(".").listFiles{file->file.extension=="txt" && file.name.startsWith("G")}?.forEach{txt->
        val g=Graph(txt.name)
        val p=g.mstPrim()
        val k=g.mstKruskal()
        println("*".repeat(100))
        println(txt.name+" "+g)
        report(g.V,p,"mstP_"+txt.name)
        report(g.V,k,"mstK_"+txt.name)
    }
}