/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.agiletec.plugins.jpversioning.aps.system.services.versioning;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntSafeXmlUtils;
import org.xml.sax.InputSource;

import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.common.entity.parse.EntityHandler;
import com.agiletec.aps.system.services.baseconfig.ConfigInterface;
import com.agiletec.aps.system.services.category.ICategoryManager;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.ContentRecordVO;
import com.agiletec.plugins.jpversioning.aps.system.JpversioningSystemConstants;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

/**
 * @author E.Santoboni, E.Mezzano
 */
@Aspect
public class VersioningManager extends AbstractService implements IVersioningManager {

    private static final EntLogger _logger = EntLogFactory.getSanitizedLogger(VersioningManager.class);

    @Override
    public void init() throws Exception {
        String deleteMidVersions = this.getConfigManager().getParam(JpversioningSystemConstants.CONFIG_PARAM_DELETE_MID_VERSIONS);
        this.setDeleteMidVersions("true".equalsIgnoreCase(deleteMidVersions));
        _logger.debug("{} ready", this.getClass().getName());
    }

    @Before("execution(* com.agiletec.plugins.jacms.aps.system.services.content.IContentManager.saveContent(..)) && args(content)")
    public void onSaveContent(Content content) {
        try {
            if (!this.hasToVersionContent(content)) {
                return;
            }
            this.saveContentVersion(content.getId());
        } catch (Exception e) {
            _logger.error("error in onSaveContent", e);
        }
    }

    @Before("execution(* com.agiletec.plugins.jacms.aps.system.services.content.IContentManager.insertOnLineContent(..)) && args(content)")
    public void onInsertOnLineContent(Content content) {
        try {
            if (!this.hasToVersionContent(content)) {
                return;
            }
            this.saveContentVersion(content.getId());
        } catch (Exception e) {
            _logger.error("error in onInsertOnLineContent", e);
        }
    }

    @Before("execution(* com.agiletec.plugins.jacms.aps.system.services.content.IContentManager.removeOnLineContent(..)) && args(content)")
    public void onRemoveOnLineContent(Content content) {
        try {
            if (!this.hasToVersionContent(content)) {
                return;
            }
            this.saveContentVersion(content.getId());
        } catch (Exception e) {
            _logger.error("error in onRemoveOnLineContent", e);
        }
    }

    @Before("execution(* com.agiletec.plugins.jacms.aps.system.services.content.IContentManager.deleteContent(..)) && args(content)")
    public void onDeleteContent(Content content) {
        try {
            if (!this.hasToVersionContent(content)) {
                return;
            }
            this.saveContentVersion(content.getId());
        } catch (Exception e) {
            _logger.error("error in onDeleteContent", e);
        }
    }

