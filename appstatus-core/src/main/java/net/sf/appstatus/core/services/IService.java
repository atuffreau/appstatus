/*
 * Copyright 2010 Capgemini Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sf.appstatus.core.services;

/**
 * A service with associated metrics.
 *
 * @author Nicolas Richeton
 *
 */
public interface IService extends Comparable<IService> {

	double getAvgNestedCalls();

	double getAvgNestedCallsWithCache();

	Double getAvgResponseTime();

	Double getAvgResponseTimeWithCache();

	long getCacheHits();

	double getCurrentRate();

	long getErrors();

	long getFailures();

	String getGroup();

	long getHits();

	Long getMaxResponseTime();

	Long getMaxResponseTimeWithCache();

	Long getMinResponseTime();

	Long getMinResponseTimeWithCache();

	String getName();

	long getRunning();

}
