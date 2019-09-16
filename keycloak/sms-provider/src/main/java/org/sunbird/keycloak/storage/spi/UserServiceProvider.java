package org.sunbird.keycloak.storage.spi;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.sunbird.keycloak.utils.Constants;

public class UserServiceProvider
    implements UserStorageProvider, UserLookupProvider, UserQueryProvider, UserRegistrationProvider {
  private static final Logger logger = Logger.getLogger(UserStorageProvider.class);

  public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";
  private final KeycloakSession session;
  private final ComponentModel model;
  private final UserService userService;

  public UserServiceProvider(KeycloakSession session, ComponentModel model,
      UserService userService) {
    this.session = session;
    this.model = model;
    this.userService = userService;
  }

  protected UserProvider localStorage() {
    return session.userLocalStorage();
  }

  @Override
  public void close() {}

  @Override
  public UserModel getUserById(String id, RealmModel realm) {
    logger.info("UserServiceProvider:getUserById: id = " + id);
    String externalId = StorageId.externalId(id);
    logger.info("UserServiceProvider:getUserById: externalId found = " + externalId);
    return new UserAdapter(session, realm, model, userService.getById(externalId));
  }

  @Override
  public UserModel getUserByUsername(String username, RealmModel realm) {
    logger.info("UserServiceProvider: getUserByUsername called");
    List<User> users = userService.getByUsername(username);
    if (users != null && users.size() == 1) {
      return new UserAdapter(session, realm, model, users.get(0));
    } else if (users != null && users.size() > 1) {
      throw new ModelDuplicateException(
          "Multiple users are associated with this login credentials.", "login credentials");
    } else {
      return null;
    }
  }

  @Override
  public UserModel getUserByEmail(String email, RealmModel realm) {
    logger.info("UserServiceProvider: getUserByEmail called");
    return getUserByUsername(email, realm);
  }

  @Override
  public int getUsersCount(RealmModel realm) {
    return 0;
  }

  @Override
  public List<UserModel> getUsers(RealmModel realm) {
    return Collections.emptyList();
  }

  @Override
  public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
    return Collections.emptyList();
  }

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realm) {
    logger.info("UserServiceProvider: searchForUser called");
    return userService.getByUsername(search).stream()
        .map(user -> new UserAdapter(session, realm, model, user)).collect(Collectors.toList());
  }

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult,
      int maxResults) {
    logger.info("UserServiceProvider: searchForUser called with firstResult = " + firstResult);
    return searchForUser(search, realm);
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
    return Collections.emptyList();
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm,
      int firstResult, int maxResults) {

    return Collections.emptyList();
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult,
      int maxResults) {

    return Collections.emptyList();
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {

    return Collections.emptyList();
  }

  @Override
  public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue,
      RealmModel realm) {
    logger.info("UserServiceProvider: searchForUserByUserAttribute called");
    if (Constants.PHONE.equalsIgnoreCase(attrName)) {
      return userService.getByKey(attrName, attrValue).stream()
          .map(user -> new UserAdapter(session, realm, model, user)).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  /*@Override
  public UserModel addUser(RealmModel realm, String username) {
    UserModel user = session.userStorage().addUser(realm, username.toLowerCase());
    return registerWithFederation(realm, user);
  }

  protected UserModel registerWithFederation(RealmModel realm, UserModel user) {
    for (UserFederationProviderModel federation : realm.getUserFederationProviders()) {
      UserFederationProvider fed = getFederationProvider(federation);
      if (fed.synchronizeRegistrations()) {
        user.setFederationLink(federation.getId());
        UserModel registered = fed.register(realm, user);
        managedUsers.put(registered.getId(), registered);
        return registered;
      }
    }
    return user;
  }*/


 /* @Override
  public UserModel addUser(RealmModel realm, String id, String username, boolean addDefaultRoles, boolean addDefaultRequiredActions) {
    UserModel user = getDelegate().addUser(realm, id, username, addDefaultRoles, addDefaultRoles);
    managedUsers.put(user.getId(), user);
    return user;
  }*/
  @Override
  public UserModel addUser(RealmModel realm, String username) {
    System.out.println(session.getAttribute("username"));
    System.out.println("Inside addUser:: username = "+username);
    System.out.println("Inside addUser:: calling UserSearchService.getUserByKey for username = "+username);
    List<User> userList = UserSearchService.getUserByKey(Constants.EMAIL,username);
    //userlist is empty means user doesnt exist in federated db
    if(null == userList ||(null != userList && userList.size() == 0)){
      localStorage().getUserByUsername("hgjhj",realm);
      System.out.println("Inside addUser:: No response got from UserSearchService.getUserByKey for username = "+userList.size());
      UserModel user = session.users().getUserByEmail(username, realm);
      System.out.println("Inside addUser:: response got from localStorage().getUserByUsername for username = "+user.getEmail());
      Map<String,Object> userMap = new HashMap<>();
      userMap.put("firstName",user.getFirstName());
      userMap.put("lastName",user.getLastName());
      userMap.put("email",user.getEmail());
      userMap.put("emailVerified",true);
      String userCreateUrl = System.getenv("sunbird_user_service_base_url")+"/v2/user/create";
      System.out.println("Inside addUser:: uri = "+userCreateUrl);
      HttpResponse response =
              HttpUtil.post2(userMap, userCreateUrl, System.getenv(Constants.SUNBIRD_LMS_AUTHORIZATION));
      if (null!= response && 200 == response.getStatusLine().getStatusCode()) {
        System.out.println("User creation success");
        //else dont do anything as user already exist
        //Once User added to federated db, delete from local storage
        //localStorage().removeUser(realm,user);
      }else {
        System.out.println("user creation failed");
        //else dont delete user from local storage
      }
    } else {
      System.out.println("Inside addUser:: response got from UserSearchService.getUserByKey for username = "+userList.size());
    }

    return null;
  }

  @Override
  public boolean removeUser(RealmModel realm, UserModel user) {
    return false;
  }
}
