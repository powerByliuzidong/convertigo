/*
 * Copyright (c) 2001-2022 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.beans.statements;

import java.util.List;

import com.twinsoft.convertigo.beans.connectors.HtmlConnector;
import com.twinsoft.convertigo.beans.core.ScreenClass;
import com.twinsoft.convertigo.beans.screenclasses.HtmlScreenClass;
import com.twinsoft.convertigo.engine.EngineException;
import com.twinsoft.convertigo.engine.util.StringUtils;

public class ScHandlerStatement extends AbstractScHandlerStatement {

	private static final long serialVersionUID = -6843768711473408997L;
	
    public static final String CHOOSE_SCREENCLASS_NAME = "[Please choose a screen class]";
    
	private String normalizedScreenClassName = "";
	
	public ScHandlerStatement(String handlerType) throws EngineException {
		this(handlerType, CHOOSE_SCREENCLASS_NAME);
	}
	
	public ScHandlerStatement(String handlerType, String normalizedScreenClassName) throws EngineException {
		super(handlerType);
		this.normalizedScreenClassName = normalizedScreenClassName.equals(CHOOSE_SCREENCLASS_NAME) ? "" : normalizedScreenClassName;
		if (handlerType.equals(EVENT_ENTRY_HANDLER)) {
			setHandlerResult(RETURN_REDETECT);
			setName("on" + this.normalizedScreenClassName + "Entry");
		}
		else {
			setHandlerResult(RETURN_ACCUMULATE);
			setName("on" + this.normalizedScreenClassName + "Exit");
		}
	}
	
	/**
	 * @return Returns the normalizedScreenClassName.
	 */
	public String getNormalizedScreenClassName() {
		return normalizedScreenClassName;
	}

	/**
	 * @param normalizedScreenClassName The normalizedScreenClassName to set.
	 */
	public void setNormalizedScreenClassName(String normalizedScreenClassName) {
		this.normalizedScreenClassName = normalizedScreenClassName;
	}
	
	@Override
	public String[] getTagsForProperty(String propertyName) {
		if(propertyName.equals("normalizedScreenClassName")){
	    	HtmlConnector connector = (HtmlConnector) getParent().getParent();
	    	List<HtmlScreenClass> v = connector.getAllScreenClasses();
			String[] sNames = new String[v.size() + 1];
			sNames[0] = "";
			for (int i = 1 ; i <= v.size() ; i++) {
				ScreenClass screenClass = (ScreenClass)v.get(i-1);
				String normalizedScreenClassName = StringUtils.normalize(screenClass.getName());
				sNames[i] = normalizedScreenClassName;
			}
			return sNames;
		}
		return super.getTagsForProperty(propertyName);
	}
}
