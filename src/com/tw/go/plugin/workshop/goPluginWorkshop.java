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
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) throws UnhandledRequestTypeException {
        return null;
    }
}
