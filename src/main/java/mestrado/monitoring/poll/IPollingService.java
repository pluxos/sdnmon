package mestrado.monitoring.poll;

import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import net.floodlightcontroller.core.module.IFloodlightService;

public interface IPollingService extends IFloodlightService{
	
	public List<Map<String, Object>> queryFlowsList();
	
	public Map<String, List<Map<String, Object>>> queryFlowTraffic(Integer flowID);
	
}
