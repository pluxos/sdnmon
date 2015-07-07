package mestrado.monitoring.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mestrado.flow.FlowStatistics;

public class ConstructObjectJson {
	

	public Map<String, Object> constructFlowList(String sourceIP, String destinationIP,
			String transportProtocol, Integer sourcePort, Integer destinationPort) {
		// TODO Auto-generated method stub
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sourceIP", sourceIP);
		map.put("destinationIP", destinationIP);
		map.put("transportProtocol", transportProtocol);
		map.put("sourcePort", sourcePort);
		map.put("destinationPort", destinationPort);
		return map;
	}


	public Map<String, List<Map<String, Object>>> constructSwitchPortMap(String switchDPID, 
			Integer portNumber, Integer weight, Double transmissionRate, Integer totalWeight, Map<String, List<Map<String, Object>>> map) {
		// TODO Auto-generated method stub
		Double effectiveTransmissionRate;
		if(totalWeight == null && weight == null){ // Sem grupo.
			effectiveTransmissionRate = FlowStatistics.applyPrecision(transmissionRate, 3);
		}
		else{
			effectiveTransmissionRate = FlowStatistics.applyPrecision(((transmissionRate*weight)/totalWeight), 3); //Com grupo.
		}
		if(!map.containsKey(switchDPID)){
			List<Map<String, Object>> list= new ArrayList<Map<String, Object>>();
			Map<String, Object> aux = new HashMap<String, Object>();
			aux.put("portNumber", portNumber);
			aux.put("rate", effectiveTransmissionRate);
			list.add(aux);
			map.put(switchDPID, list);
			return map;
		}
		else{
			List<Map<String, Object>> list = map.get(switchDPID);
			if(list == null){
				list = new ArrayList<Map<String,Object>>();
				Map<String, Object> aux = new HashMap<String, Object>();
				aux.put("portNumber", portNumber);
				aux.put("rate", effectiveTransmissionRate);
				list.add(aux);
				map.put(switchDPID, list);
				return map;
			}
			else{
				Map<String, Object> aux = new HashMap<String, Object>();
				aux.put("portNumber", portNumber);
				aux.put("rate", effectiveTransmissionRate);
				list.add(aux);
				return map;
			}
		}
	}

}
