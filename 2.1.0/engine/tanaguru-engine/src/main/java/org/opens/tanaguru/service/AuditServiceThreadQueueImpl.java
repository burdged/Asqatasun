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
package org.opens.tanaguru.service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.opens.tanaguru.entity.audit.Audit;
import org.opens.tanaguru.service.command.AuditCommand;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author enzolalay
 */
public class AuditServiceThreadQueueImpl implements AuditServiceThreadQueue, AuditServiceThreadListener {
    
    private static final Logger LOGGER = Logger.getLogger(AuditServiceThreadQueueImpl.class);
    
    private Queue<AuditCommand> pageAuditWaitQueue = new ConcurrentLinkedQueue<AuditCommand>();
    private Queue<AuditCommand> scenarioAuditWaitQueue = new ConcurrentLinkedQueue<AuditCommand>();
    private Queue<AuditCommand> uploadAuditWaitQueue = new ConcurrentLinkedQueue<AuditCommand>();
    private Queue<AuditCommand> siteAuditWaitQueue = new ConcurrentLinkedQueue<AuditCommand>();

    private List<AuditServiceThread> pageAuditExecutionList = new ArrayList<AuditServiceThread>();
    private List<AuditServiceThread> scenarioAuditExecutionList = new ArrayList<AuditServiceThread>();
    private List<AuditServiceThread> uploadAuditExecutionList = new ArrayList<AuditServiceThread>();
    private List<AuditServiceThread> siteAuditExecutionList = new ArrayList<AuditServiceThread>();

    private static final int MAX_AUDIT_EXECUTION_LIST_VALUE = 3;
    
    private int pageAuditExecutionListMax = MAX_AUDIT_EXECUTION_LIST_VALUE;
    public int getPageAuditExecutionListMax() {
        return pageAuditExecutionListMax;
    }
    @Override
    public void setPageAuditExecutionListMax(int max) {
        this.pageAuditExecutionListMax = max;
    }

    private int scenarioAuditExecutionListMax = MAX_AUDIT_EXECUTION_LIST_VALUE;
    public int getScenarioAuditExecutionListMax() {
        return scenarioAuditExecutionListMax;
    }
    @Override
    public void setScenarioAuditExecutionListMax(int max) {
        this.scenarioAuditExecutionListMax = max;
    }

    private int uploadAuditExecutionListMax = MAX_AUDIT_EXECUTION_LIST_VALUE;
    public int getUploadAuditExecutionListMax() {
        return uploadAuditExecutionListMax;
    }
    @Override
    public void setUploadAuditExecutionListMax(int max) {
        this.uploadAuditExecutionListMax = max;
    }
    
    private int siteAuditExecutionListMax = MAX_AUDIT_EXECUTION_LIST_VALUE;
    public int getSiteAuditExecutionListMax() {
        return siteAuditExecutionListMax;
    }
    @Override
    public void setSiteAuditExecutionListMax(int max) {
        this.siteAuditExecutionListMax = max;
    }

    private Set<AuditServiceListener> listeners;
    public Set<AuditServiceListener> getListeners() {
        return listeners;
    }
    
    private Long lastToken = 0L;

    private AuditServiceThreadFactory auditServiceThreadFactory;

    @Autowired
    public void setAuditServiceThreadFactory(AuditServiceThreadFactory auditServiceThreadFactory) {
        this.auditServiceThreadFactory = auditServiceThreadFactory;
    }

    public AuditServiceThreadQueueImpl() {
        super();
    }

    @Override
    public void add(AuditServiceListener listener) {
        if (listeners == null) {
            listeners = new HashSet<AuditServiceListener>();
        }
        listeners.add(listener);
    }

