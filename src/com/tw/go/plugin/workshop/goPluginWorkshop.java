package com.tw.go.plugin.workshop;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        HashMap<String, Object> response = new HashMap<String, Object>();
        String workingDirectory, scriptFileName;
        Boolean isWindows = isWindows();

        try{
            Map<String, Object> body = (Map<String, Object>) new GsonBuilder().create().fromJson(request.requestBody
                    (), Object.class);
            Map<String, Object> config = (Map<String, Object>) body.get("config");
            Map<String, String> script = (Map<String, String>) config.get("script");
            String scriptValue = script.get("value");

            Map<String, Object> context = (Map<String, Object>) body.get("context");
            Map<String, String> environmentVariables = (Map<String, String>) context.get("environmentVariables");
            workingDirectory = (String) context.get("workingDirectory");
            scriptFileName = generateScriptFileName(isWindows);

            createScript(workingDirectory, scriptFileName, isWindows, scriptValue);

            int exitCode = executeScript(workingDirectory, scriptFileName, isWindows, environmentVariables);

            if(exitCode == 0){
                response.put("success", true);
                response.put("message", "[pulgin workshop] Script executed successfully.");
            }else{
                response.put("success", false);
                response.put("message", "[pulgin workshop] Script exited with exit code: " + exitCode);
            }
        }catch (Exception e){
            response.put("success", false);
            response.put("message", "[pulgin workshop] Error: " + e.getMessage());
        }

        return createResponse(200, response);
    }

    private void createScript(String workingDirectory, String scriptFileName, Boolean isWindows, String scriptValue) throws IOException, InterruptedException {
        File file = new File(workingDirectory + "/" + scriptFileName);
        scriptValue = scriptValue.replaceAll("(\\r\\n|\\r|\\n)", System.getProperty("line.separator"));
        FileUtils.writeStringToFile(file, scriptValue);

        if(!isWindows){
            executeCommand(workingDirectory, null, "chmod", "u+x", scriptFileName);
        }

        JobConsoleLogger.getConsoleLogger().printLine("[plugin workshop] Script written into '" + file.getAbsolutePath
                () + "'.");
    }

    private int executeScript(String workingDirectory, String scriptFileName, Boolean isWindows, Map<String, String> environmentVariables) throws IOException, InterruptedException {
        if (isWindows) {
            return executeCommand(workingDirectory, environmentVariables, "cmd", "/c", scriptFileName);
        }
        return executeCommand(workingDirectory, environmentVariables, "/bin/sh", "-c", "./" + scriptFileName);
    }

    private int executeCommand(String workingDirectory, Map<String, String> environmentVariables, String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(workingDirectory));
        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            processBuilder.environment().putAll(environmentVariables);
        }
        Process process = processBuilder.start();

        JobConsoleLogger.getConsoleLogger().readOutputOf(process.getInputStream());
        JobConsoleLogger.getConsoleLogger().readErrorOf(process.getErrorStream());

        return process.waitFor();
    }

    private String generateScriptFileName(Boolean isWindows) {
        return UUID.randomUUID() + (isWindows ? ".bat" : ".sh");
    }

    private Boolean isWindows() {
        String osName = System.getProperty("os.name");
        boolean isWindows = osName.toLowerCase().contains("windows");
        JobConsoleLogger.getConsoleLogger().printLine("[plugin workshop] OS detected: '" + osName + "'. Is windows? "
                                                              + isWindows);
        return isWindows;
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
