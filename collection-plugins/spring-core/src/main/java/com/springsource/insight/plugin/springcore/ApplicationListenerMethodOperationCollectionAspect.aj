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

package com.springsource.insight.plugin.springcore;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.context.event.SourceFilteringListener;

import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;


public aspect ApplicationListenerMethodOperationCollectionAspect extends SpringEventReferenceCollectionAspect {
    public ApplicationListenerMethodOperationCollectionAspect() {
        super(SpringCorePluginRuntimeDescriptor.APP_LISTENER_TYPE);
    }

    public pointcut appListener()
        : execution(* ApplicationListener+.onApplicationEvent(..));

    public pointcut delegatingGenericListenerAdapter()
        : execution(* GenericApplicationListenerAdapter+.onApplicationEvent(..));

    public pointcut sourceFilteringListener()
        : execution(* SourceFilteringListener+.onApplicationEvent(..));

    public pointcut collectionPoint() : 
        appListener() && 
        !delegatingGenericListenerAdapter() && 
        !sourceFilteringListener() && 
        !AnnotationDrivenMethodOperationCollectionAspect.collectionPoint();
}

