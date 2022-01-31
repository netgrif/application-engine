# Kerberos + AD integration

## Prerequisites for implementation

Information required for implementation and application configuration:

* **Domain name** under which the application will be registered
* **Service Principal Name** e.g. HTTP/test.domain.com@DOMAIN.COM
* **Keytab file** created for the Service principal (contains ticket-granting ticket for service principal)
* **AD address** for requesting user data after successful authentication, via LDAP protocol
* **LDAP Search base** \- directory in AD structure where the user content will be searched
* **AD user group** if desired to filter out users based on group membership

The application frontend must be accessed via the domain in the Service principal name by the end user (HTTP/**
test.domain.com**@DOMAIN.COM), or else autologin will not work.

Service principal user must have sufficient encryption enabled in AD (from experience DES is not sufficient).

Another requirement for the SSO to work correctly is that the DNS record must be of type A.

## Tools for testing

It is useful to install the following packages on an environment from which AD can be requested, such as the application
server:

* _Cyrus-sasl-gssapi_ – for AD connection using Kerberos protocol from a Linux server
* _krb5-workstation_ – for connection testing
* _openldap-clients_ – for connection testing and user querying

After the installation of _krb5-workstation_ it is necessary to configure _/etc/krb5.conf_
(_[https://web.mit.edu/kerberos/krb5-1.12/doc/admin/conf\_files/krb5\_conf.html](https://web.mit.edu/kerberos/krb5-1.12/doc/admin/conf_files/krb5_conf.html)_)

[Example file](../_media/integration/krb5.example.conf)

Afterwards the keytab file and connection can be tested:

- `klist -k {keytab file}` - displays the service principal name in the keytab file. Displayed principal should be
  case-sensitive equal to Service principal name
- `kinit -kt {keytab file} {service principal name}` - loads the credentials from the keytab, no output is shown if
  successful
- `klist` _\-_ displays the credentials cache, after a successful kinit command the credentials for service principal
  will be found here

Subsequently, it is possible to call `ldapsearch` without explicitly providing the credentials (credentials cache is
used) - this is achieved using -Y GSSAPI.

`ldapsearch -v -h [AD server] -Y GSSAPI -b "[search base]" "[filter]"`

## Maven dependencies

Maven dependencies required for implementation

```xml

<dependencies>
  <dependency>
    <groupId>org.springframework.ldap</groupId>
    <artifactId>spring-ldap-core</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-ldap</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.security.kerberos</groupId>
    <artifactId>spring-security-kerberos-web</artifactId>
    <version>1.0.1.RELEASE</version>
  </dependency>

  <dependency>
    <groupId>org.springframework.security.kerberos</groupId>
    <artifactId>spring-security-kerberos-client</artifactId>
    <version>1.0.1.RELEASE</version>
  </dependency>
</dependencies>
```

## Application-level configuration

It is required to add the following configuration to the application - `GlobalSunJaasKerberosConfig` and provide the
path to the _krb5.conf_ file.

```java

@Configuration
public class KerberosGlobalConfig {
    public static final Logger log = LoggerFactory.getLogger(KerberosGlobalConfig.class);

    @Value("${application.specific.kerberos.conf}")
    private String kerberosGlobalConfPath;

    @Value("${application.specific.kerberos.debug}")
    private boolean debug;

    @Bean
    public GlobalSunJaasKerberosConfig globalSunJaasKerberosConfig() throws Exception {
        GlobalSunJaasKerberosConfig globalSunJaasKerberosConfig = new GlobalSunJaasKerberosConfig();
        globalSunJaasKerberosConfig.setDebug(this.debug);
        globalSunJaasKerberosConfig.setKrbConfLocation(this.kerberosGlobalConfPath);
        log.info("Setting kerberos global config with file " + kerberosGlobalConfPath + ", file exists: " + new FileSystemResource(kerberosGlobalConfPath).exists());
        globalSunJaasKerberosConfig.afterPropertiesSet();
        return globalSunJaasKerberosConfig;
    }
}
```

Next up it is necessary to override the default engine security configuration and extend it with the following settings
and Bean declarations, and also provide configuration properties such as `adServer` (AD server address), `keyTab` (
keytab file path), `servicePrincipal` (service principal name for which the keytab is issued), `debug` (enable debug
logging - boolean), `ldapSearchBase` (e.g. _CN=Users,DC=domain,DC=com_), `ldapSearchFilter` (filter to find a specific
user using a parameter), and optionally properties like `group` and `onlyGroupMembers` if it is desired to only allow
users to authenticate if they belong to a specific user group.

Several classes need to be implemented, such as `ApplicationSpecificFilterBasedLdapUserSearch`
, `ApplicationSpecificLdapUserDetailsService`, `ApplicationSpecificLdapUserDetailsMapper`, see below.

Security configuration override:

```java

@Configuration
@Controller
@EnableWebSecurity
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
@ConditionalOnExpression("${application.specific.kerberos.enabled}")
public class KerberosSecurityConfiguration extends com.netgrif.application.engine.configuration.SecurityConfiguration {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuration with frontend separated");
        http
                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .cors()
                .and()
                .addFilterBefore(createPublicAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(getPatterns()).permitAll()
                .antMatchers(OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .and()
                .headers()
                .frameOptions().disable()
                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000)
                .and()
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy", "frame-src: 'none'"))
                .and()
                .addFilterBefore(
                        spnegoAuthenticationProcessingFilter(),
                        BasicAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(spnegoEntryPoint());
        setCsrf(http);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(kerberosAuthenticationProvider())
                .authenticationProvider(kerberosServiceAuthenticationProvider());
    }

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        return new SpnegoEntryPoint();
    }

    @Bean
    public KerberosAuthenticationProvider kerberosAuthenticationProvider() throws Exception {
        KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
        SunJaasKerberosClient client = new SunJaasKerberosClient();
        client.setDebug(this.debug);
        provider.setKerberosClient(client);
        provider.setUserDetailsService(ldapUserDetailsService());
        return provider;
    }

    @Bean
    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter() throws Exception {
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.afterPropertiesSet();
        return filter;
    }

    @Bean
    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() throws Exception {
        KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
        provider.setTicketValidator(sunJaasKerberosTicketValidator());
        provider.setUserDetailsService(ldapUserDetailsService());
        provider.afterPropertiesSet();
        return provider;
    }

    @Bean
    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() throws Exception {
        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
        ticketValidator.setServicePrincipal(this.servicePrincipal);
        ticketValidator.setKeyTabLocation(new FileSystemResource(this.keyTab));
        ticketValidator.setDebug(this.debug);
        ticketValidator.afterPropertiesSet();
        return ticketValidator;
    }

    @Bean
    public KerberosLdapContextSource kerberosLdapContextSource() throws Exception {
        KerberosLdapContextSource contextSource = new KerberosLdapContextSource(this.adServer);
        SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
        FileSystemResource keyTabFile = new FileSystemResource(this.keyTab);
        loginConfig.setKeyTabLocation(keyTabFile);
        loginConfig.setServicePrincipal(this.servicePrincipal);
        log.info("KerberosLdapContextSource: service principal: " + servicePrincipal + ", " + keyTab + " - exists: " + keyTabFile.exists());
        loginConfig.setDebug(this.debug);
        loginConfig.setIsInitiator(true);
        loginConfig.afterPropertiesSet();
        contextSource.setLoginConfig(loginConfig);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapUserDetailsService ldapUserDetailsService() throws Exception {
        LdapUserSearch userSearch = new ApplicationSpecificFilterBasedLdapUserSearch(this.ldapSearchBase, this.ldapSearchFilter, kerberosLdapContextSource());
        LdapUserDetailsService service = new ApplicationSpecificLdapUserDetailsService(userSearch, this.group, this.onlyGroupMembers);
        service.setUserDetailsMapper(ldapUserDetailsContextMapper());
        return service;
    }

    @Bean
    public UserDetailsContextMapper ldapUserDetailsContextMapper() {
        return new ApplicationSpecificLdapUserDetailsMapper();
    }
}
```

Value of `this.adServer` must have the protocol specified as `ldap://`

In method `void configure(HttpSecurity http)` lines 27 to 31 were added.

After a successful user authentication via Kerberos Spring calls the following
method: `ApplicationSpecificLdapUserDetailsService::loadUserByUsername`. This service is responsible for providing the
UserDetails object from the authentication data (in NAE the type is LoggedUser). In this case it is implemented
using `ApplicationSpecificFilterBasedLdapUserSearch` to search for the given user in AD
and `ApplicationSpecificLdapUserDetailsMapper` for mapping the result onto a UserDetails object and putting the
necessary data into the database.

In order for the search to work it is necessary to provide a user filter and implement `LdapUserSearch`. An example of a
filter is `(userPrincipalName={0})`. The _{0}_ parameter is replaced with user principal name by default at the time of
search, which is obtained during the authentication. If the user is searchable using the obtained user principal
name `FilterBasedLdapUserSearch` (available with Spring) is sufficient. In case the user principal name needs to be
transformed before searching it is necessary to provide a custom
implementation (`ApplicationSpecificFilterBasedLdapUserSearch`), as seen below. With this approach it is possible to
specify more than a single parameter in the filter (not just *{0}*) if desired,
e.g.. `(|(sAMAccountName={1})(userPrincipalName={0}))`.

```java
public class ApplicationSpecificLdapUserDetailsService extends LdapUserDetailsService {

    public static final Logger log = LoggerFactory.getLogger(ApplicationSpecificLdapUserDetailsService.class);

    private final String group;
    private final boolean onlyGroupMembers;
    private final LdapUserSearch userSearch;
    private UserDetailsContextMapper userDetailsMapper;

    public ApplicationSpecificLdapUserDetailsService(LdapUserSearch userSearch, String group, boolean onlyGroupMembers) {
        super(userSearch);
        this.userSearch = userSearch;
        this.group = group;
        this.onlyGroupMembers = onlyGroupMembers;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Searching LDAP for user " + username);
        DirContextOperations userData = this.userSearch.searchForUser(username);
        validateGroupMembership(userData);
        UserDetails userDetails = this.userDetailsMapper.mapUserFromContext(userData, username, Collections.emptyList());
        return userDetails;
    }

    private void validateGroupMembership(DirContextOperations userData) throws UsernameNotFoundException {
        if (onlyGroupMembers && group != null) {
            String[] memberOf = userData.getStringAttributes("memberOf");
            if (memberOf != null) {
                List<String> groups = Arrays.asList(memberOf);
                groups.stream().filter(g -> g.equalsIgnoreCase(group)).findFirst().orElseThrow(() -> new UsernameNotFoundException("Not member of group " + group));
            } else {
                throw new UsernameNotFoundException("Not member of group " + group);
            }
        }
    }

    public void setUserDetailsMapper(UserDetailsContextMapper userDetailsMapper) {
        Assert.notNull(userDetailsMapper, "userDetailsMapper must not be null");
        this.userDetailsMapper = userDetailsMapper;
        super.setUserDetailsMapper(userDetailsMapper);
    }
}
```

```java
public class ApplicationSpecificFilterBasedLdapUserSearch implements LdapUserSearch {
    public static final Logger log = LoggerFactory.getLogger(ApplicationSpecificFilterBasedLdapUserSearch.class);

    private final ContextSource contextSource;
    private final SearchControls searchControls = new SearchControls();
    private String searchBase = "";
    private final String searchFilter;

    public ApplicationSpecificFilterBasedLdapUserSearch(String searchBase, String searchFilter, BaseLdapPathContextSource contextSource) {
        Assert.notNull(contextSource, "contextSource must not be null");
        Assert.notNull(searchFilter, "searchFilter must not be null.");
        Assert.notNull(searchBase, "searchBase must not be null (an empty string is acceptable).");
        this.searchFilter = searchFilter;
        this.contextSource = contextSource;
        this.searchBase = searchBase;
        this.setSearchSubtree(true);
        if (searchBase.length() == 0) {
            log.info("SearchBase not set. Searches will be performed from the root: " + contextSource.getBaseLdapPath());
        }
    }

    public DirContextOperations searchForUser(String username) {
        SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(this.contextSource);
        template.setSearchControls(this.searchControls);

        // custom parameters in filter
        String[] params;
        if (username.contains("@")) {
            String usernameNoDomain = username.split("@")[0];
            params = new String[]{
                    username, usernameNoDomain
            }; /*{0} =username, {1} = usernameNoDomain */
        } else {
            params = new String[]{
                    username, username
            };
        }

        try {
            return template.searchForSingleEntry(this.searchBase, this.searchFilter, params);
        } catch (IncorrectResultSizeDataAccessException var4) {
            if (var4.getActualSize() == 0) {
                throw new UsernameNotFoundException("User " + username + " not found in directory.");
            } else {
                throw var4;
            }
        }
    }

    public void setDerefLinkFlag(boolean deref) {
        this.searchControls.setDerefLinkFlag(deref);
    }

    public void setSearchSubtree(boolean searchSubtree) {
        this.searchControls.setSearchScope(searchSubtree ? 2 : 1);
    }

    public void setSearchTimeLimit(int searchTimeLimit) {
        this.searchControls.setTimeLimit(searchTimeLimit);
    }

    public void setReturningAttributes(String[] attrs) {
        this.searchControls.setReturningAttributes(attrs);
    }
}
```

```java
public class ApplicationSpecificLdapUserDetailsMapper extends LdapUserDetailsMapper {
    public static final Logger log = LoggerFactory.getLogger(ApplicationSpecificUserDetailsMapper.class);

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        return makeLoggedUser(ctx, userDetails);
    }

    protected LoggedUser makeLoggedUser(DirContextOperations ctx, UserDetails userDetails) {
        // search for user in DB
        User user = searchDB();
        if (user == null) {
            user = createNewUser(ctx, userDetails);
        } else {
            user = updateUser(user, ctx);
        }
        return user.transformToLoggedUser();
    }

    protected User createNewUser(DirContextOperations ctx, UserDetails userDetails) {
        // create new user in DB
        return user;
    }

    protected User updateUser(User user, DirContextOperations ctx) {
        // update existing user in DB
        return user;
    }
}
```

It is recommended to either extend or replace the default User object and save a uniquely identifying field, such
as `DN`, `userPrincipalName` or `sAMAccountName` - using which the specific user can be retrieved from the database
after authentication. Extending the User object may require an override of the default UserService provided by NAE.

## What happens during the authentication

The steps of Kerberos authentication in the application:

* Browser requests a resource from the API without an Authorization header
* Application server returns a 401 Unauthorized response, but appends a WWW-Authenticate: negotiate header (spNEGO
  authentication is configured)
* Browser reacts to the WWW-Authenticate: negotiate header and status 401 by requesting a ticket from the KDC and
  appending it to a new request as Authorization

The browser may need to be manually configured to behave this way,

e.g. in Firefox it is required to set (in about:config) **network.negotiate-auth.trusted-**  
**uris** to a value containing the domain with which the application is accessed (test.domain.com)

* Application validates the Kerberos token against the KDC
* Application requests the authenticated user data using the search filter provided
* Application authorizes the user and initiates a session
    

