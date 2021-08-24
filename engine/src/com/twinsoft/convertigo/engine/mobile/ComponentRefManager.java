package com.twinsoft.convertigo.engine.mobile;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.engine.DatabaseObjectImportedEvent;
import com.twinsoft.convertigo.engine.DatabaseObjectListener;
import com.twinsoft.convertigo.engine.DatabaseObjectLoadedEvent;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.util.GenericUtils;

public class ComponentRefManager implements DatabaseObjectListener {

	static private ComponentRefManager cm = new ComponentRefManager();
	
	static public ComponentRefManager get() {
		return cm;
	}
	
	private Map<String, Set<String>> consumers = new HashMap<String, Set<String>>();
	
	private ComponentRefManager() {
		
	}

	public void addConsumer(String qname, String pname) {
		if (qname.startsWith(pname+"."))
			return;
		synchronized (consumers) {
			if (consumers.get(qname) == null) {
				consumers.put(qname, new HashSet<String>());
			}
			if (consumers.get(qname).add(pname)) {
				Engine.logEngine.trace(pname + " has been added as consumer for comp: "+ qname + ", consummers:"+consumers.get(qname).size());
			}
		}
	}
	
	public void removeConsumer(String qname, String pname) {
		synchronized (consumers) {
			if (consumers.get(qname) != null) {
				consumers.get(qname).remove(pname);
			}
		}
	}
	
	public Set<String> getConsumers(String qname) {
		synchronized (consumers) {
			if (consumers.get(qname) != null) {
				return Collections.unmodifiableSet(consumers.get(qname));
			}
			return Collections.emptySet();
		}
	}

	public Set<String> getKeys() {
		synchronized (consumers) {
			return Collections.unmodifiableSet(consumers.keySet());
		}
	}
	
	@Override
	public void databaseObjectLoaded(DatabaseObjectLoadedEvent event) {
		
	}

	@Override
	public void databaseObjectImported(DatabaseObjectImportedEvent event) {
		DatabaseObject dbo = (DatabaseObject)event.getSource();
		
		if (dbo instanceof com.twinsoft.convertigo.beans.ngx.components.UIUseShared) {
			com.twinsoft.convertigo.beans.ngx.components.UIUseShared uius = GenericUtils.cast(dbo);
			String pname = uius.getProject().getName();
			String qname = uius.getSharedComponentQName();
			addConsumer(qname, pname);
		}
	}
	
}
