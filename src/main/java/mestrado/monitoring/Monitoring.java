package mestrado.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import mestrado.monitoring.database.RetrieveRow;
import mestrado.monitoring.poll.Polling;
import mestrado.monitoring.poll.IPollingService;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.forwarding.IForwardingAuxService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;

public class Monitoring implements IFloodlightModule, IOFMessageListener, IPollingService{
	
	protected Polling polling;
	protected IRestApiService restApiService;
	protected IFloodlightProviderService floodlightProviderService;
	protected IOFSwitchService switchService;
	protected IForwardingAuxService auxFService; 
	protected RetrieveRow rr = new RetrieveRow();
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "monitoring";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD); //Pegar o Pacote enviado pelo Switch.
		polling.main(switchService.getAllSwitchMap(), eth, auxFService);
		return Command.CONTINUE;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IPollingService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		Map<Class<? extends IFloodlightService>, 
		IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(IPollingService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IRestApiService.class);
		l.add(IOFSwitchService.class);
		l.add(IForwardingAuxService.class);
		return null;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		polling = new Polling();
		restApiService = context.getServiceImpl(IRestApiService.class);
		floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		auxFService = context.getServiceImpl(IForwardingAuxService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
		restApiService.addRestletRoutable(new MonitoringWebRoutable());
	}

	/*************POLLING*************/
	@Override
	public List<Map<String, Object>> queryFlowsList() {
		// TODO Auto-generated method stub
		return polling.queryFlowsList();
	}

	@Override
	public Map<String, List<Map<String, Object>>> queryFlowTraffic(Integer flowID) {
		// TODO Auto-generated method stub
		return polling.queryFlowTraffic(flowID);
	}
	/***********************************/
}
