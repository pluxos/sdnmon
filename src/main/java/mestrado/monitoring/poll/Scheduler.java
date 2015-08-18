package mestrado.monitoring.poll;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Set;

import mestrado.flow.FlowEntry;
import mestrado.flow.FlowMatch;
import mestrado.monitoring.database.ConnectDatabase;
import mestrado.monitoring.database.DeleteRow;
import mestrado.monitoring.database.RetrieveRow;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.forwarding.IForwardingAuxService;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;


public class Scheduler {
	
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private Integer minimumAllowedPort = 0; //Se o número de *portas* ativas de *um switch* for menor ou igual a isso, junta.
	private Integer minimumAllowedIP = 0; //Se o número de *ips* ativos de *uma porta* for menor ou igual a isso, junta.
	private Integer thresholdSplit = 3; //Se já tiver 3 fluxos, quebra.
	private ConnectDatabase cd = new ConnectDatabase();
	private RetrieveRow rr = new RetrieveRow();
	private DeleteRow dr = new DeleteRow();
	private Set<DatapathId> switchesSet = new HashSet<DatapathId>(); //Avoid creation of new initial threads.
	private Map<DatapathId, IOFSwitch> dpidSwitchMapping = new HashMap<DatapathId, IOFSwitch>();//Mapping Switch DPID -> Switch
	private Map<String, Integer> switchIPPortMapping = new HashMap<String, Integer>(); //(Switch+IP)->Port Mapping.
	private Set<ThreadsAux> queueOfThreads = new HashSet<ThreadsAux>(); //Threads waiting to be created.
	private List<String> switchPortSet = new ArrayList<String>(); //(Switch + Port) mapping IP.
	private IForwardingAuxService auxForwardingService;
	public final static Integer tableValue = 0; //Define 0 for now.
	
	class ThreadsAux{
		private IOFSwitch sw;
		private Scope scope;
		
		public ThreadsAux(IOFSwitch sw, Scope scope){
			this.sw = sw;
			this.scope = scope;
		}
		
		public IOFSwitch getSw() {
			return sw;
		}

		public Scope getScope() {
			return scope;
		}
	}
	
	
	private void startManageThreadsDeletion() {
		class ManageThreadsDeletion implements Runnable{ //Thread de *escrita*, nenhum leitor pode ler o BD enquando ela estiver escrevendo.
			// A idéia principal aqui é fazer a "junção" das Threads.
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					Statement stmt = cd.createStatement();
					rwlock.writeLock().lock();  //Recebi o Lock, as threads de leitura devem aguardar.
					populateAndUpdateThreadMons(stmt);
					rwlock.writeLock().unlock();//Liberei o Lock, as threads de leitura podem ler.
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cd.closeStatement(stmt);
				}	
			}
	
			private void populateAndUpdateThreadMons(Statement stmt) {
				// TODO Auto-generated method stub
				List<MatchMon> matchMonList = auxForwardingService.getMatchMonList();
				List<ThreadMon> list = rr.getThreads(stmt);
				Map<String, List<ThreadMon>> portMap = new HashMap<String, List<ThreadMon>>();
				Map<String, List<ThreadMon>> ipMap = new HashMap<String, List<ThreadMon>>();
				fillMap(list, portMap, ipMap); //Separa em threads granularidade de Porta e IP.
				updatePortThreads(portMap); //Processa Granularidade de Porta.
				updateIPThreads(ipMap); //Processa Granularidade de IP.
				updateIPThreadsAux(matchMonList);
			}
	
			
			
			/***
			 * A idéia desse método é receber os novos fluxos que foram inseridos na rede e criar uma thread para esses fluxos, caso a granularidade
			 * desse fluxo seja IP.   
			 * @param matchMonList
			 */
			private void updateIPThreadsAux(List<MatchMon> matchMonList) {
				// TODO Auto-generated method stub
				int i;
				for(i=0; i<switchPortSet.size(); i++){
					String key = switchPortSet.get(i);
					int j;
					for(j=0; j<matchMonList.size(); j++){
						MatchMon mm = matchMonList.get(j);
						if(mm.toString().contains(key)){
							if(!switchIPPortMapping.containsKey(key)){
								IOFSwitch sw = dpidSwitchMapping.get(DatapathId.of(mm.getDpid()));
								switchIPPortMapping.put(mm.getDpid()+","+mm.getSourceIP()+","+mm.getDestinationIP(), mm.getOutputPort().intValue());
								Scope scope = new Scope.ScopeBuilder().ip(mm.getSourceIP(), mm.getDestinationIP()).build(sw);
								ThreadsAux threadAux = new ThreadsAux(sw, scope);
								queueOfThreads.add(threadAux);
							}
						}
					}
				}
				matchMonList.clear(); //Acredito que não seria necessário sincronizar ou declarar essa variável como volatile.
			}

