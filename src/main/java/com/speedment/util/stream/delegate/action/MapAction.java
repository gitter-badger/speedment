/**
 *
 * Copyright (c) 2006-2015, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.util.stream.delegate.action;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author pemi
 * @param <T> Incoming Stream type
 * @param <R> Outgoing Stream type
 */
public class MapAction<T, R> implements Action<T, R> {

    private final Function<? super T, ? extends R> mapper;

    public MapAction(final Function<? super T, ? extends R> mapper) {
        this.mapper = mapper;
    }

    public Function<? super T, ? extends R> getMapper() {
        return mapper;
    }

    @Override
    public Stream<R> apply(Stream<T> s) {
        return s.map(mapper);
    }

}
