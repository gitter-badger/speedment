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
package com.speedment.orm.code.model.java.entity;

import com.speedment.orm.code.model.java.BaseEntityAndManagerTranslator;
import com.speedment.codegen.base.CodeGenerator;
import com.speedment.codegen.lang.models.AnnotationUsage;
import com.speedment.codegen.lang.models.File;
import com.speedment.codegen.lang.models.Import;
import com.speedment.codegen.lang.models.Interface;
import com.speedment.codegen.lang.models.Method;
import com.speedment.codegen.lang.models.Type;
import static com.speedment.codegen.lang.models.constants.DefaultType.LIST;
import static com.speedment.codegen.lang.models.constants.DefaultType.STRING;
import com.speedment.codegen.lang.models.implementation.GenericImpl;
import com.speedment.codegen.lang.models.values.ReferenceValue;
import com.speedment.orm.code.model.java.manager.EntityManagerTranslator;
import com.speedment.orm.config.model.Column;
import com.speedment.orm.config.model.ForeignKey;
import com.speedment.orm.config.model.ForeignKeyColumn;
import com.speedment.orm.config.model.Table;
import com.speedment.orm.core.entity.Entity;
import com.speedment.util.Pluralis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author pemi
 */
public class EntityTranslator extends BaseEntityAndManagerTranslator<Interface> {

    public EntityTranslator(CodeGenerator cg, Table configEntity) {
        super(cg, configEntity);
    }

    @Override
    protected Interface make(File file) {
        final Map<Table, List<String>> fkStreamers = new HashMap<>();

        final Interface iface = new InterfaceBuilder(ENTITY.getName())
                // Getters
                .addColumnConsumer((i, c) -> {
                    i.add(Method.of(GETTER_METHOD_PREFIX + typeName(c), Type.of(c.getMapping())));
                })
                // Add streamers from back pointing FK:s
                .addForeignKeyReferencesThisTableConsumer((i, fk) -> {
                    final FkUtil fu = new FkUtil(fk);
                    fu.imports().forEach(file::add);
                    final String methodName = pluralis(fu.getTable()) + "By" + typeName(fu.getColumn());
                    // Record for later use in the construction of aggregate streamers
                    fkStreamers.computeIfAbsent(fu.getTable(), t -> new ArrayList<>()).add(methodName);
                    final Type returnType = Type.of(Stream.class).add(fu.getEmt().GENERIC_OF_ENTITY);
                    final Method method = Method.of(methodName, returnType)
                    .default_()
                    .add("return " + managerTypeName(fu.getTable()) + ".get()")
                    //                    .add("return Platform.get().get(ManagerComponent.class)")
                    //                    .add("        .manager(" + managerTypeName(fu.getTable()) + ".class)")
                    .add("        .stream().filter(" + variableName(fu.getTable()) + " -> Objects.equals(this." + GETTER_METHOD_PREFIX + typeName(fu.getForeignColumn()) + "(), " + variableName(fu.getTable()) + "." + GETTER_METHOD_PREFIX + typeName(fu.getColumn()) + "()));");
                    i.add(method);
                })
                .addForeignKeyConsumer((i, fk) -> {
                    final FkUtil fu = new FkUtil(fk);
                    fu.imports().forEach(file::add);
                    final Type returnType;
                    final String getCode;
                    if (fu.getColumn().isNullable()) {
                        file.add(Import.of(Type.of(Optional.class)));
                        returnType = Type.of(Optional.class).add(fu.getForeignEmt().GENERIC_OF_ENTITY);
                        getCode = "";
                    } else {
                        returnType = fu.getForeignEmt().ENTITY.getType();
                        getCode = ".get()";
                    }
                    final Method method = Method.of("find" + typeName(fu.getColumn()), returnType).default_();
                    method.add("return " + fu.getForeignEmt().MANAGER.getName() + ".get()");
//                    method.add("        .manager(" + fu.getForeignEmt().MANAGER.getName() + ".class)");
                    method.add("        .stream().filter(" + variableName(fu.getForeignTable()) + " -> Objects.equals(this." + GETTER_METHOD_PREFIX + typeName(fu.getColumn()) + "(), " + variableName(fu.getForeignTable()) + "." + GETTER_METHOD_PREFIX + typeName(fu.getForeignColumn()) + "())).findAny()" + getCode + ";");
                    i.add(method);
                })
                .build()
                .public_();

        // Create aggregate streaming functions, if any
        fkStreamers.keySet().stream().forEach((referencingTable) -> {
            final List<String> methodNames = fkStreamers.get(referencingTable);
            if (!methodNames.isEmpty()) {
                final Method method = Method.of(pluralis(referencingTable), Type.of(Stream.class).add(new GenericImpl(typeName(referencingTable))))
                        .default_();
                if (methodNames.size() == 1) {
                    method.add("return " + methodNames.get(0) + "();");
                } else {
                    file.add(Import.of(Type.of(Function.class)));
                    method.add("return Stream.of("
                            + methodNames.stream().map(n -> n + "()").collect(Collectors.joining(", "))
                            + ").flatMap(Function.identity()).distinct();");
                }
                iface.add(method);
            }
        });

        // Requred for persist() et. al
//        file.add(Import.of(Type.of(Platform.class)));
//        file.add(Import.of(Type.of(ManagerComponent.class)));
        file.add(Import.of(Type.of(Optional.class)));

        iface
                .add(entityAnnotation(file))
                .add(builder())
                .add(toBuilder())
                .add(toJson())
                .add(stream())
                .add(persist())
                .add(update())
                .add(remove());
//                .add(manager());

        return iface;
    }

