/*
 * Tanaguru - Automated webpage assessment
 * Copyright (C) 2008-2011  Open-S Company
 *
 * This file is part of Tanaguru.
 *
 * Tanaguru is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: open-s AT open-s DOT com
 */
package org.opens.tgol.action.voter;

import org.opens.tgol.action.Action;
import org.opens.tgol.action.ActionImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jkowalczyk
 */
public class ResultActionHandlerImpl implements ActionHandler {

    private Map<String, ActionVoter> actionAccessibilityVoterMap;

    public Map<String, ActionVoter> getActionAccessibilityVoterMap() {
        return actionAccessibilityVoterMap;
    }

    public void setActionAccessibilityVoterMap(
            Map<String, ActionVoter> actionAccessibilityVoterMap) {
        this.actionAccessibilityVoterMap = actionAccessibilityVoterMap;
    }

    private List<Action> actionList;

    public final void setActionList(List<Action> actionList) {
        this.actionList = actionList;

    }

    @SuppressWarnings("unchecked")
    public List<Action> getActionList() {
        List<Action> actionListCopy = new ArrayList<Action>();
        for (Action action : actionList) {
            actionListCopy.add((Action) ((ActionImpl)action).clone());
        }
        return actionListCopy;
    }

    private boolean isVoterInitialised = false;

    /**
     * This method initialises each voter with a copy of the contractAction
     * collection. Each voter, regarding its configuration will enable/disable
     * each action.
     */
    protected void initialiseVoter() {
        if (!isVoterInitialised) {
            for (ActionVoter cav : actionAccessibilityVoterMap.values()) {
                List<Action> cal = getActionList();
                cav.initialize(cal);
            }
            isVoterInitialised = true;
        }
    }

    /**
     * The action list is without condition for the moment
     * @param object
     * @return
     */
    @Override
    public synchronized List<Action> getActionList(
            Object object) {
        initialiseVoter();
        if (!(object instanceof String)) {
            return null;
        }
        String key = (String)object;
        ActionVoter actionAccessibilityVoter = chooseActionAccessibilityVoter(key);
        List<Action> userContractActionList = new ArrayList<Action>();
        if (actionAccessibilityVoter != null) {
            userContractActionList = actionAccessibilityVoter.getActionList();
        }
        return userContractActionList;
    }

    /**
     * 
     * @param product
     * @return
     *      the ContractActionVoter regarding the product
     */
    private ActionVoter chooseActionAccessibilityVoter(String key) {
        if (actionAccessibilityVoterMap.containsKey(key)) {
            return actionAccessibilityVoterMap.get(key);
        }
        return null;
    }
    
}