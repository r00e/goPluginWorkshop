package com.tw.go.plugin.workshop;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

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
        return null;
    }

    private GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        return null;
    }

    private GoPluginApiResponse handleGetConfigRequest() {
        return null;
    }
}