    public String pluralis(Table table) {
        return Pluralis.INSTANCE.pluralizeJavaIdentifier(variableName(table));
    }

//    private Method manager() {
//        return Method.of("manager", MANAGER.getType()).static_()
//                .add("return Platform.get().get(ManagerComponent.class).manager(" + MANAGER.getName() + ".class);");
//    }
    private AnnotationUsage entityAnnotation(File file) {
        final AnnotationUsage result = AnnotationUsage.of(Type.of(Entity.class));
        result.put("managerType", new ReferenceValue(MANAGER.getName() + ".class"));
        result.put("builderType", new ReferenceValue(BUILDER.getName() + ".class"));
        if (primaryKeyColumns().count() > 1) {
            result.put("primaryKeyType", new ReferenceValue("List.class"));
            file.add(Import.of(LIST));
        } else {
            primaryKeyColumns().map(pkc -> {
                final Class<?> mapping = pkc.getColumn().getMapping();
                file.add(Import.of(Type.of(mapping)));
                return new ReferenceValue(mapping.getSimpleName() + ".class");
            }).forEach(pk -> result.put("primaryKeyType", pk));
        }
        return result;
    }

    private Method builder() {
        return Method.of("builder", BUILDER.getType()).static_()
                .add("return " + MANAGER.getName() + ".get().builder();");
    }

    private Method toBuilder() {
        return Method.of("toBuilder", BUILDER.getType()).default_()
                .add("return " + MANAGER.getName() + ".get().toBuilder(this);");
    }

    private Method toJson() {
        return Method.of("toJson", STRING).default_()
                .add("return " + MANAGER.getName() + ".get().toJson(this);");

    }

    private Method stream() {
        return Method.of("stream", Type.of(Stream.class
        ).add(new GenericImpl(ENTITY.getName()))).static_()
                .add("return " + MANAGER.getName() + ".get().stream();");
    }

    private Method persist() {
        return dbMethod("persist");
    }

    private Method update() {
        return dbMethod("update");
    }

    private Method remove() {
        return dbMethod("remove");
    }

    private Method dbMethod(String name) {
        return Method.of(name, ENTITY.getOptionalType()).default_()
                .add("return " + MANAGER.getName() + ".get()." + name + "(this);");
    }

    @Override
    protected String getJavadocRepresentText() {
        return "An interface";
    }

    @Override
    protected String getFileName() {
        return ENTITY.getName();

    }

    private class FkUtil {

        private final ForeignKey fk;
        private final ForeignKeyColumn fkc;
        private final Column column;
        private final Table table;
        private final Column foreignColumn;
        private final Table foreignTable;
        private final EntityManagerTranslator emt;
        private final EntityManagerTranslator foreignEmt;

        public FkUtil(ForeignKey fk) {
            this.fk = fk;
            fkc = fk.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("FK " + fk.getName() + " does not have a ForeignKeyColumn"));
            column = fkc.getColumn();
            table = column.ancestor(Table.class).get();
            foreignColumn = fkc.getForeignColumn();
            foreignTable = fkc.getForeignTable();
            emt = new EntityManagerTranslator(getCodeGenerator(), getTable());
            foreignEmt = new EntityManagerTranslator(getCodeGenerator(), getForeignTable());
        }

        public Stream<Import> imports() {
            final Stream.Builder<Type> sb = Stream.builder();
//            sb.add(Type.of(Platform.class));
//            sb.add(Type.of(ManagerComponent.class));
            sb.add(Type.of(Objects.class));
            sb.add(getEmt().ENTITY.getType());
            sb.add(getEmt().MANAGER.getType());
            sb.add(getForeignEmt().ENTITY.getType());
            sb.add(getForeignEmt().MANAGER.getType());
            return sb.build().map(t -> Import.of(t));
        }

        public Column getColumn() {
            return column;
        }

        public Table getTable() {
            return table;
        }

        public Column getForeignColumn() {
            return foreignColumn;
        }

        public Table getForeignTable() {
            return foreignTable;
        }

        public EntityManagerTranslator getEmt() {
            return emt;
        }

        public EntityManagerTranslator getForeignEmt() {
            return foreignEmt;
        }

    }

}
