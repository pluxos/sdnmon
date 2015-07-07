package mestrado.monitoring.poll;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mestrado.flow.FlowEntry;
import mestrado.flow.FlowInstructions;
import mestrado.flow.FlowMatch;
import mestrado.flow.FlowStatistics;
import mestrado.monitoring.FileIO;
import mestrado.monitoring.database.ConnectDatabase;
import mestrado.monitoring.database.DeleteRow;
import mestrado.monitoring.database.UpdateRow;
import mestrado.monitoring.database.InsertRow;
import mestrado.monitoring.database.RetrieveRow;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlowStatsRequest;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;

public class Polling{
	
	private Scheduler scheduler = new Scheduler();
	private FileIO fileIO = new FileIO();
	private boolean cleanThreadFlag = false;
	/*** Avoid create objects below many times ***/
	private ConnectDatabase cd = new ConnectDatabase();
	private InsertRow insertRow = new InsertRow();
	private RetrieveRow retrieveRow = new RetrieveRow();
	private UpdateRow updateRow = new UpdateRow();
	/***/
	
	public void main(Map<DatapathId, IOFSwitch> map, Ethernet eth){
		if(verifyPacketReceived(eth) == false){ // Accept only IP Packets!
			return;
		}
		synchronized (this) { //Multi-Threaded Free.
			createFlowsThread(map);
			createCleanerThread(cd);
		}
	}
	

	private boolean verifyPacketReceived(Ethernet eth) {
		// TODO Auto-generated method stub
		if (eth.getEtherType().getValue() == Ethernet.TYPE_IPv4){
			return true;
		}
		return false;
	}

	/***
	 * Thread will verify if the switch has a initial thread. If not, create it.
	 * @param map
	 */
	private void createFlowsThread(Map<DatapathId, IOFSwitch> map) {
		// TODO Auto-generated method stub
		Set<Entry<DatapathId, IOFSwitch>> entrySet = new HashSet<Entry<DatapathId, IOFSwitch>>(map.entrySet());
		Iterator<Entry<DatapathId, IOFSwitch>> iterator = entrySet.iterator();
		while(iterator.hasNext()){
			Entry<DatapathId, IOFSwitch> entry = iterator.next();
			IOFSwitch sw = entry.getValue();
			verifyFlowsThread(sw);
		}
	}
	
	public void verifyFlowsThread(IOFSwitch sw) {
		// TODO Auto-generated method stub
		 DatapathId dpid = sw.getId(); //Switch's identification
		 Boolean result = scheduler.createFirstThread(dpid);
		 if(result == true){
			 ConnectDatabase cd = new ConnectDatabase(); // One connection for each switch.
			 createPollingThread(sw, new Scope.ScopeBuilder().build(sw), cd);
		 }
	}
	


