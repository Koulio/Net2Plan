package com.net2plan.examples.ocnbook.offline;

import com.google.common.collect.ImmutableMap;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.InputParameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class Offline_tca_nodeLocationTest 
{
	private NetPlan np;

	@Before
	public void setUp() throws Exception 
	{
		this.np = new NetPlan (new File ("src/test/resources/data/networkTopologies/example7nodes_withTraffic.n2p"));
	}

	@After
	public void tearDown() throws Exception 
	{
		np.checkCachesConsistency();
	}

	@Test
	public void test() 
	{
		final IAlgorithm algorithm = new Offline_tca_nodeLocation();
		Map<String,List<String>> testingParameters = new HashMap<> ();
		testingParameters.put("solverName" , Arrays.asList("cplex"));
		testingParameters.put("maxNumCoreNodesPerSite" , Arrays.asList("1" , "2"));
		testingParameters.put("K_max" , Arrays.asList("2" , "5"));
		List<Map<String,String>> testsParam = InputParameter.getCartesianProductOfParameters (testingParameters);
		if (testsParam.isEmpty()) testsParam = Arrays.asList(InputParameter.getDefaultParameters(algorithm.getParameters()));
		for (Map<String,String> params : testsParam)
		{
			Map<String,String> paramsUsedToCall = InputParameter.getDefaultParameters(algorithm.getParameters());
			paramsUsedToCall.putAll(params); // so default parameters that are also in param, are replaced
			final NetPlan npInput = np.copy ();
			algorithm.executeAlgorithm(np , paramsUsedToCall , ImmutableMap.of("precisionFactor" , "0.0001"));
			checkValidity (npInput , np , paramsUsedToCall);
		}
	}

	private void checkValidity (NetPlan npInput , NetPlan npOutput , Map<String,String> params)
	{
		
		for (Node n : npOutput.getNodes())
		{
			final int numOutAccessLinks = n.getOutgoingLinks().size();
			final int numIncomingAccessLinksPlusInternal = (numOutAccessLinks == 0? 1 : 0) + n.getIncomingLinks().size();
			final int numCoreNodes = (int) Math.ceil(((double) numIncomingAccessLinksPlusInternal) / Integer.parseInt(params.get("K_max")));
			assertTrue (numOutAccessLinks < 2);
			assertTrue (numCoreNodes <= Integer.parseInt(params.get("maxNumCoreNodesPerSite")));
		}
	}

}
