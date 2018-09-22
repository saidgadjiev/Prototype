package ru.saidgadjiev.prototype.core.codec;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 21.09.2018.
 */
public class UriAttributesDecoder {

    private Map<String, String> attributes;

    private final String path;

    private String targetPath;

    private boolean equal = true;

    public UriAttributesDecoder(String path, String targetPath) {
        this.path = path;
        this.targetPath = targetPath;
    }

    public String getAttr(String name) {
        if (attributes == null) {
            attributes = getPathAttributes();
        }

        return attributes.get(name);
    }

    public boolean isEqual() {
        if (attributes == null) {
            attributes = getPathAttributes();
        }

        return equal;
    }

    private Map<String,String> getPathAttributes() {
        String [] pathParts = path.split("/");
        String [] targetParts = targetPath.split("/");
        Map<String, String> attributes = new HashMap<>();

        for (int i = 0; i < Math.max(pathParts.length, targetParts.length); ++i) {
            if (pathParts.length <= i) {
                equal = false;

                break;
            }
            if (targetParts.length <= i) {
                equal = false;

                break;
            }
            if (!targetParts[i].equals(pathParts[i])) {
                if (isPathAttrName(targetParts[i])) {
                    attributes.put(toPathAttrName(targetParts[i]), pathParts[i]);
                } else {
                    equal = false;

                    break;
                }
            }
        }

        return attributes;
    }

    private String toPathAttrName(String pathAttrName) {
        return pathAttrName.substring(1, pathAttrName.length() - 1);
    }

    private boolean isPathAttrName(String pathAttrName) {
        return pathAttrName.startsWith("{") && pathAttrName.endsWith("}");
    }
}
