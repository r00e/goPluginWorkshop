package com.tw.go.plugin.workshop;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.IOUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

@Extension
public class goPluginWorkshop implements GoPlugin{
    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        //ignore
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", asList("1.0"));
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        if("configuration".equals(request.requestName())){
            return handleGetConfigRequest();
        } else if("validate".equals(request.requestName())){
            return handleValidation(request);
        } else if("view".equals(request.requestName())){
            return handleView();
        } else if("execute".equals(request.requestName())){
            return handleExecution(request);
        }

        throw new UnhandledRequestTypeException(request.requestName());
    }

    private GoPluginApiResponse handleExecution(GoPluginApiRequest request) {
        return null;
    }

    private GoPluginApiResponse handleView() {
        HashMap view = new HashMap();
        view.put("displayValue", "plugin workshop script");
        try{
            view.put("template", IOUtils.toString(getClass().getResourceAsStream("/view/task.html"), "UTF-8"));
        } catch(Exception e){
            String errorMsg = "Failed to find template: " + e.getMessage();
            view.put("exception", errorMsg);
            return createResponse(500, view);
        }
        return createResponse(200, view);
    }

    private GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        Map requestBody = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);

        if(!requestBody.containsKey("script") ||
                ((Map)requestBody.get("script")).get("value") == null ||
                ((String)((Map)requestBody.get("script")).get("value")).trim().isEmpty()){
            HashMap error = new HashMap();
            error.put("script", "Script cannot be empty, please write your script...");
            response.put("errors", error);
        }

        return createResponse(200, response);
    }

    private GoPluginApiResponse handleGetConfigRequest() {
        final HashMap<String, Object> response = new HashMap<String, Object>();
        HashMap<String, Object> fieldProperty = new HashMap<String, Object>();
        fieldProperty.put("default-value", null);
        fieldProperty.put("required", true);

        response.put("script", fieldProperty);

        return createResponse(200, response);
    }

    private GoPluginApiResponse createResponse(final int code, final HashMap<String, Object> response) {
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return code;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return response == null ? null : new GsonBuilder().create().toJson(response);
            }
        };
    }
}
