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
	//iniciar, seleccionar arista, actualizar
	var cola_adyacente = PriorityQueue<Triple<Int,Int,Double>>(compareBy {it.third})
	var lista_adyacente: MutableList<Triple<Int,Int,Double>> = mutableListOf()
	var cola_vertice = PriorityQueue<Int>()
	var lista_vertice: MutableList<Int> = mutableListOf()
	val V = g.obtenerNumeroDeVertices()
	for(i in 1..V){
		if(i !in lista_vertice){
			lista_vertice.clear()
			lista_adyacente.clear()
			cola_vertice.add(i)
			var adyacentes = g.adyacentes(i)
			for(j in adyacentes){
				cola_adyacente.add(Triple(j.a,j.b,j.costo))
			}		
			while(cola_adyacente.isNotEmpty()){
				var primero = cola_adyacente.poll()
				if(primero.first !in cola_vertice){	
					lista_adyacente.add(primero)				
					adyacentes = g.adyacentes(primero.first)
					for(j in adyacentes){
						if(j.a !in cola_vertice && j.b !in cola_vertice){
							cola_adyacente.add(Triple(j.a,j.b,j.costo))
						}
					}
					cola_vertice.add(primero.first)
				}
				if(primero.second !in cola_vertice){
					lista_adyacente.add(primero)				
					adyacentes = g.adyacentes(primero.second)
					for(j in adyacentes){
						if(j.a !in cola_vertice && j.b !in cola_vertice){
							cola_adyacente.add(Triple(j.a,j.b,j.costo))
						}
					}
					cola_vertice.add(primero.second)
				}
			}
			while(cola_vertice.isNotEmpty()){
				lista_vertice.add(cola_vertice.poll())
			}
			val par = Pair(lista_vertice,lista_adyacente)
			m.add(par)
			println(m)
		}
	}
	println("m final $m")
	return m
}

fun main(args: Array<String>) {
	val t = seleccionaArchivoTxt("Seleccionar archivo de grafo")?:exitProcess(1)
	val g = GrafoNoDirigidoCosto(t)?:exitProcess(1)
	println("$t $g")
	getMSTbyPrim(g).forEach{println("CC=${it.first} E=[${formatLista(it.second)}] W=${formatDouble(it.second.sumOf{it.third})}")}
 }