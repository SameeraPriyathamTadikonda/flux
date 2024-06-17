/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.ParameterException;
import com.marklogic.flux.api.AuthenticationType;

import java.util.Map;

public class ConnectionParamsValidator implements IParametersValidator {

    private final ParamNames paramNames;

    public ConnectionParamsValidator() {
        this(false);
    }

    public ConnectionParamsValidator(boolean isOutput) {
        this.paramNames = new ParamNames(isOutput);
    }

    @Override
    public void validate(Map<String, Object> parameters) throws ParameterException {
        if (parameters.get(paramNames.connectionString) == null && parameters.get("--preview") == null) {
            if (parameters.get(paramNames.host) == null) {
                throw new ParameterException(String.format("Must specify a MarkLogic host via %s or %s.",
                    paramNames.host, paramNames.connectionString));
            }
            if (parameters.get(paramNames.port) == null) {
                throw new ParameterException(String.format("Must specify a MarkLogic app server port via %s or %s.",
                    paramNames.port, paramNames.connectionString));
            }

            AuthenticationType authType = (AuthenticationType) parameters.get(paramNames.authType);
            boolean isDigestOrBasicAuth = authType == null || (AuthenticationType.DIGEST.equals(authType) || AuthenticationType.BASIC.equals(authType));
            if (isDigestOrBasicAuth) {
                if (parameters.get(paramNames.username) == null) {
                    throw new ParameterException(String.format("Must specify a MarkLogic user via %s when using 'BASIC' or 'DIGEST' authentication.",
                        paramNames.username));
                }
                if (parameters.get(paramNames.password) == null) {
                    throw new ParameterException(String.format("Must specify a password via %s when using 'BASIC' or 'DIGEST' authentication.",
                        paramNames.password));
                }
            }
        }
    }

    private static class ParamNames {
        final String connectionString;
        final String host;
        final String port;
        final String authType;
        final String username;
        final String password;

        ParamNames(boolean isOutput) {
            connectionString = isOutput ? "--output-connection-string" : "--connection-string";
            host = isOutput ? "--output-host" : "--host";
            port = isOutput ? "--output-port" : "--port";
            authType = isOutput ? "--output-auth-type" : "--auth-type";
            username = isOutput ? "--output-username" : "--username";
            password = isOutput ? "--output-password" : "--password";
        }
    }
}
