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
package org.apache.fineract.sms;

import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import com.eukaprotech.asyncconnection.AsyncConnection;
import com.eukaprotech.asyncconnection.AsyncConnectionHandler;
import com.eukaprotech.asyncconnection.Parameters;

@Service(value="SmsAfrica")
public class SmsAfricaGateway

{

	private String api_key;
    private String account_number;
    private String sender_id;
    
    public SmsAfricaGateway(String account_number, String api_key){
        this.account_number = account_number;
        this.api_key = api_key;
    }
    
    public SmsAfricaGateway(String account_number, String api_key, String sender_id){
        this.account_number = account_number;
        this.api_key = api_key;
        this.sender_id = sender_id;
    }
    public interface Listener{
        void onStart();
        void onSucceed(HashMap<String, String> status_map, HashMap<String, String> message_map);
        void onFail(int responseCode, String response);
        void onComplete();
    }
    public void postMessage(final String recipient, final String message, final Listener listener){
        AsyncConnection asyncConnection = new AsyncConnection();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("apikey", api_key);
        Parameters parameters = new Parameters();
        parameters.put("apikey", api_key);
        parameters.put("account_number", account_number);
        parameters.put("to", recipient);
        parameters.put("message",message);
        if(sender_id != null && !sender_id.isEmpty()){
            parameters.put("from",sender_id);
        }
        
        asyncConnection.post("https://smsafrica.tech/api/sms", headers, parameters, new AsyncConnectionHandler() {

                @Override
            public void onStart() {
                if(listener != null) {
                        listener.onStart();
                }
            }

            @Override
            public void onSucceed(int responseCode, HashMap<String, String> headers, byte[] response) {
                try {
                        JSONParser parser = new JSONParser();
                                        JSONObject json_object = (JSONObject) parser.parse(new String(response));
                                        JSONObject status_json_object = (JSONObject) json_object.get("status");
                                        JSONArray message_json_array = null;
                                        JSONObject message_json_object = null;
                                        try {
                                                message_json_array = (JSONArray) json_object.get("message");
                                                message_json_object = (JSONObject) message_json_array.get(0);
                                        }catch(Exception ex) {
                                                message_json_object = (JSONObject) json_object.get("message");
                                        }
                                        
                                        HashMap<String, String> status_map = new HashMap<String, String>();
                                        HashMap<String, String> message_map = new HashMap<String, String>();
                                        
                                        for(Iterator<?> iterator = status_json_object.keySet().iterator(); iterator.hasNext();) {
                                            String key = String.valueOf(iterator.next());
                                            String value = String.valueOf(status_json_object.get(key));
                                            status_map.put(key, value);
                                        }
                                        
                                        for(Iterator<?> iterator = message_json_object.keySet().iterator(); iterator.hasNext();) {
                                            String key = String.valueOf(iterator.next());
                                            String value = String.valueOf(message_json_object.get(key));
                                            message_map.put(key, value);
                                        }
                                        
                                        if(listener != null) {
                                listener.onSucceed(status_map, message_map);
                        }
                                } catch (Exception e) {
                                        if(listener != null) {
                                listener.onFail(responseCode, e.toString()+new String(response));
                        }
                                }
            }

            @Override
            public void onFail(int responseCode, HashMap<String, String> headers, byte[] response, Exception error) {
                if(listener != null) {
                        listener.onFail(responseCode, new String(response));
                }
            }

            @Override
            public void onComplete() {
                if(listener != null) {
                        listener.onComplete();
                }
            }
                
        });
    }
    public void sendMessage(String message, String accountId, String authToken, String providerName, String mobileNo) throws MessageGatewayException {
        SmsAfricaGateway sms = new SmsAfricaGateway(accountId, authToken, providerName);
        sms.postMessage(mobileNo, message, null);
    }
 
    public SmsAfricaGateway() {
    }

}
