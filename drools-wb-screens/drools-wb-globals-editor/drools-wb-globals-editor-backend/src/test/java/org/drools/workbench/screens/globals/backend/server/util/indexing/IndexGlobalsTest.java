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

package org.drools.workbench.screens.globals.backend.server.util.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.drools.workbench.screens.globals.type.GlobalResourceTypeDefinition;
import org.junit.Test;
import org.kie.workbench.common.services.refactoring.backend.server.BaseIndexingTest;
import org.kie.workbench.common.services.refactoring.backend.server.TestIndexer;
import org.kie.workbench.common.services.refactoring.backend.server.indexing.RuleAttributeNameAnalyzer;
import org.kie.workbench.common.services.refactoring.model.index.IndexableElements;
import org.uberfire.java.nio.file.Path;
import org.uberfire.metadata.backend.lucene.index.LuceneIndex;
import org.uberfire.metadata.backend.lucene.util.KObjectUtil;
import org.uberfire.metadata.engine.Index;
import org.uberfire.metadata.model.KObject;

import static org.apache.lucene.util.Version.*;
import static org.junit.Assert.*;

public class IndexGlobalsTest extends BaseIndexingTest<GlobalResourceTypeDefinition> {

    @Test
    public void testIndexGlobals() throws IOException, InterruptedException {
        //Don't ask, but we need to write a single file first in order for indexing to work
        final Path basePath = getDirectoryPath().resolveSibling( "someNewOtherPath" );
        ioService().write( basePath.resolve( "dummy" ),
                           "<none>" );

        //Add test files
        final Path path1 = basePath.resolve( "global1.gdrl" );
        final String drl1 = loadText( "global1.gdrl" );
        ioService().write( path1,
                           drl1 );
        final Path path2 = basePath.resolve( "global2.gdrl" );
        final String drl2 = loadText( "global2.gdrl" );
        ioService().write( path2,
                           drl2 );

        Thread.sleep( 5000 ); //wait for events to be consumed from jgit -> (notify changes -> watcher -> index) -> lucene index

        final Index index = getConfig().getIndexManager().get( org.uberfire.metadata.io.KObjectUtil.toKCluster( basePath.getFileSystem() ) );

        {
            final IndexSearcher searcher = ( (LuceneIndex) index ).nrtSearcher();
            final TopScoreDocCollector collector = TopScoreDocCollector.create( 10,
                                                                                true );

            final BooleanQuery query = new BooleanQuery();
            query.add( new TermQuery( new Term( IndexableElements.TYPE_NAME.toString(),
                                                "java.util.arraylist" ) ),
                       BooleanClause.Occur.MUST );
            searcher.search( query,
                             collector );
            final ScoreDoc[] hits = collector.topDocs().scoreDocs;
            assertEquals( 2,
                          hits.length );

            final List<KObject> results = new ArrayList<KObject>();
            for ( int i = 0; i < hits.length; i++ ) {
                results.add( KObjectUtil.toKObject( searcher.doc( hits[ i ].doc ) ) );
            }
            assertContains( results,
                            path1 );
            assertContains( results,
                            path2 );

            ( (LuceneIndex) index ).nrtRelease( searcher );
        }
    }

    @Override
    protected TestIndexer getIndexer() {
        return new TestGlobalsFileIndexer();
    }

    @Override
    public Map<String, Analyzer> getAnalyzers() {
        return new HashMap<String, Analyzer>() {{
            put( IndexableElements.RULE_ATTRIBUTE_NAME.toString(),
                 new RuleAttributeNameAnalyzer( LUCENE_40 ) );
        }};
    }

    @Override
    protected GlobalResourceTypeDefinition getResourceTypeDefinition() {
        return new GlobalResourceTypeDefinition();
    }

    @Override
    protected String getRepositoryName() {
        return this.getClass().getSimpleName();
    }

}