    private boolean hasToVersionContent(Content content) {
        String contentTypesToIgnoreCsv = this.getConfigManager().getParam(JpversioningSystemConstants.CONFIG_PARAM_CONTENT_TYPES_TO_IGNORE);
        if (!StringUtils.isBlank(contentTypesToIgnoreCsv)) {
            String[] codes = contentTypesToIgnoreCsv.split(",");
            for (String code : codes) {
                if (code.trim().equalsIgnoreCase(content.getTypeCode())) {
                    return false;
                }
            }
        }
        String contentsToIgnoreCsv = this.getConfigManager().getParam(JpversioningSystemConstants.CONFIG_PARAM_CONTENTS_TO_IGNORE);
        if (!StringUtils.isBlank(contentsToIgnoreCsv)) {
            String[] codes = contentsToIgnoreCsv.split(",");
            for (String code : codes) {
                if (code.trim().equalsIgnoreCase(content.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<Long> getVersions(String contentId) throws EntException {
        try {
            return this.getVersioningDAO().getVersions(contentId);
        } catch (Exception e) {
            _logger.error("Error loading version identifiers", e);
            throw new EntException("Error loading version identifiers");
        }
    }

    @Override
    public List<Long> getLastVersions(String contentType, String descr) throws EntException {
        try {
            return this.getVersioningDAO().getLastVersions(contentType, descr);
        } catch (Exception e) {
            _logger.error("Error loading last version identifiers", e);
            throw new EntException("Error loading last version identifiers");
        }
    }

    @Override
    public ContentVersion getVersion(long id) throws EntException {
        try {
            return this.getVersioningDAO().getVersion(id);
        } catch (Exception e) {
            _logger.error("Error loading version of id {}", id, e);
            throw new EntException("Error loading version of id " + id);
        }
    }

    @Override
    public ContentVersion getLastVersion(String contentId) throws EntException {
        try {
            return this.getVersioningDAO().getLastVersion(contentId);
        } catch (Exception e) {
            _logger.error("Error loading last version for content {}", contentId, e);
            throw new EntException("Error loading last version for content" + contentId);
        }
    }

    @Override
    public void saveContentVersion(String contentId) throws EntException {
        try {
            if (contentId != null) {
                ContentRecordVO record = this.getContentManager().loadContentVO(contentId);
                if (record != null) {
                    ContentVersion versionRecord = this.createContentVersion(record);
                    //CANCELLAZIONE VERSIONE WORK OBSOLETE
                    if (versionRecord.isApproved()) {
                        int onlineVersionsToDelete = versionRecord.getOnlineVersion() - 1;
                        this.deleteWorkVersions(versionRecord.getContentId(), onlineVersionsToDelete);
                    }
                    this.getVersioningDAO().addContentVersion(versionRecord);
                }
            }
        } catch (Exception e) {
            _logger.error("error in Error saving version for content {}", contentId, e);
            throw new EntException("Error saving version for content" + contentId);
        }
    }

    @Override
    public void deleteWorkVersions(String contentId, int onlineVersion) throws EntException {
        try {
            if (this.isDeleteMidVersions()) {
                this.getVersioningDAO().deleteWorkVersions(contentId, onlineVersion);
            }
        } catch (Exception e) {
            _logger.error("Error in delete Work Versions", e);
            throw new EntException("Errore in delete Work Versions", e);
        }
    }

    @Override
    public Content getContent(ContentVersion contentVersion) throws EntException {
        try {
            return this.createContentFromXml(contentVersion.getContentType(), contentVersion.getXml());
        } catch (Exception e) {
            _logger.error("Error loading Content from version xml", e);
            throw new EntException("Error loading Content from version xml", e);
        }
    }

    /**
     * Crea un'entità specifica valorizzata in base alla sua definizione in xml
     * ed al tipo.
     *
     * @param entityTypeCode Il codice del tipo di entità.
     * @param xml L'xml dell'entità specifica.
     * @return L'entità valorizzata.
     * @throws EntException In caso di errore nella lettura dell'entità.
     */
    protected Content createContentFromXml(String entityTypeCode, String xml) throws EntException {
        try {
            Content entityPrototype = (Content) this.getContentManager().getEntityPrototype(entityTypeCode);
            SAXParser parser = EntSafeXmlUtils.newSafeSAXParser();
            InputSource is = new InputSource(new StringReader(xml));
            EntityHandler handler = this.getEntityHandler();
            handler.initHandler(entityPrototype, this.getXmlAttributeRootElementName(), this.getCategoryManager());
            parser.parse(is, handler);
            return entityPrototype;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            _logger.error("Error on creation entity. typecode: {} xml: {}", entityTypeCode, xml, e);
            throw new EntException("Error on creation entity", e);
        }
    }

    private ContentVersion createContentVersion(ContentRecordVO record) {
        ContentVersion versionRecord = new ContentVersion();
        versionRecord.setContentId(record.getId());
        versionRecord.setContentType(record.getTypeCode());
        versionRecord.setDescr(record.getDescription());
        versionRecord.setStatus(record.getStatus());
        versionRecord.setXml(record.getXmlWork());
        versionRecord.setVersionDate(record.getModify());
        String version = record.getVersion();
        versionRecord.setVersion(version);
        String[] versionParts = version.split("\\.");
        int onlineVersion = Integer.parseInt(versionParts[0]);
        versionRecord.setOnlineVersion(onlineVersion);
        int workVersion = Integer.parseInt(versionParts[1]);
        versionRecord.setApproved(workVersion == 0);
        versionRecord.setUsername(record.getLastEditor());
        return versionRecord;
    }

    protected boolean isDeleteMidVersions() {
        return _deleteMidVersions;
    }

    protected void setDeleteMidVersions(boolean deleteMidVersions) {
        this._deleteMidVersions = deleteMidVersions;
    }

    @Override
    public void deleteVersion(long versionId) {
        this.getVersioningDAO().deleteVersion(versionId);
    }

    protected String getXmlAttributeRootElementName() {
        return this._xmlAttributeRootElementName;
    }

    /**
     * Setta il nome dell'attributo della root dell'xml rappresentante la
     * singola entità. Il metodo è ad uso della definizione del servizio
     * nell'xml di configurazione di spring. Di default, la definizione del
     * servizio astratto nella configurazione di spring presenta una un nome
     * base "entity"; questa definizione và sostituita nella definizione del
     * servizio concreto nel caso si desideri specificare un nome particolare.
     *
     * @param xmlAttributeRootElementName Il nome dell'attributo della root
     * dell'xml rappresentante la singola entità.
     */
    public void setXmlAttributeRootElementName(String xmlAttributeRootElementName) {
        this._xmlAttributeRootElementName = xmlAttributeRootElementName;
    }

    /**
     * Restituisce la classe handler delegata alla interpretazione degli xml
     * delle singole entità. Il metodo restituisce un prototipo pronto per la
     * interpretazione di una entità.
     *
     * @return La classe handler delegata alla interpretazione degli xml delle
     * singole entità.
     */
    protected EntityHandler getEntityHandler() {
        return this._entityHandler.getHandlerPrototype();
    }

    /**
     * Setta la classe handler delegata alla interpretazione degli xml delle
     * singole entità. Il metodo è ad uso della definizione del servizio
     * nell'xml di configurazione di spring. Di default, la definizione del
     * servizio astratto nella configurazione di spring presenta una classe
     * handler base (EntityHandler); questa definizione và sostituita nella
     * definizione del servizio concreto nel caso si desideri specificare
     * un'handler particolare (estendente EntityHandler) delegato alla lettura
     * di entità particolari derivate dalla struttura specifica della classe
     * entità (estendente ApsEntity) che si desidera gestire nel servizio.
     *
     * @param entityHandler La classe handler delegata alla interpretazione
     * degli xml delle singole entità.
     */
    public void setEntityHandler(EntityHandler entityHandler) {
        this._entityHandler = entityHandler;
    }

    protected IVersioningDAO getVersioningDAO() {
        return _versioningDAO;
    }

    public void setVersioningDAO(IVersioningDAO versioningDAO) {
        this._versioningDAO = versioningDAO;
    }

    protected IContentManager getContentManager() {
        return _contentManager;
    }

    public void setContentManager(IContentManager contentManager) {
        this._contentManager = contentManager;
    }

    protected ICategoryManager getCategoryManager() {
        return _categoryManager;
    }

    public void setCategoryManager(ICategoryManager categoryManager) {
        this._categoryManager = categoryManager;
    }

    protected ConfigInterface getConfigManager() {
        return _configManager;
    }

    public void setConfigManager(ConfigInterface configManager) {
        this._configManager = configManager;
    }

    private boolean _deleteMidVersions;

    private EntityHandler _entityHandler;

    private String _xmlAttributeRootElementName;

    private IVersioningDAO _versioningDAO;

    private IContentManager _contentManager;
    private ICategoryManager _categoryManager;
    private ConfigInterface _configManager;

}
