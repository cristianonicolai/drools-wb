/*
 * Copyright 2015 JBoss Inc
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

package org.drools.workbench.screens.guided.dtable.client.widget.analysis.condition;

import java.util.Date;

import org.drools.workbench.models.guided.dtable.shared.model.Pattern52;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DateConditionInspectorSubsumptionTest {

    @Test
    public void testSubsume001() throws Exception {
        ComparableConditionInspector<Date> a = getCondition( new Date( 100 ), "!=" );
        ComparableConditionInspector<Date> b = getCondition( new Date( 100 ), "!=" );

        assertTrue( a.subsumes( b ) );
        assertTrue( b.subsumes( a ) );
    }

    @Test
    public void testSubsumeEquals001() throws Exception {
        ComparableConditionInspector<Date> a = getCondition( new Date( 100 ), "==" );
        ComparableConditionInspector<Date> b = getCondition( new Date( 10 ), ">" );

        assertFalse( a.subsumes( b ) );
        assertTrue( b.subsumes( a ) );
    }

    @Test
    public void testSubsumeEquals002() throws Exception {
        ComparableConditionInspector<Date> a = getCondition( new Date( 10 ), "==" );
        ComparableConditionInspector<Date> b = getCondition( new Date( 100 ), ">" );

        assertFalse( a.subsumes( b ) );
        assertFalse( b.subsumes( a ) );
    }

    private ComparableConditionInspector<Date> getCondition( Date date,
                                                             String operator ) {
        return new ComparableConditionInspector<Date>( mock( Pattern52.class ), "birthday", date, operator );
    }
}