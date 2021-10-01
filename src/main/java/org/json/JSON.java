/*!            
    C+edition for Desktop, Community Edition.
    Copyright (C) 2021 Cplusedition Limited.  All rights reserved.
    
    The author licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.json;

class JSON {
    /**
     * Returns the input if it is a JSON-permissible value; throws otherwise.
     */
    static double checkDouble(final double d) throws JSONException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new JSONException("Forbidden numeric value: " + d);
        }
        return d;
    }

    static Boolean toBoolean(final Object value) {
        if (value == JSONObject.NULL) {
            return null;
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            final String stringValue = (String)value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return false;
        }}
        return null;
    }

    static Double toDouble(final Object value) {
        if (value == JSONObject.NULL) {
            return null;
        } else if (value instanceof Double) {
            return (Double)value;
        } else if (value instanceof Number) {
            return ((Number)value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.valueOf((String)value);
            } catch (final NumberFormatException ignored) {
        }}
        return null;
    }

    static Integer toInteger(final Object value) {
        if (value == JSONObject.NULL) {
            return null;
        } else if (value instanceof Integer) {
            return (Integer)value;
        } else if (value instanceof Number) {
            return ((Number)value).intValue();
        } else if (value instanceof String) {
            try {
                return (int)Double.parseDouble((String)value);
            } catch (final NumberFormatException ignored) {
        }}
        return null;
    }

    static Long toLong(final Object value) {
        if (value == JSONObject.NULL) {
            return null;
        } else if (value instanceof Long) {
            return (Long)value;
        } else if (value instanceof Number) {
            return ((Number)value).longValue();
        } else if (value instanceof String) {
            try {
                return (long)Double.parseDouble((String)value);
            } catch (final NumberFormatException ignored) {
        }}
        return null;
    }

    static String toString(final Object value) {
        if (value == JSONObject.NULL) {
            return null;
        } else if (value instanceof String) {
            return (String)value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static JSONException typeMismatch(final Object indexOrName, final Object actual, final String requiredType)
        throws JSONException {
        if (actual == null) {
            throw new JSONException("Value at " + indexOrName + " is null.");
        } else {
            throw new JSONException(
                "Value "
                    + actual
                    + " at "
                    + indexOrName
                    + " of type "
                    + actual.getClass().getName()
                    + " cannot be converted to "
                    + requiredType);
    }}

    public static JSONException typeMismatch(final Object actual, final String requiredType) throws JSONException {
        if (actual == null) {
            throw new JSONException("Value is null.");
        } else {
            throw new JSONException(
                "Value " + actual + " of type " + actual.getClass().getName() + " cannot be converted to " + requiredType);
    }}
}
