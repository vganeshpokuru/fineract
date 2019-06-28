/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.hooks.listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.hooks.processor.HookProcessor;
import org.apache.fineract.infrastructure.hooks.processor.HookProcessorProvider;
import org.apache.fineract.infrastructure.hooks.service.HookReadPlatformService;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.apache.fineract.template.service.TemplateMergeService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

@Service
public class FineractHookListener implements HookListener {

    private final HookProcessorProvider hookProcessorProvider;
    private final HookReadPlatformService hookReadPlatformService;
    private final TenantDetailsService tenantDetailsService;
    private final TemplateMergeService templateMergeService;
    private final DefaultToApiJsonSerializer<String> apiJsonSerializerService;

    @Autowired
    public FineractHookListener(final HookProcessorProvider hookProcessorProvider,
            final HookReadPlatformService hookReadPlatformService,
            final TenantDetailsService tenantDetailsService,
            final TemplateMergeService templateMergeService,
            final DefaultToApiJsonSerializer<String> apiJsonSerializerService) {
        this.hookReadPlatformService = hookReadPlatformService;
        this.hookProcessorProvider = hookProcessorProvider;
        this.tenantDetailsService = tenantDetailsService;
        this.templateMergeService = templateMergeService;
        this.apiJsonSerializerService = apiJsonSerializerService;
    }

    @Override
    public void onApplicationEvent(final HookEvent event) {

        final String tenantIdentifier = event.getTenantIdentifier();
        final FineractPlatformTenant tenant = this.tenantDetailsService
                .loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);

        final AppUser appUser = event.getAppUser();
        final String authToken = event.getAuthToken();

        final HookEventSource hookEventSource = event.getSource();
        final String entityName = hookEventSource.getEntityName();
        final String actionName = hookEventSource.getActionName();
        String payload = event.getPayload();

        final List<Hook> hooks = this.hookReadPlatformService
                .retrieveHooksByEvent(hookEventSource.getEntityName(),
                        hookEventSource.getActionName());

        for (final Hook hook : hooks) {
            final HookProcessor processor = this.hookProcessorProvider
                    .getProcessor(hook);
            if(hook.getUgdTemplate() != null) {
            	payload = processUgdTemplate(payload, hook, authToken);
            }
            processor.process(hook, appUser, payload, entityName, actionName,
                    tenantIdentifier, authToken);
        }
    }
    
    private String processUgdTemplate(final String payload,
            final Hook hook, final String authToken) {
        String json = "";
        try {
            @SuppressWarnings("unchecked")
            final HashMap<String, Object> map = new ObjectMapper().readValue(payload, HashMap.class);
            map.put("BASE_URI", System.getProperty("baseUrl"));
                this.templateMergeService.setAuthToken(authToken);
                final String compiledMessage = this.templateMergeService.compile(hook.getUgdTemplate(), map).replace("<p>", "")
                        .replace("</p>", "").replace("&quot;", "\"");
                final Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("payload", payload);
                jsonMap.put("UGDtemplate", new JsonParser().parse(compiledMessage).getAsJsonObject().toString());
                final String jsonString = new Gson().toJson(jsonMap);
                json = new JsonParser().parse(jsonString).getAsJsonObject().toString();
        } catch (IOException e) {}
        return this.apiJsonSerializerService.serialize(json).replace("\\", "").replace("\"{", "{").replace("}\"", "}");
    }

}
