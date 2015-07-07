package mestrado.monitoring.poll.resources;

import java.util.List;
import java.util.Map;

import mestrado.monitoring.poll.IPollingService;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class FlowTrafficResource extends ServerResource{
	
    @Get("json")
    public Map<String, List<Map<String, Object>>> getFlowTraffic() {
        IPollingService pollingService =
                (IPollingService)getContext().getAttributes().
                get(IPollingService.class.getCanonicalName());

        String flowID = (String) getRequestAttributes().get("flow-id");
        
        return pollingService.queryFlowTraffic(Integer.parseInt(flowID));
        
    }
    
}
