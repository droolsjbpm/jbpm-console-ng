package org.jbpm.console.ng.server.impl;

import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import org.jbpm.shared.services.cdi.Selectable;
import org.kie.commons.cluster.ClusterServiceFactory;
import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.commons.io.impl.cluster.IOServiceClusterImpl;
import org.kie.internal.task.api.UserGroupCallback;
import org.uberfire.backend.repositories.Repository;

import static org.uberfire.backend.server.repositories.SystemRepository.*;
import org.uberfire.security.impl.authz.RuntimeAuthorizationManager;
import org.uberfire.security.server.cdi.SecurityFactory;

/**
 * This class should contain all ApplicationScoped producers required by the application.
 */
@ApplicationScoped
public class ApplicationScopedProvider {

    @Inject
    @Named("clusterServiceFactory")
    private ClusterServiceFactory clusterServiceFactory;

    private IOService ioService;

    @PostConstruct
    public void setup() {
        SecurityFactory.setAuthzManager( new RuntimeAuthorizationManager() );
        if ( clusterServiceFactory == null ) {
            ioService = new IOServiceDotFileImpl();
        } else {
            ioService = new IOServiceClusterImpl( new IOServiceDotFileImpl(), clusterServiceFactory );
        }
    }

    @Inject
    @Selectable
    private UserGroupCallback userGroupCallback;

    @Produces
    public UserGroupCallback produceSelectedUserGroupCalback() {
        return userGroupCallback;
    }

    @Produces
    @Named("system")
    public Repository systemRepository() {
        return SYSTEM_REPO;
    }

    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;

    @Produces
    public EntityManagerFactory getEntityManagerFactory() {
        if ( this.emf == null ) {
            // this needs to be here for non EE containers
            try {
                this.emf = InitialContext.doLookup( "jBPMEMF" );
            } catch ( NamingException e ) {
                this.emf = Persistence.createEntityManagerFactory( "org.jbpm.domain" );
            }

        }
        return this.emf;
    }

    @Produces
    @Named("ioStrategy")
    public IOService ioService() {
        return ioService;
    }

    @Produces
    public Logger createLogger( InjectionPoint injectionPoint ) {
        return Logger.getLogger( injectionPoint.getMember().getDeclaringClass().getName() );
    }
}
