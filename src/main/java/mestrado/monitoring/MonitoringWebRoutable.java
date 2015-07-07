package mestrado.monitoring;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import mestrado.monitoring.poll.resources.FlowTrafficResource;
import mestrado.monitoring.poll.resources.FlowsListResource;
import net.floodlightcontroller.restserver.RestletRoutable;

public class MonitoringWebRoutable implements RestletRoutable{

	
	@Override
	public Restlet getRestlet(Context context) {
		// TODO Auto-generated method stub
		Router router = new Router(context);
		router.attach("/polling/flowsList/json", FlowsListResource.class);
		router.attach("/polling/{flow-id}/flowTraffic/json", FlowTrafficResource.class);
		return router;
	}

	@Override
	public String basePath() {
		// TODO Auto-generated method stub
		 return "/wm/monitoring";
	}
	
	

}
