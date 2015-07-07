package mestrado.monitoring.poll.resources;

import java.util.List;
import java.util.Map;

import mestrado.monitoring.poll.IPollingService;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class FlowsListResource extends ServerResource{
    @Get("json")
    public List<Map<String, Object>> getFlowsList() {
        IPollingService pollingService =
                (IPollingService)getContext().getAttributes().
                get(IPollingService.class.getCanonicalName());
        
        List<Map<String, Object>> teste = pollingService.queryFlowsList();
        return teste;
    }
	
	

}
