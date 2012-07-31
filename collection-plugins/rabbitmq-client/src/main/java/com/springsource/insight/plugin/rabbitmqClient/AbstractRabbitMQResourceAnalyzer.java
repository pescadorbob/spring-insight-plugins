/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.rabbitmqClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.StringUtil;

public abstract class AbstractRabbitMQResourceAnalyzer extends AbstractSingleTypeEndpointAnalyzer implements ExternalResourceAnalyzer {
	public static final String RABBIT = "RabbitMQ";	
	/**
	 * Placeholder string used if no exchange name specified
	 */
	public static final String NO_EXCHANGE = "DefaultExchange";

	/**
	 * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
	 * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
	 * to let other endpoints &quot;beat&quot; this one
	 */
	public static final int	DEFAULT_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE;

	private static final String UNNAMED_TEMP_QUEUE_KEY_PREFIX = "amq.gen-";
	private static final String UNNAMED_TEMP_QUEUE_LABEL = "temporaryQueue";
	private static final String UNNAMED_RPC_QUEUE_KEY_PREFIX = "amqp.gen-";
	private static final String UNNAMED_RPC_QUEUE_LABEL = "temporaryQueue(RPC)";

	private final RabbitPluginOperationType operationType;
	private final boolean isIncoming;

	protected AbstractRabbitMQResourceAnalyzer(RabbitPluginOperationType type, boolean incoming) {
		super(type.getOperationType());
		this.operationType = type;
		this.isIncoming = incoming;
	}

	public final boolean isIncomingResource () {
		return isIncoming;
	}

	public final RabbitPluginOperationType getRabbitPluginOperationType () {
		return operationType;
	}

	@Override
	protected int getDefaultScore(int depth) {
		return DEFAULT_SCORE;
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
		Operation op = frame.getOperation();
		String label = buildLabel(op);
		String endPointLabel = RABBIT + "-" + label;
		String example = getExample(label);
		EndPointName endPointName = getName(label);

		return new EndPointAnalysis(endPointName, endPointLabel, example, getOperationScore(op, depth), op);
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		Collection<Frame> queueFrames = trace.getLastFramesOfType(operationType.getOperationType());
		if ((queueFrames == null) || queueFrames.isEmpty()) {
			return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> queueDescriptors = new ArrayList<ExternalResourceDescriptor>(queueFrames.size());
		ColorManager					 colorManager=ColorManager.getInstance();
		for (Frame queueFrame : queueFrames) {
			Operation op = queueFrame.getOperation();
			String label = buildLabel(op);
			String host = op.get("host", String.class);            
			Integer portProperty = op.get("port", Integer.class);
			int port = portProperty == null ? -1 : portProperty.intValue();
			String color = colorManager.getColor(op);			

			ExternalResourceDescriptor descriptor =
					new ExternalResourceDescriptor(queueFrame,
							buildResourceName(label, host, port),
							buildResourceLabel(label),
							ExternalResourceType.QUEUE.name(),
							RABBIT,
							host,
							port,
							color, isIncoming);
			queueDescriptors.add(descriptor);            
		}

		return queueDescriptors;
	}

	protected abstract String getExchange(Operation op);

	protected abstract String getRoutingKey(Operation op);

	static String buildResourceName (String label, String host, int port) {
		return buildResourceHash(MD5NameGenerator.getName(createExternalResourceName(label, host, port)));
	}

	static String buildResourceHash (String hashString) {
		return RABBIT + ":" + hashString;
	}

	static String buildResourceLabel (String label) {
		return RABBIT + "-" + label;
	}

	protected EndPointName getName(String label) {
		return EndPointName.valueOf(label);
	}

	protected String getExample(String label) {
		return buildDefaultExample(operationType,  label);
	}

	static String buildDefaultExample (RabbitPluginOperationType type, String label) {
		return type.getEndPointPrefix() + label;
	}

	protected String buildLabel(Operation op) {
		return buildLabel(getExchange(op), getRoutingKey(op));      
	}

	protected String buildLabel(String exchange, String routingKey) {
		return buildDefaultLabel(exchange, routingKey);
	}

	static final String buildDefaultLabel (String exchange, String routingKey) {
		boolean hasExchange = !isTrimEmpty(exchange);
		boolean hasRoutingKey=!isTrimEmpty(routingKey);

		if (!hasExchange) {
			exchange = NO_EXCHANGE;
		}

		if (hasRoutingKey) {
			if (routingKey.startsWith(UNNAMED_TEMP_QUEUE_KEY_PREFIX)){
				routingKey = UNNAMED_TEMP_QUEUE_LABEL;
			}
			if (routingKey.startsWith(UNNAMED_RPC_QUEUE_KEY_PREFIX)){			
				routingKey = UNNAMED_RPC_QUEUE_LABEL;
			}
		}

		StringBuilder sb = new StringBuilder(StringUtil.getSafeLength(exchange)
				+ StringUtil.getSafeLength(routingKey)
				+ 24 /* extra text */);		
			sb.append("Exchange#").append(exchange);
		
		if (hasRoutingKey) {				
			sb.append(' ').append("RoutingKey#").append(routingKey);
		}

		return sb.toString();
	}

	static String createExternalResourceName (String label, String host, int port) {
		return label + host + port;
	}

	private static boolean isTrimEmpty(String str){
		return (str == null) || (str.trim().length() <= 0);
	}

	@Override
	public String toString() {
		return getRabbitPluginOperationType().name() + "[incoming=" + isIncomingResource() + "]";
	}

}
