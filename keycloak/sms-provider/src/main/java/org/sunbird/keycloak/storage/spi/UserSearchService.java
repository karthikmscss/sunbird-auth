package org.sunbird.keycloak.storage.spi;

import org.jboss.logging.Logger;
import org.sunbird.keycloak.utils.Constants;

import java.util.*;

public class UserSearchService {

  private static Logger logger = Logger.getLogger(UserSearchService.class);

  private UserSearchService() {}

  @SuppressWarnings({"unchecked"})
  public static List<User> getUserByKey(String key, String value) {
    Map<String, Object> userRequest = new HashMap<>();
    Map<String, Object> request = new HashMap<>();
    Map<String, String> filters = new HashMap<>();
    filters.put(key, value);
    request.put("filters", filters);
    request.put("fields", Arrays.asList("email","firstName","lastName","id","phone","userName","countryCode","status"));
    userRequest.put("request", request);
    String searchUrl = System.getenv("sunbird_user_service_base_url")+"/private/user/v1/search";
    Map<String, Object> resMap =
        HttpUtil.post(userRequest, searchUrl, System.getenv(Constants.SUNBIRD_LMS_AUTHORIZATION));
    logger.info("UserSearchService:getUserByKey responseMap "+resMap);
    Map<String, Object> result = null;
    Map<String, Object> responseMap = null;
    List<Map<String, Object>> content = null;
    if (null != resMap) {
      result = (Map<String, Object>) resMap.get("result");
    }
    if (null != result) {
      responseMap = (Map<String, Object>) result.get("response");
    }
    if (null != responseMap) {
      content = (List<Map<String, Object>>) (responseMap).get("content");
    }
    if (null != content) {
      List<User> userList = new ArrayList<>();
      if (!content.isEmpty()) {
        logger.info("usermap is not null from ES");
        content.forEach(userMap -> {
          if (null != userMap) {
            userList.add(createUser(userMap));
          }
        });
      }
      return userList;
    }
    return Collections.emptyList();
  }

  private static User createUser(Map<String, Object> userMap) {
    User user = new User();
    user.setEmail((String) userMap.get(Constants.EMAIL));
    user.setFirstName((String) userMap.get("firstName"));
    user.setId((String) userMap.get(Constants.ID));
    user.setLastName((String) userMap.get("lastName"));
    user.setPhone((String) userMap.get(Constants.PHONE));
    user.setUsername((String) userMap.get("userName"));
    user.setCountryCode((String) userMap.get("countryCode"));
    if ( null != userMap.get("status") && ((Integer)userMap.get("status")) == 0) {
      user.setEnabled(false);
    } else {
      user.setEnabled(true);
    }
    return user;
  }
}
