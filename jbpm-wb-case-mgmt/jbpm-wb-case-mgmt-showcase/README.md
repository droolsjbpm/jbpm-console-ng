# jBPM Case Management Showcase

Showcase application for managing Case instances backed by a running [Kie Server](https://docs.jboss.org/drools/release/latest/drools-docs/html_single/#_ch.kie.server

## Getting Started

You can clone the repository and build all the projects using: mvn clean install

In order to start the showcase for development you should execute the following Maven commands:
```
cd jbpm-wb-case-mgmt/
mvn clean install
cd jbpm-wb-case-mgmt-showcase/
mvn clean gwt:run
```
The default login for the showcase app is admin/admin

## Connecting to the KIE Execution Server

There are a few supported ways for you to connect and authenticate to a running Kie Server.
These settings are provded using system variables to the running application server where the Case Management application is runnig.
If running on Wildfly, please check the different ways you can provide these settings:
- [A quick tour](https://docs.wildfly.org/23/Getting_Started_Guide.html#wildfly---a-quick-tour)
- [Starting Wildfly](https://docs.wildfly.org/23/Getting_Started_Guide.html#starting-wildfly-23)

### General settings

To get started, you need to provide the url for the running Kie Server instance. This is done via the system property *org.kie.server.location*.

  Example:

  *org.kie.server.location*=http://localhost:8230/kie-server/services/rest/server

For some administrative operations to be executed on the Kie Server, i.e. provisioning of example project, it is also required that a pre-defined user is set.
This can be done via two different methods:

- Using a pre-defined user name and password (default)

    Example:

    *org.kie.server.user*=kieserver

    *org.kie.server.pwd*=kieserver1!

- Using a Keyclock token

    For more information on how to set up Keyclock using token based authentication, please read the [Keycloak SSO integration](https://docs.jboss.org/drools/release/6.5.0.Final/drools-docs/html_single/index.html#kie.KeycloakSSOIntegration) documentaion.

    Example:

    *org.kie.server.location*=http://localhost:8230/kie-server/services/rest/server

    *org.kie.server.token*=kieserver1!

For more information about how to set up and operate the Kie Server, please read the upstream [documentation](https://docs.jboss.org/drools/release/latest/drools-docs/html_single/#_ch.kie.server).

### Authentication setup

To authenticate using the current logged in user and forward the user credentials to the Kie Server, a new login module should be added to the WildFly instance running the showcase app.
  Please add this new login module in the security domain *other*.
  ```
  <login-module code="org.kie.security.jaas.KieLoginModule" flag="required" module="deployment.jbpm-wb-case-mgmt-showcase.war"/>
  ```
  When using this mode, users must co-exist in both servers where the showcase app and Kie Server are running in order for the authentication to suceed.

  Users should also have the following roles:
  - user
  - kie-server

## Development mode

The development mode allows you to manage Case instances using an in memory database, whithout connecting to a running Kie Server.
To start the showcase in development mode, please make sure the system property *org.kie.server.location* is not provided.
In order to create case instances, a list of case definitions is set via JSON format [case_definitions.json](./src/main/resources/case_definitions.json).

## Deploying to Wildfly 23

Download and unzip the WildFly 23 distribution. Let's call the root of the distribution WILDFLY_HOME. This directory is named after the WildFly version, so for example wildfly-23.0.2.Final.

Build the jbpm-wb-case-mgmt-showcase project using the following Maven command
```
cd jbpm-wb-case-mgmt/
mvn clean install -Dfull
```
Copy the newly create war file (jbpm-wb-case-mgmt-showcase/jbpm-wb-case-mgmt-showcase.war) into WILDFLY_HOME/standalone/deployments.

Configure user(s) and role(s). Execute the following command:
```
WILDFLY_HOME/bin/add-user.[sh|bat] -a -u 'kieserver' -p 'kieserver1!' -ro 'user'
```
You can of course choose different username and password, just make sure that the user has role user.

Start the server by running:
```
./standalone.sh -Dorg.kie.server.location=http://localhost:8230/kie-server/services/rest/server -Dorg.kie.server.user=kieserver -Dorg.kie.server.pwd=kieserver1!
```

Verify the application is running. Go to http://SERVER:PORT/jbpm-wb-case-mgmt-showcase/ and type the specified username and password. You should see simple XML message with basic information about the server.

For more details, please check the [Getting Started Guide](https://docs.wildfly.org/23/Getting+Started+Guide).