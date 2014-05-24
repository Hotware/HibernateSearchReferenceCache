package de.hotware.hibernate.search.extension.reference.bytecode;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.logging.Logger;

public class ReferenceCacheAgent {
	
	private static final Logger LOGGER = Logger.getLogger(ReferenceCacheAgent.class.getName());

	public static void premain(String agentArguments, Instrumentation instrumentation) {
    	RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    	LOGGER.info("Runtime: " + runtimeMxBean.getName() + ", :"+ runtimeMxBean.getInputArguments());
        LOGGER.info("Starting agent with arguments " + agentArguments);
    	
    	// define the class transformer to use
        instrumentation.addTransformer(new ObjectLoaderTransformer());
    }
	
}
