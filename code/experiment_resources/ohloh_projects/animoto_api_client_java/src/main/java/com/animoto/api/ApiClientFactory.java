package com.animoto.api;

import java.util.ResourceBundle;

/**
 * Factory to create ApiClient objects with key, secret, and host read from a properties file, animoto_api_client.properties.<p/>
 *
 * As per all resource bundles, the bundle must be located in your application CLASS_PATH.<p/>
 *
 * There is an example copy inside of src/test/resources for use with the ApiClientIntegrationTest.<p/>
 *
 * The configurations are:<p/>
 * <ul>
 *  <li>api.key - The credential key provided by Animoto.</li>
 *  <li>api.secret - The credential secret provided by Animoto.</li>
 *  <li>api.host - The environment host you want the client to communicate with.</li>
 * </ul>
 */
public class ApiClientFactory {

  public static ApiClient newInstance() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("animoto_api_client");
    ApiClient apiClient = new ApiClient();
    apiClient.setKey(resourceBundle.getString("api.key").trim());
    apiClient.setSecret(resourceBundle.getString("api.secret").trim());
    apiClient.setHost(resourceBundle.getString("api.host").trim()); 
    return apiClient;
  }
}
