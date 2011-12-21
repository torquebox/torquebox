package org.torquebox.core.injection.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.injection.InjectionMetaData;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;

public class InjectionYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public InjectionYamlParsingProcessor() {
        setSectionName( "injection" );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parse(DeploymentUnit unit, Object data) throws Exception {
        Map<String, Object> injection = (Map<String, Object>) data;
        if (injection != null) {
            InjectionMetaData imd = new InjectionMetaData();
            Boolean enabled = (Boolean) injection.get( "enabled" );
            imd.setEnabled( enabled );
            unit.putAttachment( InjectionMetaData.ATTACHMENT_KEY, imd );
        }

    }

}
