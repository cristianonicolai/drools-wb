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

package org.drools.workbench.screens.guided.dtable.client.widget.analysis.panel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;
import org.drools.workbench.screens.guided.dtable.client.widget.analysis.reporting.Issue;

public interface AnalysisReportScreenView
        extends IsWidget {

    void setUpDataProvider( ListDataProvider<Issue> dataProvider );

    void setPresenter( AnalysisReportScreen presenter );

    void show( Issue issue );

    void clearIssue();
}
