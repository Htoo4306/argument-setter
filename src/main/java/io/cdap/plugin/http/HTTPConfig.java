/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.http;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.plugin.PluginConfig;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Common http plugin properties.
 */
public class HTTPConfig extends PluginConfig {

  @Description("The URL to fetch data from.")
  @Macro
  private String url;

  @Description("Request headers to set when performing the http request.")
  @Nullable
  private String requestHeaders;

  @Description("Whether to automatically follow redirects. Defaults to true.")
  @Nullable
  private Boolean followRedirects;

  @Description("Sets the connection timeout in milliseconds. Set to 0 for infinite. Default is 60000 (1 minute).")
  @Nullable
  private Integer connectTimeout;

  @Description("Sets the read timeout in milliseconds. Set to 0 for infinite. Default is 60000 (1 minute).")
  @Nullable
  private Integer readTimeout;

  @Description("The http request method. Defaults to GET.")
  @Macro
  private String method;

  @Nullable
  @Description("The http request body.")
  @Macro
  private String body;

  @Nullable
  @Description("The number of times the request should be retried if the request fails. Defaults to 0.")
  @Macro
  private Integer numRetries;

  public HTTPConfig() {
    method = io.cdap.common.http.HttpMethod.GET.name();
    numRetries = 0;
    followRedirects = true;
    connectTimeout = 60000;
    readTimeout = 60000;
  }

  @SuppressWarnings("ConstantConditions")
  void validate() {
    if (url != null) {
      try {
        new URL(url);
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(String.format("URL '%s' is malformed: %s", url, e.getMessage()), e);
      }
    }
    if (connectTimeout < 0) {
      throw new IllegalArgumentException(String.format(
        "Invalid connectTimeout %d. Timeout must be 0 or a positive number.", connectTimeout));
    }

    if (!containsMacro("method")) {
      try {
        io.cdap.common.http.HttpMethod.valueOf(method.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Invalid request method %s, must be one of %s.",
                                                         method,
                                                         Joiner.on(',').join(io.cdap.common.http.HttpMethod.values())));
      }
    }
    if (!containsMacro("numRetries") && numRetries < 0) {
      throw new IllegalArgumentException(String.format(
        "Invalid numRetries %d. Retries cannot be a negative number.", numRetries));
    }

    getHeaders();
  }



  public String getUrl() {
    return url;
  }

  @Nullable
  public String getRequestHeaders() {
    return requestHeaders;
  }

  @Nullable
  public Boolean getFollowRedirects() {
    return followRedirects;
  }

  @Nullable
  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  @Nullable
  public Integer getReadTimeout() {
    return readTimeout;
  }

  public String getMethod() {
    return method;
  }

  @Nullable
  public String getBody() {
    return body;
  }

  @Nullable
  public Integer getNumRetries() {
    return numRetries;
  }

  Map<String, String> getHeaders() {
    try {
      return requestHeaders == null ? Collections.<String, String>emptyMap() : parseMap(requestHeaders);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not parse headers: " + e.getMessage(), e);
    }
  }

  protected Map<String, String> parseMap(String val) {
    try {
      return Splitter.on(";").trimResults().withKeyValueSeparator(":").split(val);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not parse headers: " + e.getMessage(), e);
    }
  }
}
