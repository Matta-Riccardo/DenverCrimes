package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private Graph<String, DefaultWeightedEdge> grafo;
	private EventsDao dao;
	
	private List<String> best;
	
	public Model() {
		dao = new EventsDao();
	}
	 
	public void creaGrafo(String categoria, int mese) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
//		Aggiunta vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
//		Aggiunta archi
		for(Adiacenza a : dao.getArchi(categoria, mese)) {
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		
		System.out.println("Grafo creato!");
		System.out.println("# VERTICI: " + this.grafo.vertexSet().size());
		System.out.println("# ARCHI: " + this.grafo.edgeSet().size());
		
	}
	
	public List<Adiacenza> getArchiMaggioriPesoMedio(){
//		Scorro sugli archi del grafo e calcolo il peso medio
		double pesoTot = 0.0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoTot += this.grafo.getEdgeWeight(e);
		}
		
		double avg = pesoTot / this.grafo.edgeSet().size();
		System.out.println(avg);
		
//		Ri-scorrotutti gli archi del grafo e prendo solo quelli il cui peso è maggiore della media appena calcolata
		List<Adiacenza> result = new ArrayList<Adiacenza>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) >= avg) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int) this.grafo.getEdgeWeight(e)));
			}
		}
		
		return result;
	}
	
	public List<String> calcolaPercorso(String sorgente, String destinazione){
		best = new LinkedList<>();
		List<String> parziale = new LinkedList<>();
		parziale.add(sorgente);
		
		cerca(parziale, destinazione);
		return best;
	}

	private void cerca(List<String> parziale, String destinazione) {
		
//		CONDIZIONE DI TERMINAZIONE
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
//			controllo se è la soluzione migliore
			if(parziale.size() > best.size()) {
				best = new LinkedList<>(parziale);
			} 
			return;
		}
		
		
//		esploro i percorsi che vanno verso tutti gli adiacenti, lancio la ricorsione verso il primo adiacente, poi faccio back-traking ed esploro pure a partire dal secondo adiacente e così via
//		(scorro i vicini dell'ultimo inserito e provo le varie "strade")
		for(String v : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(v)) {
				parziale.add(v);
				cerca(parziale, destinazione);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
	public List<Adiacenza> getArchi(){
		List<Adiacenza> archi = new ArrayList<Adiacenza>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			archi.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int) this.grafo.getEdgeWeight(e)));
		}
		return archi;
	}
	
	public List<String> getCategorie(){
		return this.dao.getCategorie();
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
}