			private void updateIPThreads(Map<String, List<ThreadMon>> ipMap) {
				// TODO Auto-generated method stub
				Statement stmt = cd.createStatement();
				Set<Entry<String, List<ThreadMon>>> entrySet = ipMap.entrySet();
				Iterator<Entry<String, List<ThreadMon>>> iterator = entrySet.iterator();
				while(iterator.hasNext()){
					Entry<String, List<ThreadMon>> entry = iterator.next();
					List<ThreadMon> list = entry.getValue();
					int i;
					int counterActive = 0;
					for(i=0; i<list.size(); i++){
						ThreadMon entryThread = list.get(i);
						if(entryThread.getStatus().equals("active")){
							counterActive++;
						}
					}
					if(counterActive <= minimumAllowedIP){
						for(i=0; i<list.size(); i++){
							ThreadMon entryThread = list.get(i);
							dr.removeThreadsBDbySwitchIDandIP(stmt, entryThread.getSwitchID(), 
									entryThread.getSourceIP(), entryThread.getDestinationIP());
						}
						String[] attr = entry.getKey().split(",");
						DatapathId dpid = DatapathId.of(attr[0]);
						IOFSwitch sw = dpidSwitchMapping.get(dpid);
						Scope scope = new Scope.ScopeBuilder().outputPort(Integer.parseInt(attr[1])).build(sw);
						ThreadsAux threadAux = new ThreadsAux(sw, scope);
						queueOfThreads.add(threadAux);
						switchPortSet.remove(entry.getKey());
					}
				}
				cd.closeStatement(stmt);
			}
	
			private void updatePortThreads(Map<String, List<ThreadMon>> portMap) {
				// TODO Auto-generated method stub
				Statement stmt = cd.createStatement();
				Set<Entry<String, List<ThreadMon>>> entrySet = portMap.entrySet();
				Iterator<Entry<String, List<ThreadMon>>> iterator = entrySet.iterator();
				while(iterator.hasNext()){
					Entry<String, List<ThreadMon>> entry = iterator.next();
					DatapathId dpid = DatapathId.of(entry.getKey());
					IOFSwitch sw = dpidSwitchMapping.get(dpid);
					if(sw == null){
						return;
					}
					List<ThreadMon> list = entry.getValue();
					if(sw.getEnabledPortNumbers().size() == list.size()){ //Verificar se o número de Threads das portas do switch é igual ao número de portas do switch, com isso evita a junção de threads na granularidade IP. 
						int i;
						int counterActive = 0;
						for(i=0; i<list.size(); i++){
							ThreadMon entryThread = list.get(i);
							if(entryThread.getStatus().equals("active")){
								counterActive++;
							}
						}
						if(counterActive <= minimumAllowedPort){
							dr.removeThreadsBDbySwitchID(stmt, entry.getKey());
							Scope scope = new Scope.ScopeBuilder().build(sw);
							ThreadsAux threadAux = new ThreadsAux(sw, scope);
							queueOfThreads.add(threadAux);
							//Voltar em uma única thread.
						}
					}	
				}
				cd.closeStatement(stmt);
			}
	
			private void fillMap(List<ThreadMon> list, 
					Map<String, List<ThreadMon>> portMap, Map<String, List<ThreadMon>> ipMap) {
				// TODO Auto-generated method stub
				int i;
				for(i=0; i< list.size(); i++){
					ThreadMon thread = list.get(i);
					//System.out.println("Thread:"+thread.getName());
					if(thread.getOutPort() != -1){
						portGranularity(portMap, thread);
					}
					else if(!thread.getSourceIP().equals("-1") ){
						ipGranularity(ipMap, thread);
					}	
				}
			}
			