	/***
	 * Create Thread to clean database according to the maximum allowed time.
	 * @param cd
	 */
	private void createCleanerThread(ConnectDatabase cd) {
		// TODO Auto-generated method stub
		
			class CleanerThread implements Runnable{
				
				private ConnectDatabase cd;
				private DeleteRow dr;
				
				public CleanerThread(ConnectDatabase cd){
					this.cd = cd;
					this.dr = new DeleteRow();
				}
	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(true){
						Statement stmt = cd.createStatement();
						dr.deleteRowsTimeExpiration(stmt);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cd.closeStatement(stmt);
					}
				}	
			}
			if(cleanThreadFlag == false){
				Thread cleaner = new Thread(new CleanerThread(cd));
				cleaner.start();
				cleanThreadFlag = true;
		    }
	}

	/***
	 * Create Thread and start the Polling process.
	 * @param dpid
	 */
	public void createPollingThread(IOFSwitch sw, Scope scope, ConnectDatabase cd){
		class SwitchThread implements Runnable{
			private Boolean destroyThread = false;
			private Scope scope;
			private IOFSwitch sw;
			private ConnectDatabase cd;
			
			public SwitchThread(IOFSwitch sw, Scope scope, ConnectDatabase cd){
				this.sw = sw;
				this.scope = scope;
				this.cd = cd;
			}
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(destroyThread == false){
					List<OFFlowStatsEntry> statsReply = queryFlowsStatistics(sw, scope);
					List<FlowEntry> flowStatsList = processFlowsStatistics(statsReply, sw.getId().toString());
					System.out.println(Thread.currentThread().getName()+":"+flowStatsList);
					storeFlowData(flowStatsList, cd);
					List<Scope> newScopeList = scheduler.analyse(flowStatsList, sw, scope.getNoNWildcardedFields());
					int i;
					if(newScopeList.size() > 0){ // Means we have new scope(s), destroy this thread and create another.
						destroyThread = true;
						for(i=0; i < newScopeList.size(); i++){ //create new threads, one for each scope.
							createPollingThread(sw, newScopeList.get(i), cd);
						}
					}
					fileIO.writeFile(null, null, false);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		Thread task = new Thread(new SwitchThread(sw, scope, cd), scope.getThreadName());
		task.start();
	}

	
	/***
	 * Amortize data to avoid peaks.
	 */
	public void amortize() {
		// TODO Auto-generated method stub
		
	}
	
	
	/*
	 * Observação: Serão adicionados na lista apenas tráfegos UDP, TCP (exceto ACKs). O escalonador só analisará esses fluxos.
	 */
	public List<FlowEntry> processFlowsStatistics(List<OFFlowStatsEntry> statsReply, String switchDPID) {
		// TODO Auto-generated method stub
		int i;
		List<FlowEntry> flowEntries = new ArrayList<FlowEntry>();
		for(i=0; i<statsReply.size(); i++){
		//Tem que colocar dentro do "for" a instanciação dos 3 objetos abaixo porque senão vai mudar a referência de todos os Objetos FlowEntry
			FlowMatch flowMatch = new FlowMatch();
			FlowStatistics flowStatistics = new FlowStatistics();
			FlowInstructions flowInstructions = new FlowInstructions();
			OFFlowStatsEntry statsEntry = statsReply.get(i);
			Match match = statsEntry.getMatch();
			flowMatch.setFlowMatch(match);
			flowInstructions.setFlowInstructions(statsEntry.getInstructions());
			if(flowMatch.getTransportProtocol() == null /*Não colocar tráfego menor que camada 4.*/ || 
					(flowMatch.getTransportSourcePortNumber() < flowMatch.getTransportDestinationPortNumber())){ /*Um jeito rápido de eliminar os ACKS.*/ 
				continue;
			}
			flowStatistics.setFlowStatistics(statsEntry);
			flowEntries.add(new FlowEntry(switchDPID, flowMatch, flowInstructions, flowStatistics));
		}
		return flowEntries;
	}

	/**
     * @param sw
     *            the switch object that we wish to get the flows from
     * @param match
     *            define the parameters we are searching for the flows.
     * @return a list of OFFlowStatisticsReply objects or essentially flows
     */
    public List<OFFlowStatsEntry> queryFlowsStatistics(IOFSwitch sw, Scope scope) {

        List<OFFlowStatsEntry> statsReply = new ArrayList<OFFlowStatsEntry>();
        List<OFFlowStatsReply> values = null;
        Future<List<OFFlowStatsReply>> future;
        OFFlowStatsRequest flowStatsRequest = scope.getScopeStatsRequest();
        try {
            future = sw.writeStatsRequest(flowStatsRequest);
            values = future.get(1, TimeUnit.SECONDS);
            if (values != null) {
                for (OFFlowStatsEntry stat : values.get(0).getEntries()) {
                    statsReply.add(stat);
                }
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return statsReply;
    }

	/***
	 * Store the data in Database
	 * @param flowStatsList 
	 */
	public void storeFlowData(List<FlowEntry> flowStatsList, ConnectDatabase cd) {
		// TODO Auto-generated method stub
		int i = 0;
		Statement stmt = cd.createStatement();
		for(i=0; i<flowStatsList.size(); i++){
			FlowEntry fe = flowStatsList.get(i);
			FlowEntry oldFlowEntry = retrieveRow.retrieve(fe.getSwitchID(), fe.getFlowID(), stmt);
			if(oldFlowEntry == null){ //Nenhum fluxo lá ainda ...
				FlowStatistics fs = fe.getFlowStatistics();
				Double currentTransmissionRate = 0d;
				fs.setCurrentTransmissionRate(currentTransmissionRate);
				insertRow.insertFlowDB(stmt, fe);
			}
			else{
				FlowStatistics fs = fe.getFlowStatistics();
				Long oldTime = oldFlowEntry.getFlowStatistics().getTime();
				Long currentTime = fs.getTime();
				Long receivedBytes = fs.getReceivedBytes();
				Long oldReceivedBytes = oldFlowEntry.getFlowStatistics().getReceivedBytes();
				if(oldReceivedBytes == receivedBytes){
					fs.setCurrentTransmissionRate(0.0);
					updateRow.updateFlow(fe.getSwitchID(), fe.getFlowID(), stmt, fs);
					return;
				}
				else{
					Double currentTransmissionRate = fs.calculateTransmission(oldReceivedBytes, receivedBytes, 
							oldTime, currentTime, fs.getUnit()); //Problema tá aqui e na linha de baixo!!!
					fs.setCurrentTransmissionRate(currentTransmissionRate);
					updateRow.updateFlow(fe.getSwitchID(), fe.getFlowID(), stmt, fs);
				}	
			}	
			//amortize();
		}
		cd.closeStatement(stmt);
	}
	
	/***Offered Services***/
	public List<Map<String, Object>> queryFlowsList() {
		// TODO Auto-generated method stub
		return retrieveRow.getFlowsList(cd.getConnection());
	}

	public Map<String, List<Map<String, Object>>> queryFlowTraffic(Integer flowID) {
		// TODO Auto-generated method stub
		return retrieveRow.getFlowTraffic(cd.getConnection(), flowID);
	}
	/***/
	
	

}
