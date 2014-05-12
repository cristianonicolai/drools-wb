/*
 * Copyright 2014 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.screens.guided.dtable.backend.server.indexing;

import java.util.ArrayList;
import java.util.List;

import org.drools.workbench.models.datamodel.imports.Import;
import org.drools.workbench.models.datamodel.rule.IAction;
import org.drools.workbench.models.datamodel.rule.IPattern;
import org.drools.workbench.models.datamodel.rule.RuleModel;
import org.drools.workbench.models.guided.dtable.shared.model.ActionCol52;
import org.drools.workbench.models.guided.dtable.shared.model.ActionInsertFactCol52;
import org.drools.workbench.models.guided.dtable.shared.model.ActionSetFieldCol52;
import org.drools.workbench.models.guided.dtable.shared.model.AttributeCol52;
import org.drools.workbench.models.guided.dtable.shared.model.BRLActionColumn;
import org.drools.workbench.models.guided.dtable.shared.model.BRLConditionColumn;
import org.drools.workbench.models.guided.dtable.shared.model.BaseColumn;
import org.drools.workbench.models.guided.dtable.shared.model.CompositeColumn;
import org.drools.workbench.models.guided.dtable.shared.model.ConditionCol52;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.drools.workbench.models.guided.dtable.shared.model.Pattern52;
import org.kie.workbench.common.services.refactoring.backend.server.indexing.DefaultIndexBuilder;
import org.kie.workbench.common.services.refactoring.model.index.RuleAttribute;
import org.kie.workbench.common.services.refactoring.model.index.Type;
import org.kie.workbench.common.services.refactoring.model.index.TypeField;
import org.uberfire.commons.data.Pair;
import org.uberfire.commons.validation.PortablePreconditions;

/**
 * Visitor to extract index information from a Guided Decision Table
 */
public class GuidedDecisionTableModelIndexVisitor {

    private final DefaultIndexBuilder builder;
    private final GuidedDecisionTable52 model;
    private final List<Pair<String, String>> results = new ArrayList<Pair<String, String>>();

    public GuidedDecisionTableModelIndexVisitor( final DefaultIndexBuilder builder,
                                                 final GuidedDecisionTable52 model ) {
        this.builder = PortablePreconditions.checkNotNull( "builder",
                                                           builder );
        this.model = PortablePreconditions.checkNotNull( "model",
                                                         model );
    }

    public List<Pair<String, String>> visit() {
        visit( model );
        results.addAll( builder.build() );
        return results;
    }

    private void visit( final Object o ) {
        if ( o instanceof GuidedDecisionTable52 ) {
            visit( (GuidedDecisionTable52) o );
        } else if ( o instanceof AttributeCol52 ) {
            visit( (AttributeCol52) o );
        } else if ( o instanceof Pattern52 ) {
            visit( (Pattern52) o );
        } else if ( o instanceof BRLConditionColumn ) {
            visit( (BRLConditionColumn) o );
        } else if ( o instanceof ConditionCol52 ) {
            visit( (ConditionCol52) o );
        } else if ( o instanceof BRLActionColumn ) {
            visit( (BRLActionColumn) o );
        } else if ( o instanceof ActionInsertFactCol52 ) {
            visit( (ActionInsertFactCol52) o );
        } else if ( o instanceof ActionSetFieldCol52 ) {
            visit( (ActionSetFieldCol52) o );
        } else if ( o instanceof IPattern ) {
            visit( (IPattern) o );
        } else if ( o instanceof IAction ) {
            visit( (IAction) o );
        }
    }

    private void visit( final GuidedDecisionTable52 o ) {
        for ( AttributeCol52 c : o.getAttributeCols() ) {
            visit( c );
        }
        for ( CompositeColumn<? extends BaseColumn> c : o.getConditions() ) {
            visit( c );
        }
        for ( ActionCol52 c : o.getActionCols() ) {
            visit( c );
        }
    }

    private void visit( final AttributeCol52 o ) {
        builder.addRuleAttribute( new RuleAttribute( o.getAttribute(),
                                                     o.getDefaultValueAsString() ) );
    }

    private void visit( final Pattern52 o ) {
        builder.addType( new Type( getFullyQualifiedClassName( o.getFactType() ) ) );
        for ( ConditionCol52 c : o.getChildColumns() ) {
            visit( c );
        }
    }

    private void visit( final BRLConditionColumn o ) {
        final RuleModel rm = new RuleModel();
        rm.setImports( model.getImports() );
        for ( IPattern p : o.getDefinition() ) {
            rm.addLhsItem( p );
        }
        final GuidedRuleModelIndexVisitor visitor = new GuidedRuleModelIndexVisitor( builder,
                                                                                     rm );
        results.addAll( visitor.visit() );
    }

    private void visit( final ConditionCol52 o ) {
        builder.addField( new TypeField( o.getFactField(),
                                         getFullyQualifiedClassName( o.getFieldType() ) ) );
    }

    private void visit( final BRLActionColumn o ) {
        final RuleModel rm = new RuleModel();
        rm.setImports( model.getImports() );
        for ( IAction a : o.getDefinition() ) {
            rm.addRhsItem( a );
        }
        final GuidedRuleModelIndexVisitor visitor = new GuidedRuleModelIndexVisitor( builder,
                                                                                     rm );
        results.addAll( visitor.visit() );
    }

    private void visit( final ActionInsertFactCol52 o ) {
        builder.addType( new Type( getFullyQualifiedClassName( o.getFactType() ) ) );
        builder.addField( new TypeField( o.getFactField(),
                                         getFullyQualifiedClassName( o.getType() ) ) );
    }

    private void visit( final ActionSetFieldCol52 o ) {
        builder.addField( new TypeField( o.getFactField(),
                                         getFullyQualifiedClassName( o.getType() ) ) );
    }

    private String getFullyQualifiedClassName( final String typeName ) {
        if ( typeName.contains( "." ) ) {
            return typeName;
        }

        for ( Import i : model.getImports().getImports() ) {
            if ( i.getType().endsWith( typeName ) ) {
                return i.getType();
            }
        }
        return model.getPackageName() + "." + typeName;
    }

}
