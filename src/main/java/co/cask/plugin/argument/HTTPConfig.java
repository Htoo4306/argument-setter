package co.cask.plugin.argument;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.api.plugin.PluginConfig;
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
    method = co.cask.common.http.HttpMethod.GET.name();
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
        co.cask.common.http.HttpMethod.valueOf(method.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Invalid request method %s, must be one of %s.",
                                                         method,
                                                         Joiner.on(',').join(co.cask.common.http.HttpMethod.values())));
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
