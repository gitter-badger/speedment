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
package com.speedment.codegen.java.views;

import com.speedment.codegen.base.CodeView;
import static com.speedment.codegen.Formatting.*;
import com.speedment.codegen.base.CodeGenerator;
import com.speedment.codegen.lang.models.EnumConstant;
import java.util.Optional;
import com.speedment.codegen.util.CodeCombiner;

/**
 *
 * @author Emil Forslund
 */
public class EnumConstantView implements CodeView<EnumConstant> {
	@Override
	public Optional<String> render(CodeGenerator cg, EnumConstant model) {
		return Optional.of(
			model.getName() + 
			(model.getValues().isEmpty() ? EMPTY : SPACE) +
			cg.onEach(model.getValues()).collect(
				CodeCombiner.joinIfNotEmpty(
					COMMA_SPACE, 
					PS, 
					PE
				)
			)
		);
	}
}