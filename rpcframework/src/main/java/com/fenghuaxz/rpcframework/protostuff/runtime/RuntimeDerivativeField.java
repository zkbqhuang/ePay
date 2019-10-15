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

import com.fenghuaxz.rpcframework.protostuff.Input;
import com.fenghuaxz.rpcframework.protostuff.Schema;
import com.fenghuaxz.rpcframework.protostuff.Tag;
import com.fenghuaxz.rpcframework.protostuff.WireFormat;

import java.io.IOException;

/**
 * A runtime field w/c represents an abstract class, interface or a base type with many possible subclasses.
 * <p>
 * The type metadata is written for the deserializer to know the actual/exact schema to use upon deserialization.
 * <p>
 * Limitations: The number of fields are limited to 126 (127 is the usual limit anyway). The order of the fields being
 * written must be preserved. It will not work if the pojo serialized is coming from the browser since the fields
 * will most likey be out-of-order (unless you have control of the json serialization).
 *
 * @author David Yu
 * @created Jan 16, 2011
 */
abstract class RuntimeDerivativeField<T> extends Field<T> {

    /**
     * The schema of the polymorphic pojo.
     */
    public final DerivativeSchema schema;

    /**
     * The class of the pojo field.
     */
    public final Class<Object> typeClass;

    public RuntimeDerivativeField(Class<Object> typeClass, WireFormat.FieldType type,
                                  int number, String name, boolean repeated, Tag tag,
                                  IdStrategy strategy) {
        super(type, number, name, repeated, tag);
        this.typeClass = typeClass;

        schema = new DerivativeSchema(strategy) {
            @Override
            protected void doMergeFrom(Input input,
                                       Schema<Object> derivedSchema, Object owner)
                    throws IOException {
                RuntimeDerivativeField.this.doMergeFrom(input, derivedSchema,
                        owner);
            }
        };
    }

    protected abstract void doMergeFrom(Input input,
                                        Schema<Object> derivedSchema, Object owner) throws IOException;
}
