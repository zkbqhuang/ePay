//========================================================================
//Copyright 2012 David Yu
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

/**
 * Wraps a delegate.
 *
 * @author David Yu
 * @created Dec 5, 2012
 */
public class HasDelegate<T> implements PolymorphicSchema.Factory {

    public final Delegate<T> delegate;
    public final IdStrategy strategy;

    public final ArraySchemas.Base genericElementSchema;

    @SuppressWarnings("unchecked")
    public HasDelegate(Delegate<T> delegate, IdStrategy strategy) {
        this.delegate = delegate;
        this.strategy = strategy;

        genericElementSchema = new ArraySchemas.DelegateArray(strategy, null,
                (Delegate<Object>) delegate);
    }

    /**
     * Returns the delegate.
     */
    public final Delegate<T> getDelegate() {
        return delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final PolymorphicSchema newSchema(Class<?> typeClass,
                                             IdStrategy strategy, PolymorphicSchema.Handler handler) {
        return new ArraySchemas.DelegateArray(strategy, handler,
                (Delegate<Object>) delegate);
    }

}
