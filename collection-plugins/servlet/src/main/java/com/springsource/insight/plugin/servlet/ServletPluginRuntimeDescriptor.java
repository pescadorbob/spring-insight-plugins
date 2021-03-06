/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.servlet;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;
import com.springsource.insight.intercept.trace.TraceSourceAnalyzer;
import com.springsource.insight.util.ArrayUtil;

public class ServletPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "servlet";
    private static final ServletPluginRuntimeDescriptor	INSTANCE=new ServletPluginRuntimeDescriptor();
    private static final List<? extends EndPointAnalyzer>	epAnalyzers=
    		ArrayUtil.asUnmodifiableList(LifecycleEndPointAnalyzer.getInstance(),
             	   						 RequestDispatchEndPointAnalyzer.getInstance(),
             	   						 ServletEndPointAnalyzer.getInstance());
    private static final List<? extends TraceErrorAnalyzer>	errAnalyzers=
    		ArrayUtil.asUnmodifiableList(HttpStatusTraceErrorAnalyzer.getInstance());
    private static final List<? extends TraceSourceAnalyzer>	tsAnalyzers=
    		ArrayUtil.asUnmodifiableList(HttpTraceSourceAnalyzer.getInstance());
    private static final List<? extends MetricsGenerator>	mGenerators=
	    	ArrayUtil.asUnmodifiableList(RequestResponseSizeMetricsGenerator.getInstance());

    private ServletPluginRuntimeDescriptor () {
    	super();
    }

    public static final ServletPluginRuntimeDescriptor getInstance() {
    	return INSTANCE;
    }

    @Override
    public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
        return epAnalyzers;
    }

    @Override
    public Collection<? extends TraceErrorAnalyzer> getTraceErrorAnalyzers() {
        return errAnalyzers;
    }
    
    @Override
    public Collection<? extends TraceSourceAnalyzer> getTraceSourceAnalyzers() {
        return tsAnalyzers;
    }
    
    @Override
    public Collection<? extends MetricsGenerator> getMetricsGenerators() {
    	return mGenerators;
    }
    
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
