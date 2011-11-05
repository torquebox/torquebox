package org.torquebox.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.vfs.VirtualFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class YAMLUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseYaml(VirtualFile file) throws YAMLException, IOException {
        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load( in );
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return data;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
    }

}
