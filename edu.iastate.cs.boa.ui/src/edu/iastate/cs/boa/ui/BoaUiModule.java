/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iastate.cs.boa.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;

import edu.iastate.cs.boa.ui.syntaxcoloring.BoaAntlrTokenToAttributeIdMapper;
import edu.iastate.cs.boa.ui.syntaxcoloring.BoaHighlightingConfiguration;

/**
 * Use this class to register components to be used within the IDE.
 * 
 * @author rdyer
 */
public class BoaUiModule extends edu.iastate.cs.boa.ui.AbstractBoaUiModule {
	public BoaUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}

	public Class<? extends DefaultHighlightingConfiguration> bindILexicalHighlightingConfiguration() {
		return BoaHighlightingConfiguration.class;
	}

	public Class<? extends DefaultAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		return BoaAntlrTokenToAttributeIdMapper.class;
	}
}
