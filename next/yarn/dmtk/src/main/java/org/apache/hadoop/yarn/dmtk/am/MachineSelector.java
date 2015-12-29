package org.apache.hadoop.yarn.dmtk.am;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Resource;

public class MachineSelector {
	public ArrayList<String> GetAdjecentMachines(YarnClient amRMClient, int memory, int core)
	{
		ArrayList<String> ret = new ArrayList<String>();
		try
		{
			Map<String, List<String>> mp = getRacksAndNodes(amRMClient, memory, core);
			for (Map.Entry<String, List<String>> entry : mp.entrySet()) {  
				for (String node : entry.getValue())
					ret.add(node);
			}  
		}
		catch(Exception e)
		{
			LOG.error("Failed to GetAdjecentMachines " + e);
		}
		
		
		return ret;
	}
	

	public Map<String, List<String>> getRacksAndNodes(YarnClient amRMClient, int memory, int core) throws Exception{
        TreeMap<String, List<String>> racksAndNodes = new TreeMap<String, List<String>>(new Comparator<String>(){
        	public int compare(String o1, String o2){
        		return o2.compareTo(o1);
        	}
        });
        try {
            //YarnClient amRMClient = YarnClient.createYarnClient();
            //amRMClient.start();
            List<NodeReport> nodeReports = amRMClient.getNodeReports(NodeState.RUNNING);
            for (NodeReport report: nodeReports) {
                String rack = report.getRackName();
                if (!racksAndNodes.containsKey(rack)) {
                    racksAndNodes.put(rack, new ArrayList<String>());
                }
                Resource total = report.getCapability();
                Resource used = report.getUsed();
                int memoryLeft = total.getMemory() - used.getMemory();
                int coresLeft = total.getVirtualCores() - used.getVirtualCores();
                if (memoryLeft >= memory && coresLeft >= core) {
                    ArrayList<String> hostList = (ArrayList) racksAndNodes.get(rack);
                    String host = report.getNodeId().getHost();
                    hostList.add(host);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return racksAndNodes;
    }
	
	private static final Log LOG = LogFactory.getLog(MachineSelector.class);
	private SortedMap<String, String> machineLists_ = new TreeMap<String, String>();
	private YarnClient yarnClient_;
};