			private void portGranularity(Map<String, List<ThreadMon>> portMap, ThreadMon thread) {
				// TODO Auto-generated method stub
				String switchID = thread.getSwitchID();
				if(portMap.get(switchID) == null){
					List<ThreadMon> listThreads = new ArrayList<ThreadMon>();
					listThreads.add(thread);
					portMap.put(switchID, listThreads);
				}
				else{
					List<ThreadMon> listThreads = portMap.get(switchID);
					listThreads.add(thread);
				}
			}
			
			
			private void ipGranularity(Map<String, List<ThreadMon>> ipMap, ThreadMon thread) {
				// TODO Auto-generated method stub
				String switchID = thread.getSwitchID();
				String ips = thread.getSourceIP() + "," + thread.getDestinationIP();
				String key = switchID + "," + ips;
				Integer outputPort = switchIPPortMapping.get(key);
				String key2 = switchID+","+outputPort;
				if(ipMap.get(key2) == null){
					List<ThreadMon> listThreads = new ArrayList<ThreadMon>();
					listThreads.add(thread);
					ipMap.put(key2, listThreads);
				}
				else{
					List<ThreadMon> listThreads = ipMap.get(key2);
					listThreads.add(thread);
				}
			}
			
		}
		Thread task = new Thread(new ManageThreadsDeletion());
		task.start();
	}
	//******************************************************************************
	
	boolean manageThreadDeletionFlag = false;
	
	//****************************** Daqui em diante é feito a quebra de Threads em outras mais específicas***************************************
	/***
	 * Verify if the Thread can be created. If yes, create it.
	 * @param sw
	 * @return
	 */
	protected Boolean createFirstThread(IOFSwitch sw){
		if(manageThreadDeletionFlag == false){
			manageThreadDeletionFlag = true;
			startManageThreadsDeletion();
		}
		DatapathId dpid = sw.getId();
		if(switchesSet.contains(dpid)){
			return false;
		}
		switchesSet.add(dpid);
		dpidSwitchMapping.put(dpid, sw);
		return true;
	}

	/***
	 * Analyse the Thread's collected data and give a feedback to the Thread. 
	 * @param nonWildcardedFields 
	 */
	public List<Scope> analyse(List<FlowEntry> flowEntryList, IOFSwitch sw, Scope scope){
		List<String> nonWildcardedFields = scope.getNoNWildcardedFields();
		List<Scope> scopeList = new ArrayList<Scope>();
		if(flowEntryList == null){ // No flows.
			return scopeList;
		}
		if(flowEntryList.size() >= thresholdSplit){ // Create new Scope 
			Integer type = verifyNoNWildcardedFields(nonWildcardedFields); // Only once, because they're all the same.
			if(type == 0){
				scopeList = outPortGranularity(sw);
			}
			
			else if(type == 1){ 
				scopeList = matchGranularity(flowEntryList, sw, scope.getOutputPort()); // Use the IPs Addresses as default.
			}
			//Aumentar depois ...
		}
		return scopeList;
	}


	/***
	 * Define the NoN Wildcardeds fields for Querying.
	 * @param nonWildcardedFields
	 * @return
	 */
	private Integer verifyNoNWildcardedFields(List<String> nonWildcardedFields) {
		// TODO Auto-generated method stub
		if(nonWildcardedFields.size() == 0){ //All Wildcarded.
			return 0;
		}
		else if(nonWildcardedFields.size() == 1 && nonWildcardedFields.contains("outputPort")){ // Means the scope already has the port.
			return 1;
		}
		return 2;
		//Desenvolver algum algoritmo para fazer algo mais completo. Combinações?
	
	}

	private List<Scope> outPortGranularity(IOFSwitch sw) {
		// TODO Auto-generated method stub
		List<Scope> scopeList = new ArrayList<Scope>();
		Iterator<OFPort> portsIterator = sw.getEnabledPortNumbers().iterator();
		while(portsIterator.hasNext()){ //One Scope for each port, except the Local Port.
			Integer outputPort = portsIterator.next().getPortNumber();
			if(outputPort == -2){
				break;
			}
			scopeList.add(new Scope.ScopeBuilder().
					outputPort(outputPort).
					build(sw));
		}
		return scopeList;
	}

	private List<Scope> matchGranularity(List<FlowEntry> flowEntryList, IOFSwitch sw, Integer portNumber) {
		// TODO Auto-generated method stub
		int i;
		List<Scope> scopeList = new ArrayList<Scope>();
		Set<String> temp = new HashSet<String>();
		switchPortSet.add(sw.getId().toString() +","+ portNumber); //Quer dizer que na tabela de Threads não existe mais esse switch ID com esta porta
		for(i=0; i<flowEntryList.size(); i++){
			FlowEntry flowEntry = flowEntryList.get(i);
			FlowMatch flowMatch = flowEntry.getFlowMatch();
			String sourceIP = flowMatch.getSourceIP();
			String destinationIP = flowMatch.getDestinationIP();
			if(temp.contains(sourceIP+","+destinationIP) == false && 
					(sourceIP != null && destinationIP != null)){ //Evitar que crie vários escopos para um mesmo match. Caso algum fluxo do switch
																  //não tenha granularidade de ip, ignorar ...
				temp.add(sourceIP+","+destinationIP);
				scopeList.add(new Scope.ScopeBuilder().
								ip(sourceIP, destinationIP).
								build(sw));
				switchIPPortMapping.put((sw.getId().toString() + "," + sourceIP+","+destinationIP), portNumber); //Mapear IP -> Porta
			}
		}
		return scopeList;
	}
	

	public Set<ThreadsAux> getQueueOfThreads() {
		return queueOfThreads;
	}
	
	public void setForwardingService(IForwardingAuxService auxForwardingService){
		if(this.auxForwardingService == null){
			this.auxForwardingService = auxForwardingService;
		}
	}
	
	public ReadWriteLock getRwlock() {
		return rwlock;
	}
	
}
