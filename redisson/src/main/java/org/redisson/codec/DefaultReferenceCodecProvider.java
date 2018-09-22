/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.codec;

import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ConcurrentMap;
import org.redisson.client.codec.Codec;
import org.redisson.api.RObject;
import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RObjectField;
import org.redisson.liveobject.misc.ClassUtils;

/**
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public class DefaultReferenceCodecProvider implements ReferenceCodecProvider {

    public transient final ConcurrentMap<Class<? extends Codec>, Codec> codecCache = PlatformDependent.newConcurrentHashMap();

    @Override
    public <T extends Codec> T getCodec(Class<T> codecClass) {
        if (!codecCache.containsKey(codecClass)) {
            try {
                codecCache.putIfAbsent(codecClass, codecClass.newInstance());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return (T) codecCache.get(codecClass);
    }

    @Override
    public <T extends Codec> T getCodec(REntity anno, Class<?> cls) {
        if (!ClassUtils.isAnnotationPresent(cls, anno.annotationType())) {
            throw new IllegalArgumentException("Annotation REntity does not present on type [" + cls.getCanonicalName() + "]");
        }
        return this.<T>getCodec((Class<T>) anno.codec());
    }

    @Override
    public <T extends Codec, K extends RObject> T getCodec(RObjectField anno, Class<?> cls, Class<K> rObjectClass, String fieldName) {
        try {
            if (!ClassUtils.getDeclaredField(cls, fieldName).isAnnotationPresent(anno.getClass())) {
                throw new IllegalArgumentException("Annotation RObjectField does not present on field " + fieldName + " of type [" + cls.getCanonicalName() + "]");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if (rObjectClass.isInterface()) {
            throw new IllegalArgumentException("Cannot lookup an interface class of RObject [" + rObjectClass.getCanonicalName() + "]. Concrete class only.");
        }
        return this.<T>getCodec((Class<T>) anno.codec());
    }
    
    @Override
    public <T extends Codec, K extends RObject> T getCodec(Class<T> codecClass, Class<K> rObjectClass) {
        if (rObjectClass.isInterface()) {
            throw new IllegalArgumentException("Cannot lookup an interface class of RObject [" + rObjectClass.getCanonicalName() + "]. Concrete class only.");
        }
        return getCodec(codecClass);
    }
    
    @Override
    public <T extends Codec, K extends RObject> T getCodec(Class<T> codecClass, Class<K> rObjectClass, String name) {
        if (rObjectClass.isInterface()) {
            throw new IllegalArgumentException("Cannot lookup an interface class of RObject [" + rObjectClass.getCanonicalName() + "]. Concrete class only.");
        }
        return getCodec(codecClass, rObjectClass);
    }

    @Override
    public <T extends Codec> T getCodec(Class<T> codecClass, RObject rObject) {
        return getCodec(codecClass, rObject.getClass(), rObject.getName());
    }

    @Override
    public <T extends Codec> void registerCodec(Class<T> cls, T codec) {
        if (!cls.isInstance(codec)) {
            throw new IllegalArgumentException("codec is not an instance of the class [" + cls.getCanonicalName() + "]");
        }
        codecCache.putIfAbsent(cls, codec);
    }

}
