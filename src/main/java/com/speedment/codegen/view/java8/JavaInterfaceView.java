package com.speedment.codegen.view.java8;

import com.speedment.codegen.CodeGenerator;
import com.speedment.codegen.view.CodeView;
import com.speedment.util.$;
import static com.speedment.codegen.CodeUtil.*;
import static com.speedment.codegen.view.java8.JavaRenderSupport.*;
import com.speedment.codegen.model.Interface_;
import com.speedment.codegen.model.modifier.InterfaceModifier_;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Duncan
 * @param <Model>
 */
public class JavaInterfaceView<Model extends Interface_> extends CodeView<Model> {
	private final Map<InterfaceModifier_, CharSequence> modifierTexts = new EnumMap<>(InterfaceModifier_.class);
			
	@Override
	public CharSequence render(CodeGenerator renderer, Model interf) {
		return new $(
			renderPackage(renderer, interf), dnl(),
			renderModifiers(interf, renderer, SPACE),
			renderName(interf), SPACE,
			renderList(interf.getParents(), renderer, COMMA_STRING, EXTENDS_STRING, SPACE),
			looseBracketsIndent(new $(
				renderList(interf.getConstants(), renderer, nl()), dnl(),
				renderList(interf.getMethods(), renderer, dnl())
			))
		);
	}
}