    @Override
    public void remove(AuditServiceListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    @Override
    public synchronized void addPageAudit(AuditCommand auditCommand) {
        pageAuditWaitQueue.offer(auditCommand);
        processPageAuditWaitQueue();
    }

    private synchronized void processPageAuditWaitQueue() {
        processAuditWaitQueue(
                pageAuditWaitQueue,
                pageAuditExecutionList,
                pageAuditExecutionListMax);
    }

    @Override
    public synchronized void addScenarioAudit(AuditCommand auditCommand) {
        scenarioAuditWaitQueue.offer(auditCommand);
        processScenarioAuditWaitQueue();
    }

    private synchronized void processScenarioAuditWaitQueue() {
        processAuditWaitQueue(
                scenarioAuditWaitQueue,
                scenarioAuditExecutionList,
                scenarioAuditExecutionListMax);
    }

    @Override
    public synchronized void addPageUploadAudit(AuditCommand auditCommand) {
        uploadAuditWaitQueue.offer(auditCommand);
        processPageUploadAuditWaitQueue();
    }

    private synchronized void processPageUploadAuditWaitQueue() {
        processAuditWaitQueue(
                uploadAuditWaitQueue,
                uploadAuditExecutionList,
                uploadAuditExecutionListMax);
    }

    @Override
    public synchronized void addSiteAudit(AuditCommand auditCommand) {
        siteAuditWaitQueue.offer(auditCommand);
        processSiteAuditWaitQueue();
    }

    private synchronized void processSiteAuditWaitQueue() {
        processAuditWaitQueue(
                siteAuditWaitQueue,
                siteAuditExecutionList,
                siteAuditExecutionListMax);
    }

    /**
     *
     * @param auditWaitQueue
     * @param auditExecutionList
     * @param auditExecutionListMax
     * @return
     */
    private synchronized void processAuditWaitQueue(
            Queue<AuditCommand> auditWaitQueue,
            List<AuditServiceThread> auditExecutionList,
            int auditExecutionListMax) {
        if (auditWaitQueue.peek() == null) {
            return;
        }
        if (auditExecutionList.size() < auditExecutionListMax) {
            synchronized (lastToken) {
                Long token = new Date().getTime();
                while (token - lastToken < 10) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        LOGGER.error(ex);
                    }
                }
                lastToken = token.longValue();
            }
            AuditCommand auditCommand = auditWaitQueue.poll();
            AuditServiceThread auditServiceThread = auditServiceThreadFactory.create(auditCommand);

            auditServiceThread.add(this);
            auditExecutionList.add(auditServiceThread);
            new Thread(auditServiceThread).start();
        }
    }

    @Override
    public void auditCompleted(AuditServiceThread thread) {
        if (!pageAuditExecutionList.remove(thread) &&
                !scenarioAuditExecutionList.remove(thread) && 
                    !uploadAuditExecutionList.remove(thread)) {
            siteAuditExecutionList.remove(thread);
        }
        fireAuditCompleted(thread.getAudit());
        thread.remove(this);
        processWaitQueue();
    }

    @Override
    public void auditCrashed(AuditServiceThread thread, Exception exception) {
        if (!pageAuditExecutionList.remove(thread) &&
                scenarioAuditExecutionList.remove(thread) &&
                    uploadAuditExecutionList.remove(thread)) {
            siteAuditExecutionList.remove(thread);
        }
        fireAuditCrashed(thread.getAudit(), exception);
        thread.remove(this);
        processWaitQueue();
    }

    @Override
    public void processWaitQueue() {
        processPageAuditWaitQueue();
        processPageUploadAuditWaitQueue();
        processScenarioAuditWaitQueue();
        processSiteAuditWaitQueue();
    }

    private void fireAuditCompleted(Audit audit) {
        if (listeners == null) {
            return;
        }
        for (AuditServiceListener listener : listeners) {
            listener.auditCompleted(audit);
        }
    }

    private void fireAuditCrashed(Audit audit, Exception exception) {
        if (listeners == null) {
            return;
        }
        for (AuditServiceListener listener : listeners) {
            listener.auditCrashed(audit, exception);
        }
    }

}