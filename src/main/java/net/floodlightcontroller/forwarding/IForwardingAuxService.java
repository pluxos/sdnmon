package net.floodlightcontroller.forwarding;

import java.util.List;

import net.floodlightcontroller.core.module.IFloodlightService;

import mestrado.monitoring.poll.MatchMon;

public interface IForwardingAuxService extends IFloodlightService { //Criado por mim!
	
	public List<MatchMon> getMatchMonList();
	
}
