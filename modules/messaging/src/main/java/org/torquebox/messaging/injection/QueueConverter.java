package org.torquebox.messaging.injection;

import javax.jms.Queue;

import org.torquebox.core.injection.ConvertableRubyInjection;
import org.torquebox.core.injection.InjectableConverter;

public class QueueConverter implements InjectableConverter {

    @Override
    public ConvertableRubyInjection wrap(Object object) {
        return new ConvertibleQueue( (Queue) object );
    }

}
