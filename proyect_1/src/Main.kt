//Mauricio Fragachan 20-10265
//Gabriel De Ornelas 15-10377

import ve.usb.libGrafo.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.system.exitProcess
import java.util.PriorityQueue

val clases=arrayOf("GrafoNoDirigidoCosto","GrafoNoDirigido","GrafoDirigidoCosto","GrafoDirigido")
fun formatDouble(costo: Double)=if(costo%1==0.0) costo.toInt().toString() else costo.toString()
fun formatLista(t:List<Triple<Int,Int,Double>>)=t.joinToString(separator=", "){(a,b,w)->"($a, $b, ${formatDouble(w)})"}

fun seleccionaArchivoTxt(prompt:String):String?{
	val directory = File(".")
	val txtFiles = directory.listFiles{ _, name -> clases.any { name.contains(it) } && name.endsWith(".txt")}?.sortedBy{it.name}
	if (txtFiles != null && txtFiles.isNotEmpty()) {
		val fileChooser = JFileChooser(directory)
		fileChooser.dialogTitle = "Selecciona un archivo TXT"
		fileChooser.isMultiSelectionEnabled = false
		fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
		val options = txtFiles.map{it.name}.toTypedArray()
		val selectedFile = JOptionPane.showInputDialog(
			null,
			prompt,
			"Selector de archivos *.txt",
			JOptionPane.PLAIN_MESSAGE,
			null,
			options,
			options[0]
		)
		return selectedFile  as String?
	}
	return null
}

fun getMSTbyPrim(g: GrafoNoDirigidoCosto): List<Pair<List<Int>, List<Triple<Int, Int, Double>>>> {
	val m: MutableList<Pair<List<Int>, List<Triple<Int, Int, Double>>>> = mutableListOf()
	var cola_arista = PriorityQueue<Triple<Int,Int,Double>>(compareBy {it.third}) // la cola se ordena por costo
	var lista_arista: MutableList<Triple<Int,Int,Double>> = mutableListOf()
	var lista_vertice: MutableList<Int> = mutableListOf()
	val V = g.obtenerNumeroDeVertices()
	var nuevo_vertice = 0
	for(i in 1..V){
		if(i !in lista_vertice){
			lista_vertice.clear()
			lista_arista.clear()
			lista_vertice.add(i)	//Primer vertice de la componente conexa
			var aristas = g.adyacentes(i)
			for(j in aristas){	//Inicializamos la cola con aristas que inciden en i
				cola_arista.add(Triple(j.a,j.b,j.costo))
			}		
			while(cola_arista.isNotEmpty()){	//Al finalizar el bucle while tenderemos la componente conexa formada
				var primero = cola_arista.poll()	//primero es la arista de menor costo en la cola
				if(primero.first !in lista_vertice || primero.second !in lista_vertice){	
					lista_arista.add(primero)
					if(primero.first !in lista_vertice){
						aristas = g.adyacentes(primero.first)
						nuevo_vertice = primero.first
					}else{
						aristas = g.adyacentes(primero.second)
						nuevo_vertice = primero.second
					}
					for(j in aristas){
						if(j.a !in lista_vertice && j.b !in lista_vertice){
							cola_arista.add(Triple(j.a,j.b,j.costo))
						}
					}
					//Agregamos el vertice a la lista luego de agregar las aristas que inciden en él a la cola_arista
					lista_vertice.add(nuevo_vertice)
				}
			}
			var lista_ver = lista_vertice.sortedBy{ it }	//ordenamos la lista de vertices
			m.add(Pair(lista_ver,lista_arista.toList()))
		}
	}
	return m
}

fun main(args: Array<String>) {
	val t = seleccionaArchivoTxt("Seleccionar archivo de grafo")?:exitProcess(1)
	val g = GrafoNoDirigidoCosto(t)?:exitProcess(1)
	println("$t $g")
	getMSTbyPrim(g).forEach{println("CC=${it.first} E=[${formatLista(it.second)}] W=${formatDouble(it.second.sumOf{it.third})}")}
 }
