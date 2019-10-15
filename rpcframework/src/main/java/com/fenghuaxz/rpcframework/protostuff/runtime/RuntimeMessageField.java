//========================================================================
//Copyright 2007-2011 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtainType a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.fenghuaxz.rpcframework.protostuff.runtime;

import com.fenghuaxz.rpcframework.protostuff.Pipe;
import com.fenghuaxz.rpcframework.protostuff.Schema;
import com.fenghuaxz.rpcframework.protostuff.Tag;
import com.fenghuaxz.rpcframework.protostuff.WireFormat;

/**
 * A runtime pojo field that lazily loads the schema to support cyclic dependencies.
 *
 * @author David Yu
 * @created Jan 19, 2011
 */
abstract class RuntimeMessageField<T, P> extends Field<T> {

    /**
     * The class of the pojo field.
     */
    public final Class<P> typeClass;

    final HasSchema<P> hasSchema;

    public RuntimeMessageField(Class<P> typeClass, HasSchema<P> hasSchema,
                               WireFormat.FieldType type, int number, String name, boolean repeated, Tag tag) {
        super(type, number, name, repeated, tag);
        this.typeClass = typeClass;
        this.hasSchema = hasSchema;
    }

    /**
     * Returns the schema.
     */
    public Schema<P> getSchema() {
        return hasSchema.getSchema();
    }

    /**
     * Returns the lazy initialized pipe schema.
     */
    public Pipe.Schema<P> getPipeSchema() {
        return hasSchema.getPipeSchema();
    }

}
