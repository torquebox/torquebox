package org.torquebox.core.datasource;

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.as.connector.subsystems.datasources.AbstractDataSourceService;
import org.jboss.as.connector.subsystems.datasources.XaDataSourceService;
import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnectionFactory;
import org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.ds.CommonDataSource;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.deployers.common.DeployException;
import org.jboss.msc.value.InjectedValue;

public class HackDataSourceService extends XaDataSourceService {

    public HackDataSourceService(String jndiName) {
        super( jndiName );
    }

    @Override
    public AS7DataSourceDeployer getDeployer() {
        return new HackAS7DataSourceDeployer( ((InjectedValue<XaDataSource>) getDataSourceConfigInjector()).getValue() );
    }

    public class HackAS7DataSourceDeployer extends AbstractDataSourceService.AS7DataSourceDeployer {

        public HackAS7DataSourceDeployer(XaDataSource dataSourceConfig) {
            super( dataSourceConfig );
        }

        @Override
        protected ManagedConnectionFactory createMcf(XaDataSource arg0, String arg1, ClassLoader arg2) throws NotFoundException, DeployException {
            final MyXaMCF xaManagedConnectionFactory = new MyXaMCF();

            XaDataSource xaDataSourceConfig = ((InjectedValue<XaDataSource>) getDataSourceConfigInjector()).getValue();

            if (xaDataSourceConfig.getUrlDelimiter() != null) {
                try {
                    xaManagedConnectionFactory.setURLDelimiter( xaDataSourceConfig.getUrlDelimiter() );
                } catch (ResourceException e) {
                    throw new DeployException( "failed to get url delimiter", e );
                }
            }
            if (xaDataSourceConfig.getXaDataSourceClass() != null) {
                xaManagedConnectionFactory.setXADataSourceClass( xaDataSourceConfig.getXaDataSourceClass() );
            }
            if (xaDataSourceConfig.getXaDataSourceProperty() != null) {
                xaManagedConnectionFactory.setXaProps( xaDataSourceConfig.getXaDataSourceProperty() );
            }
            if (xaDataSourceConfig.getUrlSelectorStrategyClassName() != null) {
                xaManagedConnectionFactory
                        .setUrlSelectorStrategyClassName( xaDataSourceConfig.getUrlSelectorStrategyClassName() );
            }
            if (xaDataSourceConfig.getXaPool() != null && xaDataSourceConfig.getXaPool().isSameRmOverride() != null) {
                xaManagedConnectionFactory.setIsSameRMOverrideValue( xaDataSourceConfig.getXaPool().isSameRmOverride() );
            }

            if (xaDataSourceConfig.getNewConnectionSql() != null) {
                xaManagedConnectionFactory.setNewConnectionSQL( xaDataSourceConfig.getNewConnectionSql() );
            }

            if (xaDataSourceConfig.getUrlSelectorStrategyClassName() != null) {
                xaManagedConnectionFactory
                        .setUrlSelectorStrategyClassName( xaDataSourceConfig.getUrlSelectorStrategyClassName() );
            }

            setMcfProperties( xaManagedConnectionFactory, xaDataSourceConfig, xaDataSourceConfig.getStatement() );
            xaManagedConnectionFactory.setUserTransactionJndiName( "java:comp/UserTransaction" );
            return xaManagedConnectionFactory;
        }

        private void setMcfProperties(final BaseWrapperManagedConnectionFactory managedConnectionFactory,
                CommonDataSource dataSourceConfig, final Statement statement) {

            if (dataSourceConfig.getTransactionIsolation() != null) {
                managedConnectionFactory.setTransactionIsolation( dataSourceConfig.getTransactionIsolation().name() );
            }

            final DsSecurity security = dataSourceConfig.getSecurity();
            if (security != null) {
                if (security.getUserName() != null) {
                    managedConnectionFactory.setUserName( security.getUserName() );
                }
                if (security.getPassword() != null) {
                    managedConnectionFactory.setPassword( security.getPassword() );
                }
            }

            final TimeOut timeOut = dataSourceConfig.getTimeOut();
            if (timeOut != null) {
                if (timeOut.getUseTryLock() != null) {
                    managedConnectionFactory.setUseTryLock( timeOut.getUseTryLock().intValue() );
                }
                if (timeOut.getQueryTimeout() != null) {
                    managedConnectionFactory.setQueryTimeout( timeOut.getQueryTimeout().intValue() );
                }
            }

            if (statement != null) {
                if (statement.getTrackStatements() != null) {
                    managedConnectionFactory.setTrackStatements( statement.getTrackStatements().name() );
                }
                if (statement.isSharePreparedStatements() != null) {
                    managedConnectionFactory.setSharePreparedStatements( statement.isSharePreparedStatements() );
                }
                if (statement.getPreparedStatementsCacheSize() != null) {
                    managedConnectionFactory.setPreparedStatementCacheSize( statement.getPreparedStatementsCacheSize()
                            .intValue() );
                }
            }

            final Validation validation = dataSourceConfig.getValidation();
            if (validation != null) {
                if (validation.isValidateOnMatch()) {
                    managedConnectionFactory.setValidateOnMatch( validation.isValidateOnMatch() );
                }
                if (validation.getCheckValidConnectionSql() != null) {
                    managedConnectionFactory.setCheckValidConnectionSQL( validation.getCheckValidConnectionSql() );
                }
                final Extension validConnectionChecker = validation.getValidConnectionChecker();
                if (validConnectionChecker != null) {
                    if (validConnectionChecker.getClassName() != null) {
                        managedConnectionFactory.setValidConnectionCheckerClassName( validConnectionChecker.getClassName() );
                    }
                    if (validConnectionChecker.getConfigPropertiesMap() != null) {
                        managedConnectionFactory
                                .setValidConnectionCheckerProperties( buildConfigPropsString( validConnectionChecker
                                        .getConfigPropertiesMap() ) );
                    }
                }
                final Extension exceptionSorter = validation.getExceptionSorter();
                if (exceptionSorter != null) {
                    if (exceptionSorter.getClassName() != null) {
                        managedConnectionFactory.setExceptionSorterClassName( exceptionSorter.getClassName() );
                    }
                    if (exceptionSorter.getConfigPropertiesMap() != null) {
                        managedConnectionFactory.setExceptionSorterProperties( buildConfigPropsString( exceptionSorter
                                .getConfigPropertiesMap() ) );
                    }
                }
                final Extension staleConnectionChecker = validation.getStaleConnectionChecker();
                if (staleConnectionChecker != null) {
                    if (staleConnectionChecker.getClassName() != null) {
                        managedConnectionFactory.setStaleConnectionCheckerClassName( staleConnectionChecker.getClassName() );
                    }
                    if (staleConnectionChecker.getConfigPropertiesMap() != null) {
                        managedConnectionFactory
                                .setStaleConnectionCheckerProperties( buildConfigPropsString( staleConnectionChecker
                                        .getConfigPropertiesMap() ) );
                    }
                }
            }
        }

    }

    private class MyXaMCF extends XAManagedConnectionFactory {

        private static final long serialVersionUID = 4876371551002746953L;

        public void setXaProps(Map<String, String> inputProperties) {
            xaProps.putAll( inputProperties );
        }

    }

}
